package com.tictactore.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        if ("User not found".equals(e.getMessage())) {
            return ResponseEntity.notFound().build();
        }
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
