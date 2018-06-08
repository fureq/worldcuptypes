package com.worldcuptypes.service;

import com.worldcuptypes.data.Match;
import com.worldcuptypes.data.Result;
import com.worldcuptypes.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final PointsService pointsService;

    public String addScoreAndCalculatePoints(int matchNo, String score) {
        Match match = matchRepository.findAll().get(matchNo);
        match.setResult(Result.resultFromString(score.split(Result.RESULT_SEPARATOR)));
        log.info("Add score {}", match.printResult());
        matchRepository.save(match);
        pointsService.calculatePointsForMatch(match);
        return "Success";
    }
}
