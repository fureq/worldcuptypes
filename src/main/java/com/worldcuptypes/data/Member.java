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
public class Member {
    @Id
    private String id;
    private String name;
    private String fullName;
    private int points;
    private Map<Integer, Match> groupMatchTypes;
}
