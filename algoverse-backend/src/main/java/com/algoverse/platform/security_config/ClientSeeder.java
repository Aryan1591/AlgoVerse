// src/main/java/com/algoverse/monolith/config/ClientSeeder.java
package com.algoverse.platform.security_config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.*;
import org.springframework.security.oauth2.server.authorization.settings.*;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class ClientSeeder {

  @Bean
  ApplicationRunner seedClients(RegisteredClientRepository clients) {
    return args -> {
      if (clients.findByClientId("public-client") == null) {
        TokenSettings tokenSettings = TokenSettings.builder()
            .accessTokenTimeToLive(Duration.ofMinutes(15))
            .refreshTokenTimeToLive(Duration.ofDays(30))
            .reuseRefreshTokens(false)
            .build();

        RegisteredClient publicClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("public-client")
            .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri("http://127.0.0.1:8081/callback")
            .scope(OidcScopes.OPENID)
            .scope("read")
            .tokenSettings(tokenSettings)
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(true)
                .requireProofKey(true)
                .build())
            .build();

        clients.save(publicClient);
      }

      if (clients.findByClientId("service-client") == null) {
        RegisteredClient serviceClient = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId("service-client")
            .clientSecret("{bcrypt}$2a$10$REPLACE_WITH_BCRYPT_HASH") // ✅ not {noop} in prod
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("read")
            .scope("write")
            .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofMinutes(15)).build())
            .build();

        clients.save(serviceClient);
      }
    };
  }
}
