package com.tictactore.exception;

public class TournamentRuleMismatchException extends TournamentConflictException {

    public TournamentRuleMismatchException(String message) {
        super(message);
    }
}
