package com.algoverse.platform.security;

import com.algoverse.platform.repository.AuthAccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class MongoUserDetailsService implements UserDetailsService {

  private final AuthAccountRepository repo;

  public MongoUserDetailsService(AuthAccountRepository repo) {
    this.repo = repo;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    var acc = repo.findByEmail(email.trim().toLowerCase())
        .orElseThrow(() -> new UsernameNotFoundException("No user: " + email));

    var authorities = acc.getRoles().stream()
        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.name()))
        .toList();

    return User.withUsername(acc.getEmail())
        .password(acc.getPassword())
        .authorities(authorities)
        .build();
  }
}
