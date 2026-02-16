// src/main/java/com/algoverse/platform/service_security/MongoOAuth2AuthorizationService.java
package com.algoverse.platform.service_security;

import com.algoverse.platform.entity_security.MongoOAuth2Authorization;
import com.algoverse.platform.repository_security.MongoAuthorizationRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.stereotype.Component;

@Component
public class MongoOAuth2AuthorizationService implements OAuth2AuthorizationService {

  private final MongoAuthorizationRepo repo;
  private final ObjectMapper om;

  public MongoOAuth2AuthorizationService(MongoAuthorizationRepo repo,
                                        @Qualifier("sasObjectMapper") ObjectMapper sasObjectMapper) {
    this.repo = repo;
    this.om = sasObjectMapper;
  }

  @Override
  public void save(OAuth2Authorization authorization) {
    try {
      MongoOAuth2Authorization doc = new MongoOAuth2Authorization();
      doc.id = authorization.getId();
      doc.registeredClientId = authorization.getRegisteredClientId();
      doc.principalName = authorization.getPrincipalName();
      doc.state = authorization.getAttribute("state");

      var code = authorization.getToken(OAuth2AuthorizationCode.class);
      if (code != null) doc.authorizationCodeValue = code.getToken().getTokenValue();

      var at = authorization.getAccessToken();
      if (at != null) doc.accessTokenValue = at.getToken().getTokenValue();

      var rt = authorization.getRefreshToken();
      if (rt != null) doc.refreshTokenValue = rt.getToken().getTokenValue();

      doc.dataJson = om.writeValueAsString(authorization);
      repo.save(doc);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to save authorization", e);
    }
  }

  @Override
  public void remove(OAuth2Authorization authorization) {
    repo.deleteById(authorization.getId());
  }

  @Override
  public OAuth2Authorization findById(String id) {
    return repo.findById(id).map(this::toAuth).orElse(null);
  }

  @Override
  public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
    if (token == null) return null;

    if (tokenType == null) {
      var a = repo.findByAccessTokenValue(token).map(this::toAuth).orElse(null);
      if (a != null) return a;

      a = repo.findByRefreshTokenValue(token).map(this::toAuth).orElse(null);
      if (a != null) return a;

      a = repo.findByAuthorizationCodeValue(token).map(this::toAuth).orElse(null);
      if (a != null) return a;

      return repo.findByState(token).map(this::toAuth).orElse(null);
    }

    if (OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) {
      return repo.findByAccessTokenValue(token).map(this::toAuth).orElse(null);
    }

    if (OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) {
      return repo.findByRefreshTokenValue(token).map(this::toAuth).orElse(null);
    }

    if (OAuth2ParameterNames.CODE.equals(tokenType.getValue())) {
      return repo.findByAuthorizationCodeValue(token).map(this::toAuth).orElse(null);
    }

    if (OAuth2ParameterNames.STATE.equals(tokenType.getValue())) {
      return repo.findByState(token).map(this::toAuth).orElse(null);
    }

    return null;
  }

  private OAuth2Authorization toAuth(MongoOAuth2Authorization doc) {
    try {
      return om.readValue(doc.dataJson, OAuth2Authorization.class);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse OAuth2Authorization JSON", e);
    }
  }
}
