package com.tictactore.service;

import java.util.UUID;

public interface RateLimitService {
    void checkSubmissionLimit(UUID userId);

    void recordRejection(UUID userId);

    void checkSearchLimit(String clientIp);
}
