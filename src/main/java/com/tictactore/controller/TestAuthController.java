package com.tictactore.controller;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.model.User;
import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.service.JwtService;
import com.tictactore.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("!prod")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TestAuthController implements TestAuthApi {

    private static final String COOKIE_PATH = "/";
    private static final String COOKIE_SAME_SITE = "Lax";

    private final UserService userService;
    private final JwtService jwtService;
    private final ApplicationProperties properties;

    @Override
    @GetMapping("/test-login")
    public ResponseEntity<Void> testLogin(
            @RequestParam String email,
            @RequestParam String nickname,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        User user = userService.findOrCreateTestUser(email, nickname);

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
