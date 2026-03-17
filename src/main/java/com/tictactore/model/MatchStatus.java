package com.tictactore.model;

/**
 * Lifecycle statuses for a Match.
 */
public enum MatchStatus {
    /** Match record is being prepared and not yet submitted for approval */
    DRAFT,
    /** Match is submitted and awaiting opponent confirmation */
    PENDING_APPROVAL,
    /** Match is confirmed by opponent and affects rankings */
    CONFIRMED
}
