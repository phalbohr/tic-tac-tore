package com.tictactore.service.achievement;

public record ProgressInfo(
        long current,
        long target,
        boolean hasProgress
) {
}
