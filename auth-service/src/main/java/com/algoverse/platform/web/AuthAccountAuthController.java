package com.algoverse.platform.web;

import com.algoverse.platform.dto.*;
import com.algoverse.platform.service.AuthAccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthAccountAuthController {

  private final AuthAccountService authAccountService;

  public AuthAccountAuthController(AuthAccountService authAccountService) {
    this.authAccountService = authAccountService;
  }

  @PostMapping("/signup")
  public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
    return ResponseEntity.ok(authAccountService.signup(request));
  }

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authAccountService.login(request));
  }

  /**
   * Dev/test token endpoint: email+password -> JWT access token.
   */
  @PostMapping("/token")
  public ResponseEntity<TokenResponse> token(@Valid @RequestBody TokenRequest request) {
    return ResponseEntity.ok(authAccountService.token(request));
  }
}
