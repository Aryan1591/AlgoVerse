package com.algoverse.platform.config;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

@Configuration
public class KeysConfig {

  // ✅ Keep a stable key id for the running instance
  public static final String KEY_ID = UUID.randomUUID().toString();

  @Bean
  public JWKSource<SecurityContext> jwkSource() {
    KeyPair keyPair = generateRsaKey();
    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

    // ✅ IMPORTANT: use=sig and alg=RS256 so encoder can select it
    RSAKey rsaKey = new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyUse(KeyUse.SIGNATURE)
        .algorithm(JWSAlgorithm.PS256)
        .keyID(KEY_ID)
        .build();

    JWKSet jwkSet = new JWKSet(rsaKey);
    return (selector, context) -> selector.select(jwkSet);
  }

  @Bean
  public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
    return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
  }

  @Bean
  public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  public AuthorizationServerSettings authorizationServerSettings(
      @Value("${app.issuer:http://localhost:9000}") String issuer
  ) {
    return AuthorizationServerSettings.builder().issuer(issuer).build();
  }

  private static KeyPair generateRsaKey() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      return generator.generateKeyPair();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
}
