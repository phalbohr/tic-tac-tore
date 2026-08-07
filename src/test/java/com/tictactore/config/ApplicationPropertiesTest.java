package com.tictactore.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPropertiesTest {

    @Test
    @DisplayName("[P1] Should have default rate-limit values of 10/5/24/30")
    void shouldHaveDefaultRateLimitValues() {
        ApplicationProperties properties = new ApplicationProperties();

        var rateLimit = properties.getRateLimit();
        assertThat(rateLimit.getStandaloneSubmissionsPerHour()).isEqualTo(10);
        assertThat(rateLimit.getRejectionThreshold()).isEqualTo(5);
        assertThat(rateLimit.getRejectionWindowHours()).isEqualTo(24);
        assertThat(rateLimit.getTournamentSubmissionsPerHour()).isEqualTo(30);
    }
}
