package com.worldcuptypes.data;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "matches")
@ToString
public class Match {
    @Id
    private String id;
    private Team home;
    private Team away;
    private Result result;
    private Stage stage;

    public int getMatchHash() {
        return Objects.hash(home, away, stage);
    }
}
