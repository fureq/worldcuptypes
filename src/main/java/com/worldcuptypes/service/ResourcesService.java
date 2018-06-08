package com.worldcuptypes.service;

import com.worldcuptypes.data.*;
import com.worldcuptypes.repository.MatchRepository;
import com.worldcuptypes.repository.MemberRepository;
import com.worldcuptypes.data.StringArrayIndexes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
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
}
