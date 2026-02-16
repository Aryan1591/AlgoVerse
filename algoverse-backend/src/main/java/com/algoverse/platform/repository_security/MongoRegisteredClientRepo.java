// src/main/java/com/algoverse/monolith/oauth/mongo/MongoRegisteredClientRepo.java
package com.algoverse.platform.repository_security;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.algoverse.platform.entity_security.MongoRegisteredClient;


public interface MongoRegisteredClientRepo extends MongoRepository<MongoRegisteredClient, String> {
  Optional<MongoRegisteredClient> findByClientId(String clientId);
}
