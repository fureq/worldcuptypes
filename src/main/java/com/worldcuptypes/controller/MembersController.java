package com.worldcuptypes.controller;

import com.worldcuptypes.service.MatchService;
import com.worldcuptypes.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MembersController {

    private final MatchService matchService;
    private final PointsService pointsService;

    @PutMapping("/members/group-winners")
    public String addGroupWinners() {
        return matchService.calcGroupWinnersForMembers();
    }

    @PutMapping("/members/predicted-octo")
    public String addPredictedOcto() {
        return matchService.calcPredictedOctoFinalsForMembers();
    }

    @PutMapping("/member/group-winners/calc-points")
    public String calcPointsForPredict() {
        return pointsService.calcPointsForPrediction();
    }
}
