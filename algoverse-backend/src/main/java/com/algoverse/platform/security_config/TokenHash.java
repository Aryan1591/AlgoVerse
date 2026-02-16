// src/main/java/com/algoverse/monolith/oauth/mongo/TokenHash.java
package com.algoverse.platform.security_config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class TokenHash {
  private TokenHash() {}

  public static String sha256(String raw) {
    if (raw == null) return null;
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to hash token", e);
    }
  }
}
