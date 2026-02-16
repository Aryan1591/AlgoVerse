package com.algoverse.platform.web_security;

import com.algoverse.platform.dto_security.*;
import com.algoverse.platform.service_security.AuthAccountService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthController {

  private final AuthAccountService authAccountService;

  public AuthController(AuthAccountService authAccountService) {
    this.authAccountService = authAccountService;
  }

  @PostMapping(value = "/signup", consumes = "application/json")
  public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
    log.info("Request received for SignUp");
    SignupResponse response = authAccountService.signup(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping(value = "/login", consumes = "application/json")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    log.info("Request received for Login");
    LoginResponse response = authAccountService.login(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping(value = "/token", consumes = "application/json")
  public ResponseEntity<TokenResponse> token(@Valid @RequestBody TokenRequest request) {
    log.info("Request received for Token");
    TokenResponse response = authAccountService.token(request);
    return ResponseEntity.ok(response);
  }
}
