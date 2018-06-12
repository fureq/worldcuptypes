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

    @Override
    public String toString() {
        return homeScore + ":" + awayScore;
    }

    public static Result resultFromString(String[] slittedResult) {
        return Result.builder()
                .homeScore(Integer.valueOf(slittedResult[StringArrayIndexes.HOME]))
                .awayScore(Integer.valueOf(slittedResult[StringArrayIndexes.AWAY]))
                .build();
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
}
