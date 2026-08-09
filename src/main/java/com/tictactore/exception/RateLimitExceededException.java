package com.tictactore.exception;

public class RateLimitExceededException extends RuntimeException {
    private final int retryAfterSeconds;
    private final boolean redisFailure;

    public RateLimitExceededException(int retryAfterSeconds, String message) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.redisFailure = false;
    }

    public RateLimitExceededException(String message, Throwable cause) {
        super(message, cause);
        this.retryAfterSeconds = 0;
        this.redisFailure = true;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public boolean isRedisFailure() {
        return redisFailure;
    }
}
