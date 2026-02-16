package com.algoverse.platform.dto_security;

import java.time.Instant;
import java.util.Set;

public record SignupResponse(
    String id,
    String email,
    Set<String> roles,
    Instant createdAt
) {}
