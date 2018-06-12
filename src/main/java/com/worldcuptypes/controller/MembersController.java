package com.worldcuptypes.controller;

import com.worldcuptypes.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MembersController {

    private final PointsService pointsService;

    //    Method for debug and tests
    @GetMapping("/points/clear")
    public String clearPoints() {
        pointsService.clearPoints();
        return "Success";
    }
}
