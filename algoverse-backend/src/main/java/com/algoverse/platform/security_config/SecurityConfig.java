// src/main/java/com/algoverse/monolith/config/SecurityConfig.java
package com.algoverse.platform.security_config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  // @Bean
  // public PasswordEncoder passwordEncoder() {
  //   return new BCryptPasswordEncoder();
  // }

  /**
   * Resource Server: map JWT roles/scopes -> Spring Security authorities.
   * - roles: ["ADMIN"] -> ROLE_ADMIN
   * - scope: "read write" or scp: ["read"] -> SCOPE_read / SCOPE_write
   */
  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
    conv.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {

      List<GrantedAuthority> authorities = new ArrayList<>();

      // roles -> ROLE_*
      List<String> roles = jwt.getClaimAsStringList("roles");
      if (roles == null) roles = List.of();
      authorities.addAll(
          roles.stream()
              .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
              .map(SimpleGrantedAuthority::new)
              .toList()
      );

      // scope/scp -> SCOPE_*
      String scope = jwt.getClaimAsString("scope");
      List<String> scp = jwt.getClaimAsStringList("scp");

      List<String> scopes = new ArrayList<>();
      if (scope != null && !scope.isBlank()) scopes.addAll(List.of(scope.split("\\s+")));
      if (scp != null) scopes.addAll(scp);

      authorities.addAll(
          scopes.stream()
              .filter(s -> s != null && !s.isBlank())
              .distinct()
              .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
              .toList()
      );

      return authorities;
    });
    return conv;
  }

  /**
   * 1) Authorization Server endpoints (/oauth2/**, /.well-known/** for OIDC, etc.)
   */
  @Bean
  @Order(1)
  public SecurityFilterChain authorizationServerChain(HttpSecurity http) throws Exception {
       OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
            new OAuth2AuthorizationServerConfigurer();

    http
        .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
        .with(authorizationServerConfigurer, Customizer.withDefaults());

    authorizationServerConfigurer
        .oidc(Customizer.withDefaults()); // enable OpenID Connect

    http
      .exceptionHandling(ex -> ex
          .authenticationEntryPoint(
              new org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint("/login")
          )
      );
    return http.build();
  }

  /**
   * 2) Admin endpoints: JWT ONLY (no form login / no basic).
   * CSRF disabled because it's bearer-token protected (not cookie-auth).
   */
  @Bean
  @Order(2)
  public SecurityFilterChain adminChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthConverter) throws Exception {
    http.securityMatcher("/admin/**")
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("ADMIN"))
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter)))
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable());

    return http.build();
  }

  /**
   * 3) App/API endpoints
   *
   * You said:
   * - Called from React ✅
   * - Using formLogin ✅  (=> session cookie auth => CSRF should be ON for browser requests)
   * - JWT not stored in localStorage/cookie ✅ (so API calls will mainly use session, or bearer token when you use /auth/token)
   *
   * Strategy:
   * - Enable CSRF using CookieCsrfTokenRepository so React can read XSRF-TOKEN cookie and send it back.
   * - Ignore CSRF for /auth/** (signup/login/token) because these are typically called before you have a CSRF token
   *   OR you can keep CSRF enabled and have React first call a GET to fetch token cookie.
   */
// src/main/java/com/algoverse/monolith/config/SecurityConfig.java
@Bean
@Order(3)
public SecurityFilterChain appAndApiChain(HttpSecurity http,
                                         JwtAuthenticationConverter jwtAuthConverter) throws Exception {
  http
    .cors(Customizer.withDefaults())
    .csrf(csrf -> csrf
      .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
      .ignoringRequestMatchers("/auth/**", "/oauth2/**", "/.well-known/**")
    )
    .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)  // ✅ IMPORTANT
    .authorizeHttpRequests(auth -> auth
      .requestMatchers("/auth/**", "/public", "/error", "/.well-known/jwks.json").permitAll()
      .requestMatchers("/api/**").authenticated()
      .anyRequest().denyAll()
    )
    .formLogin(Customizer.withDefaults())
    .oauth2ResourceServer(oauth2 -> oauth2
      .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
    )
    .headers(headers -> headers
      .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true))
      // add CSP if you serve HTML from Spring; otherwise keep minimal
    )
    .sessionManagement(sm -> sm
      .sessionFixation(sf -> sf.migrateSession())
    );

  return http.build();
}



}
