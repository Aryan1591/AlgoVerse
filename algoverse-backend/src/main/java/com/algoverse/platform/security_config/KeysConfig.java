// src/main/java/com/algoverse/monolith/config/KeysConfig.java
package com.algoverse.platform.security_config;

import com.algoverse.platform.security_jwt.JwkFileStore;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

@Configuration
public class KeysConfig {

  @Bean
  public JwkFileStore jwkFileStore(
      @Value("${app.keys.dir:./keys}") String keysDir,
      @Value("${app.keys.max-old:2}") int maxOldKeys,
      @Value("${app.keys.rotate-on-start:false}") boolean rotateOnStart
  ) {
    return new JwkFileStore(keysDir, maxOldKeys, rotateOnStart);
  }

  /**
   * ✅ Dynamic: always reads latest keys (supports runtime rotation)
   */
  @Bean
  public JWKSource<SecurityContext> jwkSource(JwkFileStore store) {
    return (selector, context) -> selector.select(new JWKSet(store.getAllPublicJwks()));
  }

  /**
   * ✅ Dynamic: always reads latest private keys (supports runtime rotation)
   */
  @Bean
  public JwtEncoder jwtEncoder(JwkFileStore store) {
    JWKSource<SecurityContext> privateSource =
        (selector, context) -> selector.select(new JWKSet(store.getAllPrivateJwks()));
    return new NimbusJwtEncoder(privateSource);
  }

  /**
   * Used by both Authorization Server and Resource Server inside same app.
   */
// src/main/java/com/algoverse/monolith/config/KeysConfig.java
@Bean
public JwtDecoder jwtDecoder(
    JWKSource<SecurityContext> jwkSource,
    @Value("${app.issuer:http://localhost:9000}") String issuer
) {
  var decoder = OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);

  OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);

  OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
      jwt.getAudience() != null && jwt.getAudience().contains(JwtCustomizerConfig.API_AUDIENCE)
          ? OAuth2TokenValidatorResult.success()
          : OAuth2TokenValidatorResult.failure(
              new OAuth2Error("invalid_token", "Missing/invalid aud", null)
          );

  ((NimbusJwtDecoder) decoder).setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator));
  return decoder;
}


  @Bean
  public AuthorizationServerSettings authorizationServerSettings(
      @Value("${app.issuer:http://localhost:9000}") String issuer
  ) {
    return AuthorizationServerSettings.builder().issuer(issuer).build();
  }
}
