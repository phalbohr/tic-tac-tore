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

@Profile({"test", "e2e", "dev", "default"})
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TestAuthController implements TestAuthApi {


    private final UserService userService;
    private final JwtService jwtService;
    private final ApplicationProperties properties;

    @Override
    public ResponseEntity<Void> testLogin(
            String email,
            String nickname,
            Boolean tutorialCompleted,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        User user = userService.findOrCreateTestUser(email, nickname, tutorialCompleted);

        String jwt = jwtService.generateToken(user);
        boolean isSecure = request.isSecure();
        java.time.Duration maxAge = java.time.Duration.ofMillis(properties.getJwt().getExpiration());

        ResponseCookie authCookie = com.tictactore.util.CookieUtils.buildCookie(
                CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME, jwt, isSecure, true, maxAge);

        ResponseCookie sessionCookie = com.tictactore.util.CookieUtils.buildCookie(
                CustomOAuth2SuccessHandler.SESSION_COOKIE_NAME, "true", isSecure, false, maxAge);

        response.addHeader(HttpHeaders.SET_COOKIE, authCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie.toString());

        return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                .location(java.net.URI.create(properties.getOauth2().getRedirectUri()))
                .build();
    }
}
