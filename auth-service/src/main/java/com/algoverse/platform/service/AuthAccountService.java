package com.algoverse.platform.service;

import com.algoverse.platform.dto.*;

public interface AuthAccountService {
  SignupResponse signup(SignupRequest request);
  LoginResponse login(LoginRequest request);
  TokenResponse token(TokenRequest request); // dev/test token shortcut
}
