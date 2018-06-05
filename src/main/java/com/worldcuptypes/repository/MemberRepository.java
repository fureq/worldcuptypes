package com.worldcuptypes.repository;

import com.worldcuptypes.data.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberRepository extends MongoRepository<Member, String> {

}
