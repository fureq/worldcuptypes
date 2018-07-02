package com.worldcuptypes.service;

import com.worldcuptypes.data.*;
import com.worldcuptypes.repository.GroupWinnersRepository;
import com.worldcuptypes.repository.MatchRepository;
import com.worldcuptypes.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    void calculatePointsForGroupStageMatch(Match match) {
        memberRepository.saveAll(
                memberRepository.findAll().stream()
                        .map(member -> calculatePointsForSingleMember(member, match))
                        .collect(Collectors.toList())
        );
    }

    void calculatePointsForFinalStageMatch(Match match) {
//        memberRepository.saveAll(
        memberRepository.findAll().stream()
                .map(member -> calculatePointsForFinalStageMatchForSingleMember(member, match))
                .collect(Collectors.toList());
//        );
    }

    private Member calculatePointsForFinalStageMatchForSingleMember(Member member, Match match) {
        if(member.getFinalMatchTypes() == null || member.getFinalMatchTypes().isEmpty()) {
            return member;
        }
        Optional.ofNullable(member.getFinalMatchTypes().get(match.getMatchKey())).ifPresent(memberType -> {
            log.info("{} type: {}", member.getName(), memberType.printResult());
            int points = getPointsForFinalStage(match.getResult(), memberType.getResult());
            member.addPoints(points);
            member.addFinalPoints(points);
            log.info("{} score {}. Overall points: {}", member.getName(), points, member.getPoints());
        });

        return member;
    }

    private Member clearPointsAndGetMember(Member member) {
        member.setPoints(0);
        return member;
    }

    private Member calculatePointsForSingleMember(Member member, Match match) {
        Match memberType = member.getGroupMatchTypes().get(match.getMatchKey());
        log.info("Player type: {}", memberType.printResult());
        int points = getPointsForGroupStage(match.getResult(), memberType.getResult());
        member.addPoints(points);
        log.info("Player {} score {} points. Overall points: {}", member.getName(), points, member.getPoints());
        return member;
    }

    private int getPointsForGroupStage(Result result, Result type) {
        if (result.getScore().equals(type.getScore())) {
            if (isExactScore(result, type)) {
                return 3;
            }
            return 1;
        }
        return 0;
    }

    private int getPointsForFinalStage(Result result, Result type) {
        int point = 0;
        if (result.getScore().equals(type.getScore())) {
            point += 2;
            if (isExactScore(result, type)) {
                point += 2;
            }
            if (result.getScore().equals(Result.Score.DRAW)) {
                point += 2;
                if (result.getAwayPenaltyScore().equals(type.getAwayPenaltyScore()) && result.getHomePenaltyScore().equals(type.getHomePenaltyScore())) {
                    point += 2;
                }
            }
        }
        return point;
    }

    private boolean isExactScore(Result result, Result type) {
        return result.getAwayScore() == type.getAwayScore() && result.getHomeScore() == type.getHomeScore();
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
