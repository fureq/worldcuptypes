package com.worldcuptypes.data;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@ToString
@Document(collection = "group_winners")
@EqualsAndHashCode(exclude = "id")
public class GroupWinners {
    @Id
    private String id;
    Stage group;
    Team firstPlace;
    Team secondPlace;
    Team thirdPlace;
    Team fourthPlace;

    public String getGroupOrder() {
        return firstPlace + ", " + secondPlace + ", " + thirdPlace + ", " + fourthPlace;
    }

    public boolean hasPromoted(Team team) {
        return firstPlace.equals(team) || secondPlace.equals(team);
    }

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
