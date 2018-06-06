package com.worldcuptypes.controller;

import com.worldcuptypes.service.ResourcesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GroupStageController {

    private final ResourcesService resourcesService;

    @GetMapping("/group/init")
    public String initGroupStage() {
        return resourcesService.initGroupStage();
    }

    @PostMapping("/group/read/{playerId}")
    @ResponseBody
    public String readPlayerGroupTypes(@PathVariable String playerId, @RequestParam("fullName") String fullName) {
        return resourcesService.readPlayerGroupMatches(playerId, fullName);
    }
}
