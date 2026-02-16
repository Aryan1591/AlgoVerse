// src/main/java/com/algoverse/platform/service_security/MongoRegisteredClientRepository.java
package com.algoverse.platform.service_security;

import com.algoverse.platform.entity_security.MongoRegisteredClient;
import com.algoverse.platform.repository_security.MongoRegisteredClientRepo;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

@Component
public class MongoRegisteredClientRepository implements RegisteredClientRepository {

  private final MongoRegisteredClientRepo repo;
  private final ObjectMapper om;

  public MongoRegisteredClientRepository(MongoRegisteredClientRepo repo,
                                        @Qualifier("sasObjectMapper") ObjectMapper om) {
    this.repo = repo;
    this.om = om;
  }

  @Override
  public void save(RegisteredClient rc) {
    try {
      // ✅ Correct: lookup by clientId field (NOT Mongo _id)
      MongoRegisteredClient d = repo.findByClientId(rc.getClientId())
          .orElse(new MongoRegisteredClient());

      // Mongo _id should be rc.getId()
      d.id = rc.getId();
      d.clientId = rc.getClientId();
      d.clientSecret = rc.getClientSecret();

      d.authMethods = rc.getClientAuthenticationMethods().stream()
          .map(ClientAuthenticationMethod::getValue)
          .collect(Collectors.toSet());

      d.grantTypes = rc.getAuthorizationGrantTypes().stream()
          .map(AuthorizationGrantType::getValue)
          .collect(Collectors.toSet());

      d.redirectUris = new HashSet<>(rc.getRedirectUris());
      d.scopes = new HashSet<>(rc.getScopes());

      d.clientSettingsJson = om.writeValueAsString(rc.getClientSettings().getSettings());
      d.tokenSettingsJson  = om.writeValueAsString(rc.getTokenSettings().getSettings());

      repo.save(d);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to save RegisteredClient", e);
    }
  }

  @Override
  public RegisteredClient findById(String id) {
    return repo.findById(id).map(this::fromDoc).orElse(null);
  }

  @Override
  public RegisteredClient findByClientId(String clientId) {
    return repo.findByClientId(clientId).map(this::fromDoc).orElse(null);
  }

  @SuppressWarnings("unchecked")
  private RegisteredClient fromDoc(MongoRegisteredClient d) {
    try {
      Map<String, Object> cs = (d.clientSettingsJson == null)
          ? Map.of()
          : om.readValue(d.clientSettingsJson, Map.class);

      Map<String, Object> ts = (d.tokenSettingsJson == null)
          ? Map.of()
          : om.readValue(d.tokenSettingsJson, Map.class);

      RegisteredClient.Builder b = RegisteredClient.withId(d.id)
          .clientId(d.clientId);

      if (d.clientSecret != null) b.clientSecret(d.clientSecret);

      if (d.authMethods != null) d.authMethods.forEach(v ->
          b.clientAuthenticationMethod(new ClientAuthenticationMethod(v)));

      if (d.grantTypes != null) d.grantTypes.forEach(v ->
          b.authorizationGrantType(new AuthorizationGrantType(v)));

      if (d.redirectUris != null) d.redirectUris.forEach(b::redirectUri);
      if (d.scopes != null) d.scopes.forEach(b::scope);

      b.clientSettings(ClientSettings.withSettings(cs).build());
      b.tokenSettings(TokenSettings.withSettings(ts).build());

      return b.build();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to read RegisteredClient settings", e);
    }
  }
}
