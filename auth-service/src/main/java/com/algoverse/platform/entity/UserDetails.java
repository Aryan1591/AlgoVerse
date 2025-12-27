package com.algoverse.platform.entity;

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
  private String Id;

  @Indexed(unique = true)
  private String Email;

  private String Password;

  private String PhoneNumber;

  private String Username;

  private Qualification EducationQualification;

  private List<String> SocialLinks;

  private Set<Role> roles = new HashSet<>();

  private String CollegeName;

  private Instant createdAt;
  private Instant updatedAt;
}
