package com.tictactore.exception;

public class UnauthorizedMatchActionException extends RuntimeException {
    public UnauthorizedMatchActionException(String message) {
        super(message);
    }
}
