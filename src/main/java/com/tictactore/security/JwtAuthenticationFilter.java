package com.tictactore.security;

import com.tictactore.model.User;
import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_AVATAR = "avatar";
    private static final String CLAIM_LANGUAGE = "language";
    private static final String CLAIM_TUTORIAL_COMPLETED = "tutorialCompleted";
    private static final String ROLE_USER = "ROLE_USER";

    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        var token = jwtService.extractToken(request);

        if (token != null && jwtService.isTokenValid(token)) {
            if (!tokenRevocationService.isRevoked(token)) {
                var claims = jwtService.extractAllClaims(token);
                var userId = claims.getSubject();
                var email = claims.get(CLAIM_EMAIL, String.class);
                var nickname = claims.get(CLAIM_NAME, String.class);
                var avatar = claims.get(CLAIM_AVATAR, String.class);
                var language = claims.get(CLAIM_LANGUAGE, String.class);
                var tutorialCompleted = claims.get(CLAIM_TUTORIAL_COMPLETED, Boolean.class);

                if (userId == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing user ID");
                    return;
                }

                UUID uuid;
                try {
                    uuid = UUID.fromString(userId);
                } catch (IllegalArgumentException e) {
                    String effectiveEmail = (email != null && !email.isBlank()) ? email : userId;
                    uuid = UUID.nameUUIDFromBytes(effectiveEmail.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    if (email == null) {
                        email = effectiveEmail;
                    }
                    if (nickname == null) {
                        nickname = effectiveEmail.split("@")[0];
                    }
                }

                User user = User.builder()
                        .id(uuid)
                        .email(email)
                        .nickname(nickname)
                        .avatar(avatar)
                        .language(language)
                        .tutorialCompleted(Boolean.TRUE.equals(tutorialCompleted))
                        .build();

                var authentication = new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(ROLE_USER))
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
