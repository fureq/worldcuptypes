package com.worldcuptypes.service;

import com.worldcuptypes.data.*;
import com.worldcuptypes.repository.GroupWinnersRepository;
import com.worldcuptypes.repository.MatchRepository;
import com.worldcuptypes.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PointsService {

    private final MemberRepository memberRepository;
    private final GroupWinnersRepository groupWinnersRepository;
    private final MatchRepository matchRepository;

    @Deprecated
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

    public String calcPointsForPrediction() {
        memberRepository.saveAll(
                memberRepository.findAll().stream()
                        .map(this::calcPointsForPrediction)
                        .collect(Collectors.toList())
        );
        return "Success";
    }

    private Member calcPointsForPrediction(Member member) {
        int points = member.getGroupWinners().stream()
                .map(this::calcPointsForGroupWinner).mapToInt(Integer::intValue).sum();
        points += calcPointsForOctoTypes(member.getOctoFinalMatchTypes());
        log.debug("Member {} get {} points", member.getName(), points);
        member.setGroupFinalResultPoints(points);
        return member;
    }

    private int calcPointsForGroupWinner(GroupWinners groupWinners) {
        int points = 0;
        GroupWinners result = groupWinnersRepository.findByGroup(groupWinners.getGroup());
        points += result.hasPromoted(groupWinners.getFirstPlace()) ? 1 : 0;
        points += result.hasPromoted(groupWinners.getSecondPlace()) ? 1 : 0;
        points += result.equals(groupWinners) ? 2 : 0;
        return points;
    }

    private int calcPointsForOctoTypes(List<Match> matchList) {
        List<Match> result = matchRepository.findAllByStage(Stage.OCTOFINALS);
        return matchList.stream()
                .map(match -> {
                    Match thatMatch = result.stream().filter(match1 -> match1.getMatchNumber().equals(match.getMatchNumber())).findFirst().orElseThrow(RuntimeException::new);
                    return thatMatch.sameTeams(match) ? 1 : 0;
                }).mapToInt(Integer::intValue).sum();
    }
}
