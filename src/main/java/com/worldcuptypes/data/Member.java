package com.worldcuptypes.data;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "members")
@Data
@Builder
public class Member {
    @Id
    private String id;
    private String name;
    private String fullName;
    private int points;
    private List<Type> types;
}
