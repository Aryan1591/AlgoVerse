package com.algoverse.platform.service_security;

import com.algoverse.platform.dto_security.*;
import com.algoverse.platform.entity_security.AuthAccount;
import com.algoverse.platform.entity_security.Role;
import com.algoverse.platform.entity_security.UserDetails;
import com.algoverse.platform.exception_security.EmailAlreadyExistsException;
import com.algoverse.platform.exception_security.InvalidCredentialsException;
import com.algoverse.platform.repository_security.AuthAccountRepository;
import com.algoverse.platform.repository_security.UserDetailsRepository;
import com.algoverse.platform.security_jwt.JwkFileStore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthAccountServiceImpl implements AuthAccountService {

  private final AuthAccountRepository repo;
  private final PasswordEncoder passwordEncoder;
  private final JwtEncoder jwtEncoder;
  private final String issuer;

  private final UserDetailsRepository userDetailsRepository;
  private final JwkFileStore keyStore;

  public AuthAccountServiceImpl(
      AuthAccountRepository repo,
      UserDetailsRepository userDetailsRepository,
      PasswordEncoder passwordEncoder,
      JwtEncoder jwtEncoder,
      JwkFileStore keyStore,
      @Value("${app.issuer:http://localhost:9001}") String issuer
  ) {
    this.userDetailsRepository = userDetailsRepository;
    this.repo = repo;
    this.passwordEncoder = passwordEncoder;
    this.jwtEncoder = jwtEncoder;
    this.keyStore = keyStore;
    this.issuer = issuer;
  }

  @Override
  public SignupResponse signup(SignupRequest request) {
    String email = normalizeEmail(request.email());

    if (repo.existsByEmail(email)) {
      throw new EmailAlreadyExistsException(email);
    }

    // ---- AuthAccount ----
    AuthAccount acc = new AuthAccount();
    acc.setEmail(email);
    acc.setPasswordHash(passwordEncoder.encode(request.password()));
    acc.getRoles().add(Role.USER);
    acc.setCreatedAt(Instant.now());
    acc.setUpdatedAt(Instant.now());

    AuthAccount saved = repo.save(acc);

    // ---- UserDetails ----
    UserDetails userDetails = new UserDetails();
    userDetails.setEmail(email);

    // NOTE: store hash only (never store raw). If UserDetails has a password field, store hash here too.
    userDetails.setPassword(passwordEncoder.encode(request.password()));

    userDetails.setRoles(Set.of(Role.USER));
    userDetails.setUsername(request.username());
    userDetails.setPhoneNumber(request.phoneNumber());
    userDetails.setEducationQualification(request.educationQualification());
    userDetails.setCollegeName(request.collegeName());      // ✅ ensure method name matches your DTO
userDetails.setSocialLinks(
    request.socialLinks() == null ? List.of() : request.socialLinks()
);

    userDetails.setCreatedAt(Instant.now());
    userDetails.setUpdatedAt(Instant.now());

    userDetailsRepository.save(userDetails);

    return new SignupResponse(
        saved.getId(),
        saved.getEmail(),
        toRoleNames(saved.getRoles()),
        saved.getCreatedAt()
    );
  }

  @Override
  public LoginResponse login(LoginRequest request) {
    String email = normalizeEmail(request.email());

    AuthAccount acc = repo.findByEmail(email)
        .orElseThrow(InvalidCredentialsException::new);

    if (!passwordEncoder.matches(request.password(), acc.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }

    return new LoginResponse(
        acc.getEmail(),
        toRoleNames(acc.getRoles()),
        "Login verified"
    );
  }

  /**
   * Dev/test shortcut: mint a JWT after verifying credentials.
   * PS256 + kid from active JWK.
   */
  @Override
  public TokenResponse token(TokenRequest request) {

    String email = normalizeEmail(request.email());

    AuthAccount acc = repo.findByEmail(email)
        .orElseThrow(InvalidCredentialsException::new);

    if (!passwordEncoder.matches(request.password(), acc.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }

    String scope = (request.scope() == null || request.scope().isBlank())
        ? "read"
        : request.scope().trim().replaceAll("\\s+", " ");

    var roles = acc.getRoles().stream().map(Enum::name).toList();

    Instant now = Instant.now();
    long expiresInSeconds = 3600;
    Instant exp = now.plusSeconds(expiresInSeconds);

   JwtClaimsSet claims = JwtClaimsSet.builder()
    .issuer(issuer)
    .issuedAt(now)
    .expiresAt(exp)
    .subject(email)
    .audience(List.of("algoverse-api"))
    .claim("jti", UUID.randomUUID().toString())
    .claim("nbf", now.getEpochSecond())
    .claim("email", email)
    .claim("roles", roles)
    .claim("scope", scope)
    .build();

    String activeKid = keyStore.getActivePrivateJwk().getKeyID();

    JwsHeader headers = JwsHeader.with(SignatureAlgorithm.PS256)
        .type("JWT")
        .keyId(activeKid)
        .build();

    String tokenValue = jwtEncoder
        .encode(JwtEncoderParameters.from(headers, claims))
        .getTokenValue();

    return new TokenResponse(tokenValue, "Bearer", expiresInSeconds, scope);
  }

  private static String normalizeEmail(String email) {
    return email == null ? "" : email.trim().toLowerCase();
  }

  private static Set<String> toRoleNames(Set<Role> roles) {
    return roles.stream().map(Enum::name).collect(Collectors.toSet());
  }
}
