package com.algoverse.platform.web_security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ApiController {

  @GetMapping("/public")
  public String pub() {
    return "public ok";
  }

  @GetMapping("/api/me")
  public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
    return Map.of(
        "sub", jwt.getSubject(),
        "email", jwt.getClaimAsString("email"),
        "roles", jwt.getClaim("roles"),
        "scope", jwt.getClaimAsString("scope"),
        "issuer", jwt.getIssuer() != null ? jwt.getIssuer().toString() : null
    );
  }
}
