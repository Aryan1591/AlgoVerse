// src/main/java/com/algoverse/monolith/oauth/mongo/MongoAuthorizationRepo.java
package com.algoverse.platform.repository_security;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.algoverse.platform.entity_security.MongoOAuth2Authorization;

import java.util.Optional;

public interface MongoAuthorizationRepo extends MongoRepository<MongoOAuth2Authorization, String> {
  Optional<MongoOAuth2Authorization> findByState(String state);
  Optional<MongoOAuth2Authorization> findByAuthorizationCodeValue(String v);
  Optional<MongoOAuth2Authorization> findByAccessTokenValue(String v);
  Optional<MongoOAuth2Authorization> findByRefreshTokenValue(String v);
}
