package com.worldcuptypes.service;

import com.worldcuptypes.data.*;
import com.worldcuptypes.repository.MatchRepository;
import com.worldcuptypes.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final MemberRepository memberRepository;
    private final PointsService pointsService;

    @Deprecated
    public String addScoreAndCalculatePoints(int matchNo, String score) {
        Match match = matchRepository.findAll().get(matchNo);
        match.setResult(Result.resultFromString(score.split(Result.RESULT_SEPARATOR)));
        log.info("Add score {}", match.printResult());
        matchRepository.save(match);
        pointsService.calculatePointsForMatch(match);
        return "Success";
    }

    public String addScoreAndCalculatePoints(Team home, Team away, Stage stage, String score) {
        Optional<Match> matchOpt = matchRepository.findByHomeAndAwayAndStage(home, away, stage);
//      TODO: Handle by exception
        if (!matchOpt.isPresent()) {
            log.error("Cannot find match: {}:{} from {}", home, away, stage);
            return "Match not found";
        }
        Match match = matchOpt.get();
        match.setResult(Result.resultFromString(score.split(Result.RESULT_SEPARATOR)));
        log.info("Add score {}", match.printResult());
        matchRepository.save(match);
        pointsService.calculatePointsForMatch(match);
        return "Success";
    }

    public String calcGroupWinnersForMembers() {
        memberRepository.findAll()
                .forEach(member -> {
                    if(member.getGroupWinners() == null || member.getGroupWinners().isEmpty()) {
                        return;
                    }
                    member.setGroupWinners(calcGroupWinners(new ArrayList<>(member.getGroupMatchTypes().values())));
                    memberRepository.save(member);
                });
        return "Success";
    }

    private List<GroupWinners> calcGroupWinners(List<Match> matches) {
        return Arrays.stream(Stage.values())
                .filter(stage -> stage.toString().contains("GROUP"))
                .map(stage -> calcGroupWinner(
                        matches.stream().filter(match -> match.getStage().equals(stage)).collect(Collectors.toList()),
                        stage
                ))
                .collect(Collectors.toList());
    }

    private GroupWinners calcGroupWinner(List<Match> matches, Stage group) {
        Map<Team, TeamGroupResult> teams = new HashMap<>();
        matches.forEach(match -> {
            if (!teams.containsKey(match.getHome())) {
                teams.put(match.getHome(), new TeamGroupResult(match.getHome()));
            }
            if (!teams.containsKey(match.getAway())) {
                teams.put(match.getAway(), new TeamGroupResult(match.getAway()));
            }
            updateResults(teams.get(match.getHome()), teams.get(match.getAway()), match.getResult());
        });
        List<TeamGroupResult> finalResults = teams.values().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        return GroupWinners.fromFinalResults(finalResults, group);
    }

    private void updateResults(TeamGroupResult home, TeamGroupResult away, Result result) {
        switch (result.getScore()) {
            case WIN:
                home.win();
                break;
            case LOS:
                away.win();
                break;
            case DRAW:
                home.draw();
                away.draw();
                break;
        }
        home.updateGoals(result.getHomeScore(), result.getAwayScore());
        away.updateGoals(result.getAwayScore(), result.getHomeScore());
    }
}
