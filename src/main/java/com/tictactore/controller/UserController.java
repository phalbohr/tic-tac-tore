package com.tictactore.controller;

import com.tictactore.dto.ProfileDto;
import com.tictactore.model.User;
import com.tictactore.service.UserService;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.JwtService;
import com.tictactore.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserController implements ProfileApi {

    private final UserService userService;
    private final TokenRevocationService tokenRevocationService;
    private final JwtService jwtService;
    private final ApplicationProperties properties;

    @Override
    public ResponseEntity<ProfileDto> getMyProfile(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        var profile = ProfileDto.builder()
                .nickname(principal.getNickname())
                .avatar(principal.getAvatar())
                .language(principal.getLanguage())
                .tutorialCompleted(principal.isTutorialCompleted())
                .build();
                
        return ResponseEntity.ok(profile);
    }

    @Override
    public ResponseEntity<ProfileDto> updateProfile(
            @AuthenticationPrincipal User principal,
            com.tictactore.dto.UpdateProfileRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse httpResponse
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        var user = userService.updateProfile(principal.getId(), request);

        String oldToken = jwtService.extractToken(httpRequest);
        if (oldToken != null) {
            tokenRevocationService.revoke(oldToken);
        }

        String newToken = jwtService.generateToken(user);
        var isSecure = httpRequest.isSecure();
        var maxAge = java.time.Duration.ofMillis(properties.getJwt().getExpiration());
        var authCookie = com.tictactore.util.CookieUtils.buildCookie(com.tictactore.security.CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME, newToken, isSecure, true, maxAge);
        httpResponse.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, authCookie.toString());

        var profile = ProfileDto.builder()
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .language(user.getLanguage())
                .tutorialCompleted(user.isTutorialCompleted())
                .build();
                
        return ResponseEntity.ok(profile);
    }

    @Override
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal User principal,
            String authHeader,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        userService.deleteAccount(principal.getId());

        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else {
            token = jwtService.extractToken(request);
        }

        if (response != null) {
            var isSecure = request.isSecure();
            var authCookie = org.springframework.http.ResponseCookie.from(com.tictactore.security.CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME, "")
                    .httpOnly(true)
                    .secure(isSecure)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            var sessionCookie = org.springframework.http.ResponseCookie.from(com.tictactore.security.CustomOAuth2SuccessHandler.SESSION_COOKIE_NAME, "")
                    .httpOnly(false)
                    .secure(isSecure)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, authCookie.toString());
            response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, sessionCookie.toString());
        }

        if (token != null) {
            tokenRevocationService.revoke(token);
        }

        var session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        return ResponseEntity.noContent().build();
    }
}

