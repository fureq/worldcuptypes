package com.worldcuptypes.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "matches")
public class Match {
    private Team home;
    private Team away;
    private Result result;
    private Stage stage;
}
