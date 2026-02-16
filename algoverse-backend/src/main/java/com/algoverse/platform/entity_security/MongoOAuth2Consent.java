// src/main/java/com/algoverse/monolith/oauth/mongo/MongoOAuth2Consent.java
package com.algoverse.platform.entity_security;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("oauth_consent")
public class MongoOAuth2Consent {
  @Id public String id;

  @Indexed
  public String registeredClientId;

  @Indexed
  public String principalName;

  public String dataJson;
}
