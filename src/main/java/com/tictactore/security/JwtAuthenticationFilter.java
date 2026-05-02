package com.tictactore.security;

import com.tictactore.model.User;
import com.tictactore.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
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

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null && jwtService.isTokenValid(token)) {
            // Performance: Database Exhaustion in JWT Filter - Rely on JWT claims instead of DB lookup.
            String userId = jwtService.extractUserId(token);
            // We reconstruct a minimal User object from JWT claims to avoid DB hit.
            // If the application logic needs more user details, it can load them when needed.
            User user = User.builder()
                    .id(UUID.fromString(userId))
                    .build();

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("TTT_TOKEN".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
