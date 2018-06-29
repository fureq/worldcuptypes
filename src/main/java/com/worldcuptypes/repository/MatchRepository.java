package com.worldcuptypes.repository;

import com.worldcuptypes.data.Match;
import com.worldcuptypes.data.Stage;
import com.worldcuptypes.data.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends MongoRepository<Match, String> {
    Optional<Match> findByHomeAndAwayAndStage(Team home, Team away, Stage stage);

    List<Match> findAllByStage(Stage stage);
}
