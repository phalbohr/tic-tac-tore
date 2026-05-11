package com.tictactore.security;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.model.User;
import com.tictactore.service.JwtService;
import com.tictactore.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomOAuth2SuccessHandler Tests")
class CustomOAuth2SuccessHandlerTest {

    private static final String ATTR_EMAIL = "email";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_SUB = "sub";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_PROVIDER_ID = "provider-123";
    private static final String TEST_JWT = "test-jwt-token";
    private static final String TEST_REDIRECT_URI = "http://localhost:3000/oauth2/redirect";
    private static final long TEST_EXPIRATION = 3600000L;

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private ApplicationProperties properties;
    @Mock
    private ApplicationProperties.Jwt jwtProperties;
    @Mock
    private ApplicationProperties.OAuth2 oauth2Properties;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private OAuth2AuthenticationToken token;
    @Mock
    private OAuth2User oauth2User;

    @InjectMocks
    private CustomOAuth2SuccessHandler handler;

    @BeforeEach
    void setUp() {
        // Mock encodeRedirectURL for standard redirect strategy behavior
    }

    @Test
    @DisplayName("Valid Attributes - should authenticate user and set secure cookies")
    void onAuthenticationSuccess_validAttributes_secureRequest() throws IOException {
        var user = new User();
        user.setEmail(TEST_EMAIL);
        user.setName(TEST_NAME);
        user.setProviderId(TEST_PROVIDER_ID);

        when(token.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(Map.of(
                ATTR_EMAIL, TEST_EMAIL,
                ATTR_NAME, TEST_NAME,
                ATTR_SUB, TEST_PROVIDER_ID
        ));
        when(userService.findOrCreate(TEST_EMAIL, TEST_NAME, TEST_PROVIDER_ID)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(TEST_JWT);
        when(request.isSecure()).thenReturn(true);
        when(properties.getJwt()).thenReturn(jwtProperties);
        when(jwtProperties.getExpiration()).thenReturn(TEST_EXPIRATION);
        when(properties.getOauth2()).thenReturn(oauth2Properties);
        when(oauth2Properties.getRedirectUri()).thenReturn(TEST_REDIRECT_URI);
        when(response.encodeRedirectURL(TEST_REDIRECT_URI)).thenReturn(TEST_REDIRECT_URI);

        handler.onAuthenticationSuccess(request, response, token);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), contains(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME + "=" + TEST_JWT));
        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), contains("Secure"));
        verify(response).sendRedirect(TEST_REDIRECT_URI);
    }

    @Test
    @DisplayName("Valid Attributes - should authenticate user and set insecure cookies")
    void onAuthenticationSuccess_validAttributes_insecureRequest() throws IOException {
        var user = new User();
        user.setEmail(TEST_EMAIL);
        user.setName(TEST_NAME);
        user.setProviderId(TEST_PROVIDER_ID);

        when(token.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(Map.of(
                ATTR_EMAIL, TEST_EMAIL,
                ATTR_NAME, TEST_NAME,
                ATTR_SUB, TEST_PROVIDER_ID
        ));
        when(userService.findOrCreate(TEST_EMAIL, TEST_NAME, TEST_PROVIDER_ID)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(TEST_JWT);
        when(request.isSecure()).thenReturn(false);
        when(properties.getJwt()).thenReturn(jwtProperties);
        when(jwtProperties.getExpiration()).thenReturn(TEST_EXPIRATION);
        when(properties.getOauth2()).thenReturn(oauth2Properties);
        when(oauth2Properties.getRedirectUri()).thenReturn(TEST_REDIRECT_URI);
        when(response.encodeRedirectURL(TEST_REDIRECT_URI)).thenReturn(TEST_REDIRECT_URI);

        handler.onAuthenticationSuccess(request, response, token);

        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), contains(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME + "=" + TEST_JWT));
        verify(response, never()).addHeader(eq(HttpHeaders.SET_COOKIE), contains("Secure"));
        verify(response).sendRedirect(TEST_REDIRECT_URI);
    }

    @Test
    @DisplayName("Missing Email - should throw OAuth2AuthenticationException")
    void onAuthenticationSuccess_missingEmail_shouldThrowException() {
        when(token.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(Map.of(
                ATTR_NAME, TEST_NAME,
                ATTR_SUB, TEST_PROVIDER_ID
        ));

        assertThrows(OAuth2AuthenticationException.class, () -> handler.onAuthenticationSuccess(request, response, token));

        verify(userService, never()).findOrCreate(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Missing Provider ID - should throw OAuth2AuthenticationException")
    void onAuthenticationSuccess_missingProviderId_shouldThrowException() {
        when(token.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttributes()).thenReturn(Map.of(
                ATTR_EMAIL, TEST_EMAIL,
                ATTR_NAME, TEST_NAME
        ));

        assertThrows(OAuth2AuthenticationException.class, () -> handler.onAuthenticationSuccess(request, response, token));

        verify(userService, never()).findOrCreate(anyString(), anyString(), anyString());
    }
}
