package com.algoverse.platform.security_jwt;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.algoverse.platform.repository_security.AuthAccountRepository;

@Service
public class RepoUserDetailsService implements UserDetailsService {

  private final AuthAccountRepository repo;
  private final String adminPasswordHash;

  public RepoUserDetailsService(AuthAccountRepository repo, PasswordEncoder encoder) {
    this.repo = repo;
    // cache admin password hash
    this.adminPasswordHash = encoder.encode("admin123");
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    String u = username == null ? "" : username.trim().toLowerCase();

    if ("admin".equals(u)) {
      return User.withUsername("admin")
          .password(adminPasswordHash)
          .roles("ADMIN")
          .build();
    }

    var acc = repo.findByEmail(u).orElseThrow(() -> new UsernameNotFoundException("No user: " + u));

    var authorities = acc.getRoles().stream()
        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
        .toList();

    return User.withUsername(acc.getEmail())
        .password(acc.getPasswordHash())
        .authorities(authorities)
        .build();
  }
}
