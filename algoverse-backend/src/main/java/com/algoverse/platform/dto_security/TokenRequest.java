package com.algoverse.platform.dto_security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@Email @NotBlank String email, @NotBlank String password, String scope) {}
