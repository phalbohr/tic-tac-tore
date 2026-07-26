package com.tictactore.exception;

public class InvalidPositionException extends RuntimeException {
    public InvalidPositionException(String message) {
        super(message);
    }
}
