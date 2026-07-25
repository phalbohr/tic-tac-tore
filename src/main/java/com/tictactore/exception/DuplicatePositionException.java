package com.tictactore.exception;

public class DuplicatePositionException extends RuntimeException {
    public DuplicatePositionException(String message) {
        super(message);
    }
}
