package com.tictactore.dto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public enum TimePeriod {
    ALL_TIME,
    LAST_WEEK,
    WEEKLY,
    LAST_MONTH,
    MONTHLY,
    LAST_YEAR,
    YEARLY;

    public Instant getStartDate() {
        Instant now = Instant.now();
        return switch (this) {
            case LAST_WEEK, WEEKLY -> now.minus(7, ChronoUnit.DAYS);
            case LAST_MONTH, MONTHLY -> now.minus(30, ChronoUnit.DAYS);
            case LAST_YEAR, YEARLY -> now.minus(365, ChronoUnit.DAYS);
            case ALL_TIME -> null;
        };
    }
}
