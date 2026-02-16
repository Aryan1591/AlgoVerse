package com.algoverse.platform.repository_security;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.algoverse.platform.entity_security.AuthAccount;

import java.util.Optional;

public interface AuthAccountRepository extends MongoRepository<AuthAccount, String> {
  Optional<AuthAccount> findByEmail(String email);
  boolean existsByEmail(String email);
}
