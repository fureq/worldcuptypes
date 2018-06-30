package com.worldcuptypes.repository;

import com.worldcuptypes.data.GroupWinners;
import com.worldcuptypes.data.Stage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupWinnersRepository extends MongoRepository<GroupWinners, String> {
    GroupWinners findByGroup(Stage group);
}
