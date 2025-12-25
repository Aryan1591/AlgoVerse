package com.algoverse.platform.web;

import com.algoverse.platform.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ApiError> handleEmailExists(EmailAlreadyExistsException ex, HttpServletRequest req) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(
        new ApiError(Instant.now(), 409, "CONFLICT", ex.getMessage(), req.getRequestURI())
    );
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ApiError> handleInvalidCreds(InvalidCredentialsException ex, HttpServletRequest req) {
    // Don’t leak whether the email exists.
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
        new ApiError(Instant.now(), 401, "UNAUTHORIZED", "Invalid credentials", req.getRequestURI())
    );
  }
}
