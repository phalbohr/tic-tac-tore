package com.tictactore.util;

import org.springframework.http.ResponseCookie;
import java.time.Duration;

public class CookieUtils {

    private static final String COOKIE_PATH = "/";
    private static final String COOKIE_SAME_SITE = "Lax";

    private CookieUtils() {
    }

    public static ResponseCookie buildCookie(String name, String value, boolean isSecure, boolean isHttpOnly, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(isHttpOnly)
                .secure(isSecure)
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .sameSite(COOKIE_SAME_SITE)
                .build();
    }
}
