package com.algoverse.platform.dto;

import java.util.Set;

public record LoginResponse(
    String email,
    Set<String> roles,
    String message
) {}
