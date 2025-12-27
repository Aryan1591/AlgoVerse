package com.algoverse.platform.service;

import com.algoverse.platform.dto.*;
import com.algoverse.platform.entity.AuthAccount;
import com.algoverse.platform.entity.Role;
import com.algoverse.platform.entity.UserDetails;
import com.algoverse.platform.exception.EmailAlreadyExistsException;
import com.algoverse.platform.exception.InvalidCredentialsException;
import com.algoverse.platform.repository.AuthAccountRepository;
import com.algoverse.platform.repository.UserDetailsRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthAccountServiceImpl implements AuthAccountService {

  private final AuthAccountRepository repo;
  private final PasswordEncoder passwordEncoder;
  private final JwtEncoder jwtEncoder;
  private final String issuer;
  private final UserDetailsRepository userDetailsRepository;

  public AuthAccountServiceImpl(
      AuthAccountRepository repo,
      UserDetailsRepository userDetailsRepository,
      PasswordEncoder passwordEncoder,
      JwtEncoder jwtEncoder,
      @Value("${app.issuer:http://localhost:9002}") String issuer
  ) {
    this.userDetailsRepository = userDetailsRepository;
    this.repo = repo;
    this.passwordEncoder = passwordEncoder;
    this.jwtEncoder = jwtEncoder;
    this.issuer = issuer;
  }

  @Override
  public SignupResponse signup(SignupRequest request) 
  {
    String email = normalizeEmail(request.email());

    if (repo.existsByEmail(email)) {
      throw new EmailAlreadyExistsException(email);
    }

    AuthAccount acc = new AuthAccount();
    acc.setEmail(email);
    acc.setPassword(passwordEncoder.encode(request.password())); // BCrypt hash
    acc.getRoles().add(Role.USER);
    acc.setCreatedAt(Instant.now());
    acc.setUpdatedAt(Instant.now());

    AuthAccount saved = repo.save(acc);

    UserDetails userDetails = new UserDetails();
    userDetails.setEmail(email);
    userDetails.setPassword(passwordEncoder.encode(request.password()));
    userDetails.setRoles(Set.of(Role.USER));
    userDetails.setUsername(request.username());
    userDetails.setPhoneNumber(request.phoneNumber());
    userDetails.setEducationQualification(request.educationQualification());
    userDetails.setCollegeName(request.CollegeName());
    userDetails.setSocialLinks(request.socialLinks());
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
  public LoginResponse login(LoginRequest request) 
  {
    String email = normalizeEmail(request.email());

    AuthAccount acc = repo.findByEmail(email)
        .orElseThrow(InvalidCredentialsException::new);

    if (!passwordEncoder.matches(request.password(), acc.getPassword())) {
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
   * This token will be accepted by api-service Resource Server if issuer+keys match.
   */
 @Override
public TokenResponse token(TokenRequest request) {

  String email = normalizeEmail(request.email());

  AuthAccount acc = repo.findByEmail(email)
      .orElseThrow(InvalidCredentialsException::new);

  if (!passwordEncoder.matches(request.password(), acc.getPassword())) {
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
      .claim("email", email)
      .claim("roles", roles)
      .claim("scope", scope)
      .build();

  JwsHeader headers = JwsHeader.with(SignatureAlgorithm.PS256).type("JWT").build();

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
