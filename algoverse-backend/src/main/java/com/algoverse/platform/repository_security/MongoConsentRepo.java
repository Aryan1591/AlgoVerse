// src/main/java/com/algoverse/monolith/oauth/mongo/MongoConsentRepo.java
package com.algoverse.platform.repository_security;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.algoverse.platform.entity_security.MongoOAuth2Consent;

import java.util.Optional;

public interface MongoConsentRepo extends MongoRepository<MongoOAuth2Consent, String> {
  Optional<MongoOAuth2Consent> findByRegisteredClientIdAndPrincipalName(String clientId, String principalName);
  void deleteByRegisteredClientIdAndPrincipalName(String clientId, String principalName);
}
