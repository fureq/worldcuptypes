package com.worldcuptypes.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    @Id
    private String id;
    private Integer homeScore;
    private Integer awayScore;
}
