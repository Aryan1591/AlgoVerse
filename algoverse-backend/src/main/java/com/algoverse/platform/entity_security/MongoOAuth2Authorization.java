// src/main/java/com/algoverse/monolith/oauth/mongo/MongoOAuth2Authorization.java
package com.algoverse.platform.entity_security;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("oauth_authorization")
public class MongoOAuth2Authorization {
  @Id public String id;

  @Indexed
  public String registeredClientId;

  @Indexed
  public String principalName;

  @Indexed
  public String state;

  @Indexed
  public String authorizationCodeValue;

  @Indexed
  public String accessTokenValue;

  @Indexed
  public String refreshTokenValue;

  // full OAuth2Authorization JSON
  public String dataJson;
}
