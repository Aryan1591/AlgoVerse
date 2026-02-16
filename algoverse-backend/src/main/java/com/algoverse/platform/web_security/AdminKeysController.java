// src/main/java/com/algoverse/monolith/web/AdminKeysController.java
package com.algoverse.platform.web_security;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.algoverse.platform.security_jwt.JwkFileStore;

import java.util.Map;

@RestController
@RequestMapping("/admin/keys")
public class AdminKeysController {

  private final JwkFileStore store;

  public AdminKeysController(JwkFileStore store) {
    this.store = store;
  }

  /**
   * ✅ Username comes from JWT, not from basic auth.
   * Requires ROLE_ADMIN (enforced by SecurityConfig adminChain).
   */
  @PostMapping("/rotate")
  public ResponseEntity<?> rotate(@AuthenticationPrincipal Jwt jwt) {
    String username = jwt.getSubject(); // or jwt.getClaimAsString("email")

    store.rotate();

    return ResponseEntity.ok(Map.of(
        "rotatedBy", username,
        "activeKid", store.getActivePrivateJwk().getKeyID()
    ));
  }
}
