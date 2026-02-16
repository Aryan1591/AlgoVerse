// src/main/java/com/algoverse/monolith/config/JwtCustomizerConfig.java
package com.algoverse.platform.security_config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import com.algoverse.platform.security_jwt.JwkFileStore;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Configuration
public class JwtCustomizerConfig {

  public static final String API_AUDIENCE = "algoverse-api";

  @Bean
  public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer(JwkFileStore store) {
    return (context) -> {
      // ---- Header: PS256 + kid (only for JWTs) ----
      context.getJwsHeader().algorithm(SignatureAlgorithm.PS256);
      context.getJwsHeader().keyId(store.getActivePrivateJwk().getKeyID());

      // ---- Claims common for access tokens ----
      if ("access_token".equals(context.getTokenType().getValue())) {
        Instant now = Instant.now();

        // aud + jti + nbf
        context.getClaims()
            .audience(List.of(API_AUDIENCE))
            .claim("jti", UUID.randomUUID().toString())
            .claim("nbf", now.getEpochSecond());
      }

      // ---- Roles claim (access token is enough; you can keep for id_token too if you want) ----
      List<String> roles = context.getPrincipal().getAuthorities().stream()
          .map(GrantedAuthority::getAuthority)
          .filter(a -> a.startsWith("ROLE_"))
          .map(a -> a.substring("ROLE_".length()))
          .distinct()
          .toList();

      context.getClaims().claim("roles", roles);

      // Subject (usually already set, but safe)
      context.getClaims().subject(context.getPrincipal().getName());
    };
  }
}
