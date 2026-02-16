package com.algoverse.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication(scanBasePackages = {"com.algoverse.platform"})
@EnableMongoRepositories(basePackages = {
    "com.algoverse.platform.repository",
    "com.algoverse.platform.repository_security"
})
public class AlgoVerseBackendApplication {
  public static void main(String[] args) {
    SpringApplication.run(AlgoVerseBackendApplication.class, args);
  }
}

