package com.worldcuptypes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.worldcuptypes")
public class WorldCupTypesApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorldCupTypesApplication.class, args);
    }

}
