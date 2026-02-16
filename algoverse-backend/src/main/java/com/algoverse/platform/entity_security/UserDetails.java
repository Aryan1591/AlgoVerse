package com.algoverse.platform.entity_security;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "user_details")
public class UserDetails {
     @Id
  private String id;

  @Indexed(unique = true)
  private String email;

  private String password;

  private String phoneNumber;

  private String username;

  private Qualification educationQualification;

  private List<String> socialLinks;

  private Set<Role> roles = new HashSet<>();

  private String collegeName;

  private Instant createdAt;
  private Instant updatedAt;
}
