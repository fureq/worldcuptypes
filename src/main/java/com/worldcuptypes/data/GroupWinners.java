package com.worldcuptypes.data;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@ToString
public class GroupWinners {
    Stage group;
    Team firstPlace;
    Team secondPlace;
    Team thirdPlace;
    Team fourthPlace;

    public static GroupWinners fromFinalResults(List<TeamGroupResult> teamGroupResult, Stage stage) {
        return GroupWinners.builder()
                .firstPlace(teamGroupResult.get(0).getTeam())
                .secondPlace(teamGroupResult.get(1).getTeam())
                .thirdPlace(teamGroupResult.get(2).getTeam())
                .fourthPlace(teamGroupResult.get(3).getTeam())
                .group(stage)
                .build();
    }
}
