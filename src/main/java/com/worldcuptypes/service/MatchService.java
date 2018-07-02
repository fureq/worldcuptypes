package com.worldcuptypes.service;

import com.worldcuptypes.data.*;
import com.worldcuptypes.repository.GroupWinnersRepository;
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
    private final GroupWinnersRepository groupWinnersRepository;

    @Deprecated
    public String addScoreAndCalculateGroupStagePoints(int matchNo, String score) {
        Match match = matchRepository.findAll().get(matchNo);
        match.setResult(Result.resultFromString(score));
        log.info("Add score {}", match.printResult());
        matchRepository.save(match);
        pointsService.calculatePointsForGroupStageMatch(match);
        return "Success";
    }

    public String addScoreAndCalculateGroupStagePoints(Team home, Team away, Stage stage, String score) {
        Optional<Match> matchOpt = matchRepository.findByHomeAndAwayAndStage(home, away, stage);
//      TODO: Handle by exception
        if (!matchOpt.isPresent()) {
            log.error("Cannot find match: {}:{} from {}", home, away, stage);
            return "Match not found";
        }
        Match match = matchOpt.get();
        match.setResult(Result.resultFromString(score));
        log.info("Add score {}", match.printResult());
        matchRepository.save(match);
        pointsService.calculatePointsForGroupStageMatch(match);
        return match.printResult();
    }

    public String addScoreAndCalculateFinalStagePoints(Team home, Team away, Stage stage, String score) {
        Optional<Match> matchOpt = matchRepository.findByHomeAndAwayAndStage(home, away, stage);
        if (!matchOpt.isPresent()) {
            log.error("Cannot find match : {}:{} from {}", home, away, stage);
            return "Match not found";
        }
        Match match = matchOpt.get();
        match.setResult(Result.resultFromString(score));
        setWinnerInNextRound(match);
        log.info("Add score{}", match.printResult());
//        matchRepository.save(match);
        pointsService.calculatePointsForFinalStageMatch(match);
        return "Success";
    }

    public String calcGroupWinnersForMembers() {
        memberRepository.findAll()
                .forEach(member -> {
                    member.setGroupWinners(calcGroupWinners(new ArrayList<>(member.getGroupMatchTypes().values())));
                    memberRepository.save(member);
                });
        return "Success";
    }

    public String calcGroupWinners() {
        groupWinnersRepository.saveAll(
                calcGroupWinners(matchRepository.findAll())
        );
        return "Success";
    }

    public String calcPredictedOctoFinalsForMembers() {
        memberRepository.findAll()
                .forEach(member -> {
                    member.setOctoFinalMatchTypes(calcOctoFinal(member.getGroupWinners()));
                    memberRepository.save(member);
                });
        return "Success";
    }

    private List<Match> calcOctoFinal(List<GroupWinners> groupWinners) {
        List<Match> octoFinalMatches = new ArrayList<>();
        int j = 1;
        for (int i = 0; i < 8; i += 2) {
            octoFinalMatches.add(Match.builder()
                    .home(groupWinners.get(i).getFirstPlace())
                    .away(groupWinners.get(i + 1).getSecondPlace())
                    .stage(Stage.OCTOFINALS)
                    .matchNumber(j).build());
            j++;
        }
        for (int i = 0; i < 8; i += 2) {
            octoFinalMatches.add(Match.builder()
                    .home(groupWinners.get(i + 1).getFirstPlace())
                    .away(groupWinners.get(i).getSecondPlace())
                    .matchNumber(j)
                    .stage(Stage.OCTOFINALS)
                    .build());
            j++;
        }
        return octoFinalMatches;
    }

    private List<GroupWinners> calcGroupWinners(List<Match> matches) {
        return Arrays.stream(Stage.values())
                .filter(stage -> stage.toString().contains("STAGE"))
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
            updateGroupPoints(teams.get(match.getHome()), teams.get(match.getAway()), match.getResult());
        });
        List<TeamGroupResult> finalResults = teams.values().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
        return GroupWinners.fromFinalResults(finalResults, group);
    }

    private void updateGroupPoints(TeamGroupResult home, TeamGroupResult away, Result result) {
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

    public String calcOctoFinals() {
        matchRepository.saveAll(
                calcOctoFinal(groupWinnersRepository.findAll())
        );
        return "Success";
    }

    private void setWinnerInNextRound(Match match) {
        int nextMatchNo = (int) Math.ceil(match.getMatchNumber()/2);
        Optional<Stage> optionalStage = match.getStage().getNextRound();
        if(!optionalStage.isPresent()) {
            return;
        }
        matchRepository.findByStageAndMatchNumber(optionalStage.get(), nextMatchNo).ifPresent(nextMatch -> {
            if (match.getMatchNumber()%2==0) {
                nextMatch.setAway(match.getWinner());
            } else {
                nextMatch.setHome(match.getWinner());
            }
//            matchRepository.save(nextMatch);
            System.err.println(nextMatch);
        });
    }
}