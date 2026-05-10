package com.tictactore.controller;

import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.JwtService;
import jakarta.servlet.http.Cookie;
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

    private final TokenRevocationService tokenRevocationService;
    private final JwtService jwtService;

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtService.extractToken(request);
        if (token != null) {
            tokenRevocationService.revoke(token);
        }

        // Clear the cookie using ResponseCookie to ensure consistency
        ResponseCookie responseCookie = ResponseCookie.from(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(request.isSecure())
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        return ResponseEntity.ok().build();
    }
}