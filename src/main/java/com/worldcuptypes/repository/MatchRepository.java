package com.worldcuptypes.repository;

import com.worldcuptypes.data.Match;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchRepository extends MongoRepository<Match, String> {
}
