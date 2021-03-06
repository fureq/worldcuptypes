package com.worldcuptypes.data;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "matches")
@ToString
public class Match {

    public static final String MATCH_SEPARATOR = " – ";

    @Id
    private String id;
    private Team home;

    private Team away;
    private Result result;
    private Stage stage;
    private Integer matchNumber;

    public String getMatchKey() {
        return home.toString() + away.toString() + stage.toString();
    }

    public static Match matchFromString(String[] slittedString, Stage stage) {
        Team homeTeam = Team.fromValue(slittedString[StringArrayIndexes.HOME]);
        Team awayTeam = Team.fromValue(slittedString[StringArrayIndexes.AWAY]);
        return Match.builder()
                .away(awayTeam)
                .home(homeTeam)
                .stage(stage)
                .build();
    }

    public String printResult() {
        return !hasResult() ? "n/a" :
                result.hasPenalty() ?
                        home + " " + result.printScore() + " " + result.printPenalties() + " " + away :
                        home + " " + result.printScore() + " " + away;
    }

    public String printTeams() {
        return home == null || away == null ?
                matchNumber.toString() + " " + stage.toString() :
                home + "-" + away;
    }

    public String getResultString() {
        return result == null ? "n/a" : result.printScore();
    }

    public boolean sameTeams(Match match) {
        return match.getHome().equals(getHome()) && match.getAway().equals(getAway());
    }

    public boolean isFinalStage() {
        return !stage.toString().contains("GROUP");
    }

    public boolean hasResult() {
        return result != null;
    }

    public Team getWinner() {
        switch (result.getScore()) {
            case WIN:
                return home;
            case LOS:
                return away;
            case DRAW:
                if (result.getHomePenaltyScore() > result.getAwayPenaltyScore()) {
                    return home;
                } else {
                    return away;
                }
            default:
                return null;
        }
    }
}
