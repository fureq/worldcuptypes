package com.worldcuptypes.repository;

import com.worldcuptypes.data.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MemberRepository extends MongoRepository<Member, String> {
    Optional<Member> findByName(String name);
}
