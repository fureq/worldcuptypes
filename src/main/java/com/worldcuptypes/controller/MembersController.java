package com.worldcuptypes.controller;

import com.worldcuptypes.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MembersController {

    private final MemberRepository memberRepository;

    //    Method for debug and tests
    @GetMapping("/points/clear")
    public String clearPoints() {
        return "Success";
    }
}
