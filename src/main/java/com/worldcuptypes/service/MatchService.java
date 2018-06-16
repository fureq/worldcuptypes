package com.worldcuptypes.service;

import com.worldcuptypes.data.Match;
import com.worldcuptypes.data.Result;
import com.worldcuptypes.data.Stage;
import com.worldcuptypes.data.Team;
import com.worldcuptypes.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final PointsService pointsService;

    @Deprecated
    public String addScoreAndCalculatePoints(int matchNo, String score) {
        Match match = matchRepository.findAll().get(matchNo);
        match.setResult(Result.resultFromString(score.split(Result.RESULT_SEPARATOR)));
        log.info("Add score {}", match.printResult());
        matchRepository.save(match);
        pointsService.calculatePointsForMatch(match);
        return "Success";
    }

    public String addScoreAndCalculatePoints(Team home, Team away, Stage stage, String score) {
        Optional<Match> matchOpt = matchRepository.findByHomeAndAwayAndStage(home, away, stage);
//      TODO: Handle by exception
        if(!matchOpt.isPresent()) {
            log.error("Cannot find match: {}:{} from {}", home, away, stage);
            return "Match not found";
        }
        Match match = matchOpt.get();
        match.setResult(Result.resultFromString(score.split(Result.RESULT_SEPARATOR)));
        log.info("Add score {}", match.printResult());
        matchRepository.save(match);
        pointsService.calculatePointsForMatch(match);
        return "Success";
    }


}
