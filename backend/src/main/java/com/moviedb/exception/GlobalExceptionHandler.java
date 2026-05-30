package com.moviedb.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralised exception → HTTP response mapping.
 *
 * WHY @RestControllerAdvice?
 *   Applied globally to all @RestController classes. Controllers stay clean —
 *   no try/catch blocks. All error shapes are consistent and defined here.
 *
 * Response shapes:
 *   400 Bad Request  → { "message": "validation detail" }
 *   401 Unauthorized → { "message": "Bad credentials" }
 *   403 Forbidden    → { "message": "Access denied" }
 *   404 Not Found    → { "message": "resource description" }
 *   409 Conflict     → { "message": "duplicate description" }
 *   500 Server Error → { "message": "Server error" }
 *
 * WHY suppress the stack trace in 500 responses?
 *   Stack traces reveal internal implementation details to potential attackers
 *   (OWASP A05 Security Misconfiguration). Log the full trace server-side;
 *   return only a generic message to the client.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Jakarta Bean Validation failures (@NotBlank, @Size, @Email …) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    /** 404 — entity not found */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(
            ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    /** Duplicate resource (watchlist entry already exists, etc.) */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleConflict(
            IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    /** Business-rule violations */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(
            IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", ex.getMessage()));
    }

    /** Wrong password / username */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(
            BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Invalid username or password"));
    }

    /** JWT present but insufficient role */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(
            AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Access denied"));
    }

    /** Catch-all — log internally, return generic message externally */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception ex) {
        log.error("Unhandled exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Server error"));
    }
}
