package com.tictactore.exception;

import com.tictactore.exception.ApiError;
import com.tictactore.exception.RateLimitExceededException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimitExceeded(RateLimitExceededException e) {
        if (e.isRedisFailure()) {
            return ResponseEntity.status(503).body(new ApiError(
                    "RATE_LIMIT_UNAVAILABLE",
                    e.getMessage() != null ? e.getMessage() : "Rate limit service unavailable",
                    Map.of("retryAfter", e.getRetryAfterSeconds())
            ));
        }
        return ResponseEntity.status(429).body(new ApiError(
                "RATE_LIMIT_EXCEEDED",
                e.getMessage() != null ? e.getMessage() : "Rate limit exceeded",
                Map.of("retryAfter", e.getRetryAfterSeconds())
        ));
    }

    @ExceptionHandler({DuplicatePlayerException.class, InvalidMatchScoreException.class, InvalidPositionException.class, DuplicatePositionException.class, InvalidMatchStateException.class})
    public ResponseEntity<Map<String, String>> handleDomainValidation(RuntimeException e) {
        var msg = e.getMessage() != null ? e.getMessage() : "Invalid match data";
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }

    @ExceptionHandler(UnauthorizedMatchActionException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedMatchAction(UnauthorizedMatchActionException e) {
        var msg = e.getMessage() != null ? e.getMessage() : "Unauthorized match action";
        return ResponseEntity.status(403).body(Map.of("message", msg));
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(org.springframework.security.access.AccessDeniedException e) {
        var msg = e.getMessage() != null ? e.getMessage() : "Access denied";
        return ResponseEntity.status(403).body(Map.of("message", msg));
    }

    @ExceptionHandler(ParticipantNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleParticipantNotFound(ParticipantNotFoundException e) {
        var msg = e.getMessage() != null ? e.getMessage() : "Participant not found";
        return ResponseEntity.status(404).body(Map.of("message", msg));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({ValidationException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleValidationException(RuntimeException e) {
        var msg = e.getMessage() != null ? e.getMessage() : "Invalid input";
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        var error = e.getBindingResult().getFieldError();
        var msg = error != null ? error.getDefaultMessage() : "Validation error";
        return ResponseEntity.badRequest().body(Map.of("message", msg != null ? msg : "Validation error"));
    }

    @ExceptionHandler({
            org.springframework.dao.OptimisticLockingFailureException.class,
            jakarta.persistence.OptimisticLockException.class,
            org.hibernate.StaleObjectStateException.class,
            org.springframework.dao.DataIntegrityViolationException.class
    })
    public ResponseEntity<Map<String, String>> handleOptimisticLocking(Exception e) {
        return ResponseEntity.status(409).body(Map.of("message", "The record has been modified by another transaction"));
    }

    @ExceptionHandler({PoolConflictException.class, ChallengeConflictException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handlePoolConflict(RuntimeException e) {
        var msg = e.getMessage() != null ? e.getMessage() : "Conflict";
        return ResponseEntity.status(409).body(Map.of("message", msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException e) {
        var message = e.getConstraintViolations().stream()
                .findFirst()
                .map(v -> v.getMessage() != null ? v.getMessage() : v.toString())
                .orElse("Validation error");
        return ResponseEntity.badRequest().body(new ApiError("BAD_REQUEST", message, Map.of()));
    }
}
