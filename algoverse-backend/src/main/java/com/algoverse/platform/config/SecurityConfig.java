package com.algoverse.platform.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;



@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
    http
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/public/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/**").authenticated()
        .anyRequest().denyAll()
      )
      .oauth2ResourceServer(oauth2 -> oauth2
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())) // ✅ use method, not @Bean
      );

    return http.build();
  }

  // ✅ NOT a @Bean (so MVC conversion service won't try to introspect it)
  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthConverter() {
    return jwt -> {
      Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

      String scope = jwt.getClaimAsString("scope");
      if (scope != null && !scope.isBlank()) {
        Arrays.stream(scope.split("\\s+"))
          .filter(s -> !s.isBlank())
          .forEach(s -> authorities.add(new SimpleGrantedAuthority("SCOPE_" + s)));
      }

      List<String> roles = jwt.getClaim("roles");
      if (roles != null) {
        roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
      }

      return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    };
  }
}

