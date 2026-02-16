// src/main/java/com/algoverse/monolith/security/JwkFileStore.java
// (Use your existing implementation as-is; keeping it here as a placeholder file name.)
package com.algoverse.platform.security_jwt;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class JwkFileStore {

  private final Path dir;
  private final int maxOld;
  private final boolean rotateOnStart;

  private RSAKey activePrivate;
  private final List<RSAKey> oldPrivate = new ArrayList<>();

  public JwkFileStore(String keysDir, int maxOldKeys, boolean rotateOnStart) {
    this.dir = Paths.get(keysDir).toAbsolutePath().normalize();
    this.maxOld = Math.max(0, maxOldKeys);
    this.rotateOnStart = rotateOnStart;

    try {
      Files.createDirectories(dir);
      loadOrCreate();
      if (this.rotateOnStart) {
        rotate();
      }
      cleanupOld();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to initialize key store at " + dir, e);
    }
  }

  public synchronized void rotate() {
    try {
      if (activePrivate != null) {
        String ts = String.valueOf(Instant.now().toEpochMilli());
        String kid = activePrivate.getKeyID();
        Path oldPath = dir.resolve("old-" + ts + "-" + kid + ".json");
        Files.writeString(oldPath, activePrivate.toJSONString(), StandardCharsets.UTF_8,
            StandardOpenOption.CREATE_NEW);
        oldPrivate.add(activePrivate);
      }

      activePrivate = generateRsaKey();
      Files.writeString(dir.resolve("active.json"), activePrivate.toJSONString(), StandardCharsets.UTF_8,
          StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

      cleanupOld();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to rotate keys", e);
    }
  }

  public synchronized RSAKey getActivePrivateJwk() {
    return activePrivate;
  }

  public synchronized List<JWK> getAllPrivateJwks() {
    List<JWK> all = new ArrayList<>();
    all.add(activePrivate);
    all.addAll(oldPrivate);
    return all;
  }

  public synchronized List<JWK> getAllPublicJwks() {
    return getAllPrivateJwks().stream()
        .map(jwk -> ((RSAKey) jwk).toPublicJWK())
        .collect(Collectors.toList());
  }

  private void loadOrCreate() throws IOException {
    Path activePath = dir.resolve("active.json");
    if (Files.exists(activePath)) {
      try {
        activePrivate = RSAKey.parse(Files.readString(activePath, StandardCharsets.UTF_8));
      } catch (Exception ex) {
        throw new IllegalStateException("Cannot parse active.json JWK", ex);
      }
    } else {
      activePrivate = generateRsaKey();
      Files.writeString(activePath, activePrivate.toJSONString(), StandardCharsets.UTF_8,
          StandardOpenOption.CREATE_NEW);
    }

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "old-*.json")) {
      for (Path p : stream) {
        try {
          oldPrivate.add(RSAKey.parse(Files.readString(p, StandardCharsets.UTF_8)));
        } catch (Exception ignored) { }
      }
    }
  }

  private void cleanupOld() throws IOException {
    List<Path> oldFiles = new ArrayList<>();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "old-*.json")) {
      for (Path p : stream) oldFiles.add(p);
    }
    oldFiles.sort((a, b) -> {
      try {
        return Files.getLastModifiedTime(b).compareTo(Files.getLastModifiedTime(a));
      } catch (IOException e) {
        return 0;
      }
    });

    for (int i = maxOld; i < oldFiles.size(); i++) {
      Files.deleteIfExists(oldFiles.get(i));
    }

    oldPrivate.clear();
    for (int i = 0; i < Math.min(maxOld, oldFiles.size()); i++) {
      try {
        oldPrivate.add(RSAKey.parse(Files.readString(oldFiles.get(i))));
      } catch (Exception ignored) { }
    }
  }

  private static RSAKey generateRsaKey() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      KeyPair kp = generator.generateKeyPair();

      RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
      RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();

      return new RSAKey.Builder(pub)
          .privateKey(priv)
          .keyUse(KeyUse.SIGNATURE)
          .algorithm(JWSAlgorithm.PS256)
          .keyID(UUID.randomUUID().toString())
          .build();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }
}
