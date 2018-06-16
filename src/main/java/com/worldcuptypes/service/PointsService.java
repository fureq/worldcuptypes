package com.worldcuptypes.service;

import com.worldcuptypes.data.Match;
import com.worldcuptypes.data.Member;
import com.worldcuptypes.data.Result;
import com.worldcuptypes.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointsService {

    private final MemberRepository memberRepository;

    public void clearPoints() {
        memberRepository.findAll().forEach(member -> memberRepository.save(clearPointsAndGetMember(member)));
    }

    void calculatePointsForMatch(Match match) {
        memberRepository.findAll()
                .forEach(member -> memberRepository.save(
                        calculatePointsForSingleMember(member, match))
                );
    }

    private Member clearPointsAndGetMember(Member member) {
        member.setPoints(0);
        return member;
    }

    private Member calculatePointsForSingleMember(Member member, Match match) {
        Match memberType = member.getGroupMatchTypes().get(match.getMatchKey());
        log.info("Player type: {}", memberType.printResult());
        int points = getPoints(match.getResult(), memberType.getResult());
        member.addPoints(points);
        log.info("Player {} score {} points. Overall points: {}", member.getName(), points, member.getPoints());
        return member;
    }

    private int getPoints(Result result, Result type) {
        if (result.getScore().equals(type.getScore())) {
            if (result.getAwayScore() == type.getAwayScore() && result.getHomeScore() == type.getHomeScore()) {
                return 3;
            }
            return 1;
        }
        return 0;
    }
}
