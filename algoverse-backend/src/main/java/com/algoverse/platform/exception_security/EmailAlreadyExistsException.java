package com.algoverse.platform.exception_security;

public class EmailAlreadyExistsException extends RuntimeException {
  public EmailAlreadyExistsException(String email) { super("Email already exists: " + email); }
}
