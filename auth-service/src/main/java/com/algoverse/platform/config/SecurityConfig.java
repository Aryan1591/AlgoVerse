package com.algoverse.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;

import com.algoverse.platform.security.CsrfCookieFilter;

import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  @Order(1)
  SecurityFilterChain authorizationServerChain(HttpSecurity http) throws Exception {
    OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);



    http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
        .oidc(Customizer.withDefaults()); // enables discovery/userinfo/id_token

    return http.build();
  }

// @Bean
// @Order(2)
// SecurityFilterChain appChain(HttpSecurity http) throws Exception {
//   http
//     // ✅ REST endpoints for Postman
//     .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**"))
//     .authorizeHttpRequests(auth -> auth
//       .requestMatchers("/auth/signup", "/auth/login", "/auth/token", "/error").permitAll()
//       .anyRequest().authenticated()
//     )
//     .formLogin(Customizer.withDefaults());

//   return http.build();
// }
@Bean
@Order(2)
SecurityFilterChain appChain(HttpSecurity http) throws Exception {
  http
    // CSRF enabled for browser flows
    .csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .ignoringRequestMatchers("/auth/**") // REST endpoints
    )
    .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/auth/**", "/error").permitAll()
        .anyRequest().authenticated()
    )
    .formLogin(Customizer.withDefaults());

  return http.build();
}



  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
