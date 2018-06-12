package com.worldcuptypes.controller;

import com.worldcuptypes.service.MatchService;
import com.worldcuptypes.service.ResourcesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GroupStageController {

    private final ResourcesService resourcesService;
    private final MatchService matchService;

    @GetMapping("/group/init")
    public String initGroupStage() {
        return resourcesService.initGroupStage();
    }

    @PostMapping("/group/read")
    @ResponseBody
    public String readPlayerGroupTypes(@RequestParam("playerId") String playerId, @RequestParam("fullName") String fullName) {
        return resourcesService.readPlayerGroupMatches(playerId, fullName);
    }

    @PutMapping("/group/result")
    @ResponseBody
    public String addResultAndCalcPoint(@RequestParam("matchNo") Integer matchNo, @RequestParam("score") String score) {
        return matchService.addScoreAndCalculatePoints(matchNo, score);
    }

    @GetMapping("/group/report")
    public String generateReport() {
        return resourcesService.generateCsvReport();
    }
}
