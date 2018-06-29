package com.worldcuptypes.controller;

import com.worldcuptypes.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/finals")
@RequiredArgsConstructor
public class FinalsController {

    private final MatchService matchService;

    @PostMapping("/octo")
    public String calcOctoFinals() {
        return matchService.calcOctoFinals();
    }

}
