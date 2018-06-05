package com.worldcuptypes.service;

import com.worldcuptypes.data.Match;
import com.worldcuptypes.data.Stage;
import com.worldcuptypes.data.Team;
import com.worldcuptypes.repository.MatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ResourcesReaderService {

    private static final String GROUP_MATCHES_FILE_PATH = "src/main/resources/matches";
    private static final String MATCH_SEPARATOR = " – ";
    private static final String GROUP_KEYWORD = "GRUPA";
    private static final String SPACE_SEPARATOR = " ";

    private static final int HOME = 0;
    private static final int AWAY = 1;
    private static final int GROUP_CHAR = 1;

    private final MatchRepository matchRepository;


    public ResourcesReaderService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @PostConstruct
    private void init() {
        if (!matchRepository.findAll().isEmpty()) {
            return;
        }
        readGroupStage();
    }

    private void readGroupStage() {
        log.debug("Reading matches");
        List<Match> groupStageMatches = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(GROUP_MATCHES_FILE_PATH))){
            String line;
            Stage stage = null;
            while ((line = br.readLine()) != null) {
                if (line.contains(GROUP_KEYWORD)) {
                    stage = stageFromString(line.split(SPACE_SEPARATOR)[GROUP_CHAR]);
                }
                if (line.contains(MATCH_SEPARATOR)) {
                    String[] splittedLine = line.split(MATCH_SEPARATOR);
                    groupStageMatches.add(getMatch(splittedLine, stage));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        matchRepository.saveAll(groupStageMatches);
    }

    private Match getMatch(String[] splittedString, Stage stage) {
        Team homeTeam = teamFromString(splittedString[HOME]);
        Team awayTeam = teamFromString(splittedString[AWAY]);
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
}
