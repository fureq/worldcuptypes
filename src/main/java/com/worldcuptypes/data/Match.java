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
        return home + " " + result.getHomeScore() + ":" + result.getAwayScore() + " " + away;
    }

    public String printTeams() {
        return home + "-" + away;
    }

    public String getResultString() {
        return  result == null ? "n/a" : result.printScore();
    }
}
