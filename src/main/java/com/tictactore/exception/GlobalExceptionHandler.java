package com.tictactore.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(com.tictactore.exception.ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(com.tictactore.exception.ResourceNotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(com.tictactore.exception.ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(com.tictactore.exception.ValidationException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "Invalid input";
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        var error = e.getBindingResult().getFieldError();
        String msg = error != null ? error.getDefaultMessage() : "Validation error";
        return ResponseEntity.badRequest().body(Map.of("message", msg != null ? msg : "Validation error"));
    }
}
