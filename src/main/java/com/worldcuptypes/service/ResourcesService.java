package com.worldcuptypes.service;

import com.worldcuptypes.data.*;
import com.worldcuptypes.repository.MatchRepository;
import com.worldcuptypes.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResourcesService {

    private static final String GROUP_MATCHES_FILE_PATH = "src/main/resources/matches";
    private static final String GROUP_MATCHES_MEMBERS_PATH = "src/main/resources/types/group/";
    private static final String MATCH_SEPARATOR = " – ";
    private static final String GROUP_KEYWORD = "GRUPA";
    private static final String SPACE_SEPARATOR = " ";
    private static final String RESULT_SEPARATOR = ":";

    private static final int HOME = 0;
    private static final int AWAY = 1;
    private static final int GROUP_CHAR = 1;

    private final MatchRepository matchRepository;
    private final MemberRepository memberRepository;

    public String initGroupStage() {
        if (!matchRepository.findAll().isEmpty()) {
            return "Group Stage already created";
        }
        return readGroupStage();
    }

    public String readPlayerGroupMatches(String playerId, String fullName) {
        if (memberRepository.findByName(playerId).isPresent()){
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
        try (BufferedReader br = new BufferedReader(new FileReader(GROUP_MATCHES_FILE_PATH))){
            String line;
            Stage stage = null;
            while ((line = br.readLine()) != null) {
                if (line.contains(GROUP_KEYWORD)) {
                    stage = stageFromString(line.split(SPACE_SEPARATOR)[GROUP_CHAR]);
                    continue;
                }
                if (line.contains(MATCH_SEPARATOR)) {
                    String[] splittedLine = line.split(MATCH_SEPARATOR);
                    groupStageMatches.add(getMatch(splittedLine, stage));
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
        List<Match> groupStageTypes = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))){
            String line;
            Stage stage = null;
            while ((line = br.readLine()) != null) {
                if (line.contains(GROUP_KEYWORD)) {
                    stage = stageFromString(line.split(SPACE_SEPARATOR)[GROUP_CHAR]);
                    continue;
                }
                if (line.contains(MATCH_SEPARATOR)) {
                    String[] splittedLine = line.split(MATCH_SEPARATOR);
                    Match match = getMatch(splittedLine, stage);
                    match.setResult(getResult(br.readLine().split(RESULT_SEPARATOR)));
                    groupStageTypes.add(match);
                    continue;
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        member.setGroupMatchTypes(groupStageTypes);
        memberRepository.save(member);
        return "Success";
    }

    private Match getMatch(String[] slittedString, Stage stage) {
        Team homeTeam = teamFromString(slittedString[HOME]);
        Team awayTeam = teamFromString(slittedString[AWAY]);
        return Match.builder()
                .away(awayTeam)
                .home(homeTeam)
                .stage(stage)
                .build();
    }

    private Stage stageFromString(String value) {
        return Stage.fromValue(value);
    }

    private Team teamFromString(String value) {
        return Team.fromValue(value);
    }

    private Result getResult(String[] slittedResult) {
        return Result.builder()
                .homeScore(Integer.valueOf(slittedResult[HOME]))
                .awayScore(Integer.valueOf(slittedResult[AWAY]))
                .build();
    }
}
