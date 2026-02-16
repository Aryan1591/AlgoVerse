
package com.algoverse.platform.service_security;

import com.algoverse.platform.entity_security.MongoOAuth2Consent;
import com.algoverse.platform.repository_security.MongoConsentRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.stereotype.Component;

@Component
public class MongoOAuth2ConsentService implements OAuth2AuthorizationConsentService {

  private final MongoConsentRepo repo;
  private final ObjectMapper om;

  public MongoOAuth2ConsentService(MongoConsentRepo repo,
                                  @Qualifier("sasObjectMapper") ObjectMapper sasObjectMapper) {

    this.repo = repo;
    this.om = sasObjectMapper;
  }

  @Override
  public void save(OAuth2AuthorizationConsent consent) {
    try {
      MongoOAuth2Consent doc = new MongoOAuth2Consent();
      doc.id = consent.getRegisteredClientId() + ":" + consent.getPrincipalName();
      doc.registeredClientId = consent.getRegisteredClientId();
      doc.principalName = consent.getPrincipalName();
      doc.dataJson = om.writeValueAsString(consent);
      repo.save(doc);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to save consent", e);
    }
  }

  @Override
  public void remove(OAuth2AuthorizationConsent consent) {

    repo.deleteByRegisteredClientIdAndPrincipalName(
        consent.getRegisteredClientId(), consent.getPrincipalName());
  }

  @Override
  public OAuth2AuthorizationConsent findById(String registeredClientId, String principalName) {
    return repo.findByRegisteredClientIdAndPrincipalName(registeredClientId, principalName)
        .map(this::toConsent)
        .orElse(null);
  }

  private OAuth2AuthorizationConsent toConsent(MongoOAuth2Consent doc) {
    try {
      return om.readValue(doc.dataJson, OAuth2AuthorizationConsent.class);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse consent JSON", e);
    }
  }
}
