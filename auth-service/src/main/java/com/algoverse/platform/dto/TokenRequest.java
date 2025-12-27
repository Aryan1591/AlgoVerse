package com.algoverse.platform.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Dev/test shortcut: email+password -> signed JWT.
 * Not standard OAuth2. Use Authorization Code + PKCE in production.
 */
public record TokenRequest(
    @Email @NotBlank String email,
    @NotBlank String password,
    String scope // optional: "read write"
) {}
