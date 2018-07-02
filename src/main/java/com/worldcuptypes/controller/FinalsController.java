package com.worldcuptypes.controller;

import com.worldcuptypes.data.Stage;
import com.worldcuptypes.data.Team;
import com.worldcuptypes.service.MatchService;
import com.worldcuptypes.service.ResourcesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/finals")
@RequiredArgsConstructor
public class FinalsController {

    private final MatchService matchService;
    private final ResourcesService resourcesService;

    @PostMapping("/octo")
    public String calcOctoFinals() {
        return matchService.calcOctoFinals();
    }

    @PutMapping("/result")
    @ResponseBody
    public String addResultAndCalcPoint(
            @RequestParam("home") Team home,
            @RequestParam("away") Team away,
            @RequestParam("stage") Stage stage,
            @RequestParam("score") String score
    ) {
        return matchService.addScoreAndCalculateFinalStagePoints(home, away, stage, score);
    }

    @PutMapping("/read")
    @ResponseBody
    public String readPlayerGroupTypes(@RequestParam("playerId") String playerId) {
        return resourcesService.readPlayerFinalMatches(playerId);
    }

    @GetMapping("/report")
    public String generateReport() {
        return resourcesService.generateCsvFinalStageReport();
    }
}
