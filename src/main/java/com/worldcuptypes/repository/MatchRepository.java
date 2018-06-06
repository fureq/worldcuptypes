package com.worldcuptypes.repository;

import com.worldcuptypes.data.Match;
import com.worldcuptypes.data.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MatchRepository extends MongoRepository<Match, String> {
    Optional<Match> findByHomeAndAway(Team home, Team away);
}
