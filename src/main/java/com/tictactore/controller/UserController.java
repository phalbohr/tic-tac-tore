package com.tictactore.controller;

import com.tictactore.dto.ProfileDto;
import com.tictactore.model.User;
import com.tictactore.service.UserService;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.JwtService;
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

    @Override
    public ResponseEntity<ProfileDto> getMyProfile(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        var user = userService.getProfile(principal.getId());
        var profile = ProfileDto.builder()
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .language(user.getLanguage())
                .build();
                
        return ResponseEntity.ok(profile);
    }

    @Override
    public ResponseEntity<ProfileDto> updateProfile(
            @AuthenticationPrincipal User principal,
            com.tictactore.dto.UpdateProfileRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        var user = userService.updateProfile(principal.getId(), request);
        var profile = ProfileDto.builder()
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .language(user.getLanguage())
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

