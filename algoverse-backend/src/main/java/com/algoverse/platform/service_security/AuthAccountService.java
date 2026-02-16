package com.algoverse.platform.service_security;

import com.algoverse.platform.dto_security.*;

public interface AuthAccountService {
  SignupResponse signup(SignupRequest request);
  LoginResponse login(LoginRequest request);
  TokenResponse token(TokenRequest request);
}
