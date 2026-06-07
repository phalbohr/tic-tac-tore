package com.tictactore.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CookieUtils Tests")
class CookieUtilsTest {

    private static final String TEST_NAME = "test-cookie";
    private static final String TEST_VALUE = "test-value";
    private static final boolean IS_SECURE = true;
    private static final boolean IS_HTTP_ONLY = true;
    private static final Duration MAX_AGE = Duration.ofDays(1);
    private static final String EXPECTED_PATH = "/";
    private static final String EXPECTED_SAME_SITE = "Lax";

    @Test
    @DisplayName("buildCookie - should construct ResponseCookie with expected attributes")
    void shouldConstructCookieWithExpectedAttributes() {
        var result = CookieUtils.buildCookie(TEST_NAME, TEST_VALUE, IS_SECURE, IS_HTTP_ONLY, MAX_AGE);

        assertThat(result.getName()).isEqualTo(TEST_NAME);
        assertThat(result.getValue()).isEqualTo(TEST_VALUE);
        assertThat(result.isSecure()).isEqualTo(IS_SECURE);
        assertThat(result.isHttpOnly()).isEqualTo(IS_HTTP_ONLY);
        assertThat(result.getMaxAge()).isEqualTo(MAX_AGE);
        assertThat(result.getPath()).isEqualTo(EXPECTED_PATH);
        assertThat(result.getSameSite()).isEqualTo(EXPECTED_SAME_SITE);
    }
}
