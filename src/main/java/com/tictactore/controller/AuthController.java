package com.tictactore.controller;

import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private static final String COOKIE_EMPTY_VALUE = "";
    private static final String COOKIE_PATH = "/";
    private static final String COOKIE_SAME_SITE = "Lax";
    private static final int COOKIE_MAX_AGE_ZERO = 0;

    private final TokenRevocationService tokenRevocationService;
    private final JwtService jwtService;

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        var token = jwtService.extractToken(request);
        if (token != null) {
            tokenRevocationService.revoke(token);
        }

        var isSecure = request.isSecure();

        var authCookie = ResponseCookie.from(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME, COOKIE_EMPTY_VALUE)
                .httpOnly(true)
                .secure(isSecure)
                .path(COOKIE_PATH)
                .maxAge(COOKIE_MAX_AGE_ZERO)
                .sameSite(COOKIE_SAME_SITE)
                .build();

        var sessionCookie = ResponseCookie.from(CustomOAuth2SuccessHandler.SESSION_COOKIE_NAME, COOKIE_EMPTY_VALUE)
                .httpOnly(false)
                .secure(isSecure)
                .path(COOKIE_PATH)
                .maxAge(COOKIE_MAX_AGE_ZERO)
                .sameSite(COOKIE_SAME_SITE)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie.toString());

        return ResponseEntity.ok().build();
    }
}
