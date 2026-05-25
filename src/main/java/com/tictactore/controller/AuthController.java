package com.tictactore.controller;

import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.JwtService;
import com.tictactore.repository.UserRepository;
import com.tictactore.model.User;
import com.tictactore.config.ApplicationProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
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
    private final UserRepository userRepository;
    private final ApplicationProperties properties;

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        var token = jwtService.extractToken(request);
        if (token != null) {
            try {
                tokenRevocationService.revoke(token);
            } catch (Exception e) {
                log.warn("Failed to revoke token in Redis during logout; session cookies will still be cleared", e);
            }
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

    @Override
    @GetMapping("/test-login")
    public ResponseEntity<Void> testLogin(
            @org.springframework.web.bind.annotation.RequestParam String email,
            @org.springframework.web.bind.annotation.RequestParam String nickname,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .nickname(nickname)
                            .avatar("https://api.dicebear.com/7.x/identicon/svg?seed=" + email)
                            .language("EN")
                            .build();
                    return userRepository.save(newUser);
                });

        String jwt = jwtService.generateToken(user);
        boolean isSecure = request.isSecure();
        java.time.Duration maxAge = java.time.Duration.ofMillis(properties.getJwt().getExpiration());

        ResponseCookie authCookie = ResponseCookie.from(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME, jwt)
                .httpOnly(true)
                .secure(isSecure)
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .sameSite(COOKIE_SAME_SITE)
                .build();

        ResponseCookie sessionCookie = ResponseCookie.from(CustomOAuth2SuccessHandler.SESSION_COOKIE_NAME, "true")
                .httpOnly(false)
                .secure(isSecure)
                .path(COOKIE_PATH)
                .maxAge(maxAge)
                .sameSite(COOKIE_SAME_SITE)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie.toString());

        return ResponseEntity.ok().build();
    }
}
