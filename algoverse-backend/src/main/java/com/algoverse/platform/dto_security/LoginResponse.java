package com.algoverse.platform.dto_security;

import java.util.Set;

public record LoginResponse(
    String email,
    Set<String> roles,
    String message
) {}
