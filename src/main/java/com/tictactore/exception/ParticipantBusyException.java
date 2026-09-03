package com.tictactore.exception;

public class ParticipantBusyException extends TournamentConflictException {

    public ParticipantBusyException(String message) {
        super(message);
    }
}
