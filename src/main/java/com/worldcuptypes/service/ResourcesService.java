package com.worldcuptypes.service;

import com.worldcuptypes.data.*;
import com.worldcuptypes.repository.MatchRepository;
import com.worldcuptypes.repository.MemberRepository;
import com.worldcuptypes.data.StringArrayIndexes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourcesService {

    private static final String GROUP_MATCHES_FILE_PATH = "src/main/resources/matches";
    private static final String GROUP_MATCHES_MEMBERS_PATH = "src/main/resources/types/group/";
    private static final String CSV_OUTPUT_FILE_PATH = "src/main/resources/report.csv";
    private static final String SPACE_SEPARATOR = " ";
    private static final String WINNER = "Zwycięzca";
    private static final String STRIKER = "Król Strzelców";

    private final MatchRepository matchRepository;
    private final MemberRepository memberRepository;

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
        return readPlayerTypes(GROUP_MATCHES_MEMBERS_PATH + playerId, member);
    }

    public String generateCsvReport() {
        try (
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_OUTPUT_FILE_PATH));
                CSVPrinter csvPrinter = new CSVPrinter(
                        writer,
                        CSVFormat
                                .DEFAULT
                )
        ) {
            List<Match> matches = matchRepository.findAll();
            csvPrinter.printRecord(getCsvHeader(matches));
            memberRepository.findAll().stream()
                    .sorted()
                    .forEach(member -> printMemberRecord(member, matches, csvPrinter));
            csvPrinter.printRecord(getResults(matches));
        } catch (IOException e) {
            log.error("Cannot write csv file, cause: {}", e.getMessage());
        }
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
                    stage = Stage.fromValue(line.split(SPACE_SEPARATOR)[StringArrayIndexes.GROUP]);
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

    private String readPlayerTypes(String fileName, Member member) {
        log.info("Reading {} matches", member.getName());
        Map<String, Match> groupStageTypes = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            Stage stage = null;
            while ((line = br.readLine()) != null) {
                if (line.contains(Stage.GROUP_KEYWORD)) {
                    stage = Stage.fromValue(line.split(SPACE_SEPARATOR)[StringArrayIndexes.GROUP]);
                    continue;
                }
                if (line.contains(Match.MATCH_SEPARATOR)) {
                    String[] splittedLine = line.split(Match.MATCH_SEPARATOR);
                    Match match = Match.matchFromString(splittedLine, stage);
                    match.setResult(Result.resultFromString(br.readLine().split(Result.RESULT_SEPARATOR)));
                    groupStageTypes.put(match.getMatchKey(), match);
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
        member.setGroupMatchTypes(groupStageTypes);
        memberRepository.save(member);
        return "Success";
    }

    private List<String> getCsvHeader(List<Match> matches) {
        List<String> list = new ArrayList<>();
        list.add("Uczestnik");
        list.add("Punkty");
        matches.forEach(match -> list.add(match.printTeams()));
        list.add("Zwyciezca Mundialu");
        list.add("Krol Strzelcow");
        return list;
    }

    private void printMemberRecord(Member member, List<Match> matches, CSVPrinter csvPrinter) {
        try {
            csvPrinter.printRecord(getCsvRecord(member, matches));
        } catch (IOException e) {
            log.error("Cannot write record for: {}", member.getName());
        }

    }

    private List<String> getCsvRecord(Member member, List<Match> matches) {
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
        results.add("Results");
        results.add("n/a");
        matches.forEach(match -> results.add(match.getResultString()));
        return results;
    }
}
