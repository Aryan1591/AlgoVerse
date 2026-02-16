// src/main/java/com/algoverse/monolith/oauth/mongo/MongoRegisteredClient.java
package com.algoverse.platform.entity_security;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

// oauth_registered_client
@Document("oauth_registered_client")
public class MongoRegisteredClient {
  @Id public String id;

  @Indexed(unique = true)
  public String clientId;

  public String clientSecret;

  public Set<String> authMethods;      // e.g. "none", "client_secret_basic"
  public Set<String> grantTypes;       // e.g. "authorization_code", "refresh_token"
  public Set<String> redirectUris;
  public Set<String> scopes;

   public String clientSettingsJson; // ✅ JSON string
  public String tokenSettingsJson;  // ✅ JSON string
}