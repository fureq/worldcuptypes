package com.worldcuptypes.data;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private Map<String, Match> groupMatchTypes;

    public void addPoints(int points) {
        this.points += points;
    }

    @Override
    public int compareTo(Member member) {
        return Integer.compareUnsigned(member.getPoints(), points);
    }
}
