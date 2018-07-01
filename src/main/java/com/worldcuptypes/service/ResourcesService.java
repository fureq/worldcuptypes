package com.worldcuptypes.service;

import com.worldcuptypes.data.*;
import com.worldcuptypes.repository.GroupWinnersRepository;
import com.worldcuptypes.repository.MatchRepository;
import com.worldcuptypes.repository.MemberRepository;
import com.worldcuptypes.data.StringArrayIndexes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourcesService {

    private static final String GROUP_MATCHES_FILE_PATH = "src/main/resources/matches";
    private static final String GROUP_MATCHES_MEMBERS_PATH = "src/main/resources/types/group/";
    private static final String FINAL_MATCHES_MEMBERS_PATH = "src/main/resources/types/finals/";
    private static final String CSV_OUTPUT_GROUP_STAGE_FILE_PATH = "src/main/resources/report.csv";
    private static final String CSV_OUTPUT_GROUP_RESULT_FILE_PATH = "src/main/resources/groupFinalResult.csv";
    private static final String MATCH_NUMBERS_FILE = "src/main/resources/matchesOrdered";
    private static final String SPACE_SEPARATOR = " ";
    private static final String WINNER = "Zwycięzca";
    private static final String STRIKER = "Król Strzelców";

    private final MatchRepository matchRepository;
    private final MemberRepository memberRepository;
    private final GroupWinnersRepository groupWinnersRepository;

    public String initGroupStage() {
        if (!matchRepository.findAll().isEmpty()) {
            return "Group Stage already created";
        }
        return readGroupStage();
    }

    public String readPlayerGroupMatches(String playerId, String fullName) {
        if (memberRepository.findByName(playerId).isPresent()) {
            return "Member already exists";
        }
        Member member = Member.builder()
                .name(playerId)
                .fullName(fullName)
                .points(0)
                .build();
        return readPlayerTypes(GROUP_MATCHES_MEMBERS_PATH + playerId, member, false);
    }

    public String readPlayerFinalMatches(String playerId) {
        Optional<Member> memberOpt = memberRepository.findByName(playerId);
        if(!memberOpt.isPresent()) {
            log.error("Cannot find member {}", playerId);
            return "Cannot find member " + playerId;
        }
        Member member = memberOpt.get();
        member.setGroupStagePoints(member.getPoints());
        return readPlayerTypes(FINAL_MATCHES_MEMBERS_PATH + playerId, member, true);
    }

    public String generateCsvGroupStageReport() {
        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_OUTPUT_GROUP_STAGE_FILE_PATH));
                CSVPrinter csvPrinter = new CSVPrinter(
                        writer,
                        CSVFormat
                                .DEFAULT
                )
        ) {
            List<Match> matches = matchRepository.findAll().stream().filter(match -> match.getStage().toString().contains("STAGE")).sorted(Comparator.comparing(Match::getMatchNumber)).collect(Collectors.toList());
            csvPrinter.printRecord(getCsvGroupStageHeader(matches));
            memberRepository.findAll().stream()
                    .sorted()
                    .forEach(member -> printMemberGroupStageRecord(member, matches, csvPrinter));
            csvPrinter.printRecord(getResults(matches));
        } catch (IOException e) {
            log.error("Cannot write csv file, cause: {}", e.getMessage());
        }
        return "Success";
    }

    public String generateCsvGroupResultReport() {
        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_OUTPUT_GROUP_RESULT_FILE_PATH));
                CSVPrinter csvPrinter = new CSVPrinter(
                        writer,
                        CSVFormat
                                .DEFAULT
                )
        ) {
            csvPrinter.printRecord(
                    getCsvGroupResultHeader(Arrays.stream(Stage.values())
                            .filter(stage -> stage.toString().contains("STAGE"))
                            .collect(Collectors.toList()))
            );
            memberRepository.findAll().stream()
                    .sorted((o1, o2) -> o2.getGroupFinalResultPoints().compareTo(o1.getGroupFinalResultPoints()))
                    .forEach(member -> printMemberGroupFinal(member, csvPrinter));
            csvPrinter.printRecord(
                    printMatchesGroupFinal()
            );
        } catch (IOException e) {
            log.error("Cannot write csv file, cause: {}", e.getMessage());
        }
        return "Success";
    }

    private List<String> printMatchesGroupFinal() {
        List<String> csvRecord = new ArrayList<>();
        csvRecord.add("Wyniki:");
        csvRecord.add("");
        groupWinnersRepository.findAll().forEach(groupWinners -> {
            csvRecord.add(groupWinners.getGroupOrder());
            csvRecord.add(groupWinners.getFirstPlace().toString());
            csvRecord.add(groupWinners.getSecondPlace().toString());
        });
        matchRepository.findAllByStage(Stage.OCTOFINALS).stream()
                .map(Match::printTeams)
                .forEach(csvRecord::add);
        return csvRecord;
    }

    private void printMemberGroupFinal(Member member, CSVPrinter csvPrinter) {
        List<String> csvRecord = new ArrayList<>();
        csvRecord.add(member.getFullName());
        csvRecord.add(Optional.ofNullable(member.getGroupFinalResultPoints()).map(Object::toString).orElse(""));
        member.getGroupWinners().forEach(groupWinners -> {
            csvRecord.add(groupWinners.getGroupOrder());
            csvRecord.add(groupWinners.getFirstPlace().toString());
            csvRecord.add(groupWinners.getSecondPlace().toString());
        });
        member.getOctoFinalMatchTypes().stream().map(Match::printTeams).forEach(csvRecord::add);
        try {
            csvPrinter.printRecord(csvRecord);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public String addMatchNoField() {
        log.info("Adding new field");
        List<Match> matches = matchRepository.findAll();
        try (BufferedReader br = new BufferedReader(new FileReader(MATCH_NUMBERS_FILE))) {
            String line;
            int matchNumber = 1;
            while ((line = br.readLine()) != null) {
                Match matchFromFile = Match.matchFromString(line.split(Match.MATCH_SEPARATOR), null);
                Optional<Match> matchOptional = matches.stream()
                        .filter(match -> match.getAway().equals(matchFromFile.getAway()) && match.getHome().equals(matchFromFile.getHome()))
                        .findFirst();
                if (matchOptional.isPresent()) {
                    log.info("{} match no {}", matchOptional.get().printTeams(), matchNumber);
                    matchOptional.get().setMatchNumber(matchNumber);
                    matchNumber++;
                }

            }
        } catch (IOException e) {
            log.error(e.getMessage());
            return "ERROR";
        }
        matchRepository.saveAll(matches);
        return "Success";
    }

    private String readGroupStage() {
        log.info("Reading matches");
        List<Match> groupStageMatches = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(GROUP_MATCHES_FILE_PATH))) {
            String line;
            Stage stage = null;
            while ((line = br.readLine()) != null) {
                if (line.contains(Stage.GROUP_KEYWORD)) {
                    stage = Stage.fromValue(line.split(SPACE_SEPARATOR)[StringArrayIndexes.STAGE]);
                    continue;
                }
                if (line.contains(Match.MATCH_SEPARATOR)) {
                    String[] splittedLine = line.split(Match.MATCH_SEPARATOR);
                    groupStageMatches.add(Match.matchFromString(splittedLine, stage));
                    continue;
                }
                log.info("Skipping line: " + line);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        matchRepository.saveAll(groupStageMatches);
        return "Success";
    }

    private String readPlayerTypes(String fileName, Member member, boolean isFinals) {
        log.info("Reading {} matches", member.getName());
        Map<String, Match> types = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            Stage stage = null;
            while ((line = br.readLine()) != null) {
                if (Stage.isStageString(line)) {
                    String stageString = line.contains(SPACE_SEPARATOR) ? line.split(SPACE_SEPARATOR)[StringArrayIndexes.STAGE] : line;
                    stage = Stage.fromValue(stageString);
                    log.info(stage.toString());
                    continue;
                }
                if (line.contains(Match.MATCH_SEPARATOR)) {
                    String[] splittedLine = line.split(Match.MATCH_SEPARATOR);
                    Match match = Match.matchFromString(splittedLine, stage);
                    match.setResult(Result.resultFromString(br.readLine()));
                    types.put(match.getMatchKey(), match);
                    log.info("Player {} type: {}", member.getName(), match.printResult());
                    continue;
                }
                if (line.contains(WINNER)) {
                    member.setWinner(Team.fromValue(line.split(Member.WINNER_AND_STRIKER_SEPARATOR)[StringArrayIndexes.WINNER_AND_STRIKER]));
                    log.info("Player {} type as winner: {}", member.getName(), member.getWinner());
                    continue;
                }
                if (line.contains(STRIKER)) {
                    member.setStriker(line.split(Member.WINNER_AND_STRIKER_SEPARATOR)[StringArrayIndexes.WINNER_AND_STRIKER]);
                    log.info("Player {} type as Best Striker: {}", member.getName(), member.getStriker());
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        if(isFinals) {
            member.setFinalMatchTypes(types);
        } else {
            member.setGroupMatchTypes(types);
        }
        memberRepository.save(member);
        return "Success";
    }

    private List<String> getCsvGroupStageHeader(List<Match> matches) {
        List<String> list = new ArrayList<>();
        list.add("Uczestnik");
        list.add("Punkty");
        matches.forEach(match -> list.add(match.printTeams()));
        list.add("Zwyciezca Mundialu");
        list.add("Krol Strzelcow");
        return list;
    }

    private void printMemberGroupStageRecord(Member member, List<Match> matches, CSVPrinter csvPrinter) {
        try {
            csvPrinter.printRecord(getGroupStageCsvRecord(member, matches));
        } catch (IOException e) {
            log.error("Cannot write record for: {}", member.getName());
        }

    }

    private List<String> getGroupStageCsvRecord(Member member, List<Match> matches) {
        List<String> csvRecord = new ArrayList<>();
        csvRecord.add(member.getFullName());
        csvRecord.add(String.valueOf(member.getPoints()));
        matches.forEach(match -> csvRecord.add(member.getGroupMatchTypes().get(match.getMatchKey()).getResultString()));
        csvRecord.add(member.getWinner().toString());
        csvRecord.add(member.getStriker());
        return csvRecord;
    }

    private List<String> getResults(List<Match> matches) {
        List<String> results = new ArrayList<>();
        results.add("Wyniki fazy");
        results.add("grupowej");
        matches.forEach(match -> results.add(match.getResultString()));
        return results;
    }

    private List<String> getCsvGroupResultHeader(List<Stage> groups) {
        List<String> csvRecord = new ArrayList<>();
        csvRecord.add("Uczestnik");
        csvRecord.add("Punkty");
        groups.forEach(group -> {
            csvRecord.add(group.toString());
            csvRecord.add("1 miejsce");
            csvRecord.add("2 miejsce");
        });
        for (int i = 1; i <= 8; i++) {
            csvRecord.add(i + " 1/8");
        }
        return csvRecord;
    }
}
