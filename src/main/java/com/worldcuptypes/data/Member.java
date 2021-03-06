package com.worldcuptypes.data;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "members")
@Data
@Builder
@ToString
public class Member implements Comparable<Member>{

    public static final String WINNER_AND_STRIKER_SEPARATOR = ":";

    @Id
    private String id;
    private String name;
    private String fullName;
    private Team winner;
    private String striker;
    private int points;
    private Integer groupFinalResultPoints;
    private Integer groupStagePoints;
    private Integer finalStagePoints;
    private Map<String, Match> groupMatchTypes;
    private List<Match> octoFinalMatchTypes;
    private Map<String, Match> finalMatchTypes;
    private List<GroupWinners> groupWinners;

    public void addPoints(int points) {
        this.points += points;
    }

    public void addFinalPoints(int points) {
        finalStagePoints = finalStagePoints == null ?
                points :
                finalStagePoints + points;
    }

    @Override
    public int compareTo(Member member) {
        int comparedPoints = Integer.compareUnsigned(member.getPoints(), points);
        return comparedPoints == 0 ? fullName.compareTo(member.fullName) : comparedPoints;
    }
}
