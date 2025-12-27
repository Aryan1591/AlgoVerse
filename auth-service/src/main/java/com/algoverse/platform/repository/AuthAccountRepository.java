package com.algoverse.platform.repository;

import com.algoverse.platform.entity.AuthAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AuthAccountRepository extends MongoRepository<AuthAccount, String> {
  Optional<AuthAccount> findByEmail(String email);
  boolean existsByEmail(String email);
}
