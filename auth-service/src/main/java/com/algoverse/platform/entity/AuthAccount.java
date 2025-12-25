package com.algoverse.platform.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "auth_accounts")
@Data
public class AuthAccount {
  @Id
  private String id;

  @Indexed(unique = true)
  private String email;

  private String passwordHash;

  private Set<Role> roles = new HashSet<>();

  private Instant createdAt;
  private Instant updatedAt;
}
