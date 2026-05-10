package com.tictactore.security;

import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private TokenRevocationService tokenRevocationService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_validToken_notRevoked() throws Exception {
        String token = "valid.token";
        when(jwtService.extractToken(request)).thenReturn(token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(tokenRevocationService.isRevoked(token)).thenReturn(false);
        when(jwtService.extractUserId(token)).thenReturn("123e4567-e89b-12d3-a456-426614174000");

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUserId(token);
        verify(filterChain).doFilter(request, response);
        assert SecurityContextHolder.getContext().getAuthentication() != null;
    }

    @Test
    void doFilterInternal_validToken_isRevoked() throws Exception {
        String token = "revoked.token";
        when(jwtService.extractToken(request)).thenReturn(token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(tokenRevocationService.isRevoked(token)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService, never()).extractUserId(anyString());
        verify(filterChain).doFilter(request, response);
        assert SecurityContextHolder.getContext().getAuthentication() == null;
    }

    @Test
    void doFilterInternal_cookieToken_notRevoked() throws Exception {
        String token = "valid.token.from.cookie";
        when(jwtService.extractToken(request)).thenReturn(token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(tokenRevocationService.isRevoked(token)).thenReturn(false);
        when(jwtService.extractUserId(token)).thenReturn("123e4567-e89b-12d3-a456-426614174000");

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUserId(token);
        verify(filterChain).doFilter(request, response);
        assert SecurityContextHolder.getContext().getAuthentication() != null;
    }
}