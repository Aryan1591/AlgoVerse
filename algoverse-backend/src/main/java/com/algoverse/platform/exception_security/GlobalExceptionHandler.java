package com.algoverse.platform.exception_security;

import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.algoverse.platform.dto.ErrorResponse;
import com.algoverse.platform.exception.BatchNotFoundException;
import com.algoverse.platform.exception.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  // ✅ JSON parse/bind errors
  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request
  ) {
    ApiError err = new ApiError(
        LocalDateTime.now(),
        List.of("Invalid JSON request: " + ex.getMostSpecificCause().getMessage()),
        request.getDescription(false)
    );
    return ResponseEntity.badRequest().body(err);
  }

  // ✅ Validation errors (@Valid)
     @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String errorMessage = error.getDefaultMessage();
            errors.add(errorMessage);
        });
        ApiError loginerror = new ApiError(LocalDateTime.now(), errors, request.getDescription(false));


        return new ResponseEntity<>(loginerror, HttpStatus.BAD_REQUEST);
    }

  // ✅ Your custom exceptions
  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException ex, WebRequest request) {
    ApiError err = new ApiError(LocalDateTime.now(), List.of(ex.getMessage()), request.getDescription(false));
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
  }

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ApiError> handleEmailExists(EmailAlreadyExistsException ex, WebRequest request) {
    ApiError err = new ApiError(LocalDateTime.now(), List.of(ex.getMessage()), request.getDescription(false));
    return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
  }

  // ✅ fallback
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleAny(Exception ex, WebRequest request) {
    ApiError err = new ApiError(LocalDateTime.now(), List.of("Server error: " + ex.getMessage()), request.getDescription(false));
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
  }
      
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(BatchNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBatchNotFound(BatchNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            ex.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
}
