package com.tictactore.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DuplicatePlayerException.class, InvalidMatchScoreException.class, InvalidPositionException.class, DuplicatePositionException.class})
    public ResponseEntity<Map<String, String>> handleDomainValidation(RuntimeException e) {
        var msg = e.getMessage() != null ? e.getMessage() : "Invalid match data";
        return ResponseEntity.badRequest().body(Map.of("message", msg));
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

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        var msg = e.getMessage() != null ? e.getMessage() : "Invalid input";
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        var error = e.getBindingResult().getFieldError();
        var msg = error != null ? error.getDefaultMessage() : "Validation error";
        return ResponseEntity.badRequest().body(Map.of("message", msg != null ? msg : "Validation error"));
    }
}
