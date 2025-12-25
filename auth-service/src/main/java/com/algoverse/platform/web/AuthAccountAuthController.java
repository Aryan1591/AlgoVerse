package com.algoverse.platform.web;

import com.algoverse.platform.dto.*;
import com.algoverse.platform.exception.EmailAlreadyExistsException;
import com.algoverse.platform.exception.InvalidCredentialsException;
import com.algoverse.platform.service.AuthAccountService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/auth")
public class AuthAccountAuthController {

  private final AuthAccountService authAccountService;

  public AuthAccountAuthController(AuthAccountService authAccountService) {
    this.authAccountService = authAccountService;
  }

  @PostMapping("/signup")
  public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
    log.info("Request received for SignUp");
    try {
      SignupResponse response = authAccountService.signup(request);
      return new ResponseEntity<>(response, HttpStatus.CREATED);

    } catch (EmailAlreadyExistsException e) {
      log.warn("Signup failed: email already exists", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    log.info("Request received for Login");
    try {
      LoginResponse response = authAccountService.login(request);
      return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (InvalidCredentialsException e) {
      log.warn("Login failed: invalid credentials", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
      // If you prefer more standard semantics:
      // return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
  }

  /**
   * Dev/test token endpoint: email+password -> JWT access token.
   */
  @PostMapping("/token")
  public ResponseEntity<?> token(@Valid @RequestBody TokenRequest request) {
    log.info("Request received for Token");
    try {
      TokenResponse response = authAccountService.token(request);
      return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (InvalidCredentialsException e) {
      log.warn("Token generation failed: invalid credentials", e);
      return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
      // or HttpStatus.UNAUTHORIZED
    }
  }
}
