package com.automarket.marketplace.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> formatError(String code, String message, String path) {
        return Map.of("error", Map.of(
            "code", code,
            "message", message,
            "timestamp", Instant.now().toString(),
            "path", path
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            formatError("RESOURCE_NOT_FOUND", ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            formatError("UNAUTHORIZED", ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            formatError("FORBIDDEN", ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(com.automarket.marketplace.auth.AuthException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(com.automarket.marketplace.auth.AuthException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatus()).body(
            formatError("AUTH_ERROR", ex.getMessage(), request.getRequestURI())
        );
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(org.springframework.web.bind.MethodArgumentNotValidException ex, HttpServletRequest request) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
            .map(f -> f.getField() + " " + f.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");
            
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            formatError("VALIDATION_ERROR", msg, request.getRequestURI())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            formatError("INTERNAL_SERVER_ERROR", "An unexpected error occurred", request.getRequestURI())
        );
    }
}

