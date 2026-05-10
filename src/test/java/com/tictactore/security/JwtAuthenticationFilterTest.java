package com.tictactore.security;

import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter Tests")
class JwtAuthenticationFilterTest {

    private static final String TOKEN_VALID = "valid.token";
    private static final String TOKEN_REVOKED = "revoked.token";
    private static final String TOKEN_COOKIE = "valid.token.from.cookie";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_NAME = "name";
    private static final String EMAIL_TEST = "test@example.com";
    private static final String NAME_TEST = "Test User";
    private static final String ID_TEST = "123e4567-e89b-12d3-a456-426614174000";
    private static final String EMAIL_COOKIE = "cookie@example.com";
    private static final String NAME_COOKIE = "Cookie User";

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
    @DisplayName("Valid Token - should authenticate user when token is not revoked")
    void doFilterInternal_validToken_notRevoked() throws Exception {
        when(jwtService.extractToken(request)).thenReturn(TOKEN_VALID);
        when(jwtService.isTokenValid(TOKEN_VALID)).thenReturn(true);
        when(tokenRevocationService.isRevoked(TOKEN_VALID)).thenReturn(false);

        var mockClaims = new DefaultClaims(Map.of(CLAIM_EMAIL, EMAIL_TEST, CLAIM_NAME, NAME_TEST, Claims.SUBJECT, ID_TEST));
        when(jwtService.extractAllClaims(TOKEN_VALID)).thenReturn(mockClaims);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractAllClaims(TOKEN_VALID);
        verify(filterChain).doFilter(request, response);
        assert SecurityContextHolder.getContext().getAuthentication() != null;
    }

    @Test
    @DisplayName("Revoked Token - should not authenticate user")
    void doFilterInternal_validToken_isRevoked() throws Exception {
        when(jwtService.extractToken(request)).thenReturn(TOKEN_REVOKED);
        when(jwtService.isTokenValid(TOKEN_REVOKED)).thenReturn(true);
        when(tokenRevocationService.isRevoked(TOKEN_REVOKED)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService, never()).extractUserId(anyString());
        verify(filterChain).doFilter(request, response);
        assert SecurityContextHolder.getContext().getAuthentication() == null;
    }

    @Test
    @DisplayName("Valid Cookie Token - should authenticate user when cookie token is not revoked")
    void doFilterInternal_cookieToken_notRevoked() throws Exception {
        when(jwtService.extractToken(request)).thenReturn(TOKEN_COOKIE);
        when(jwtService.isTokenValid(TOKEN_COOKIE)).thenReturn(true);
        when(tokenRevocationService.isRevoked(TOKEN_COOKIE)).thenReturn(false);

        var mockClaims = new DefaultClaims(Map.of(CLAIM_EMAIL, EMAIL_COOKIE, CLAIM_NAME, NAME_COOKIE, Claims.SUBJECT, ID_TEST));
        when(jwtService.extractAllClaims(TOKEN_COOKIE)).thenReturn(mockClaims);

        filter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractAllClaims(TOKEN_COOKIE);
        verify(filterChain).doFilter(request, response);
        assert SecurityContextHolder.getContext().getAuthentication() != null;
    }
}
