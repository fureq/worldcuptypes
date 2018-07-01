package com.worldcuptypes.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    public static final String RESULT_SEPARATOR = ":";

    private int homeScore;
    private int awayScore;
    private Integer homePenaltyScore;
    private Integer awayPenaltyScore;

    @Override
    public String toString() {
        return homeScore + ":" + awayScore;
    }

    public static Result resultFromString(String resultString) {
        String[] splittedResult;
        if (resultString.contains("(")) {
            resultString = resultString.replace("(", ":");
            resultString = resultString.replace(")", "");

        }
        splittedResult = resultString.split(":");
        Result result = Result.builder()
                .homeScore(Integer.valueOf(splittedResult[StringArrayIndexes.HOME]))
                .awayScore(Integer.valueOf(splittedResult[StringArrayIndexes.AWAY]))
                .build();
        if (splittedResult.length == 4) {
            result.setHomePenaltyScore(Integer.valueOf(splittedResult[StringArrayIndexes.HOME_PENALTY]));
            result.setAwayPenaltyScore(Integer.valueOf(splittedResult[StringArrayIndexes.AWAY_PENALTY]));
        }
        return result;
    }

    public Score getScore() {
        if (homeScore == awayScore) {
            return Score.DRAW;
        } else if (homeScore > awayScore) {
            return Score.WIN;
        } else {
            return Score.LOS;
        }
    }

    public enum Score {
        WIN, LOS, DRAW
    }

    public String printScore() {
        return String.valueOf(homeScore) + ":" + String.valueOf(awayScore);
    }

    public String printPenalties() {
        return "(" + homePenaltyScore.toString() + ":" + awayPenaltyScore.toString() + ")";
    }

    public boolean hasPenalty() {
        return homePenaltyScore != null && awayPenaltyScore != null;
    }
}
