package vn.vinaacademy.security.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.vinaacademy.security.exception.AccessDeniedException;
import vn.vinaacademy.security.exception.AuthenticationException;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Global exception handler for security-related exceptions
 */
@Slf4j
@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = Map.of(
            "error", "Authentication Required",
            "message", ex.getMessage(),
            "status", HttpStatus.UNAUTHORIZED.value(),
            "timestamp", LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        
        Map<String, Object> errorResponse = Map.of(
            "error", "Access Denied",
            "message", ex.getMessage(),
            "status", HttpStatus.FORBIDDEN.value(),
            "timestamp", LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
}
