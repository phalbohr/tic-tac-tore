package com.tictactore.security;

import com.tictactore.repository.UserRepository;
import com.tictactore.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomOAuth2SuccessHandler Tests")
public class CustomOAuth2SuccessHandlerTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private OAuth2User oAuth2User;

    private CustomOAuth2SuccessHandler successHandler;
    private final String redirectUri = "http://localhost:3000/oauth2/redirect";

    @BeforeEach
    void setUp() {
        successHandler = new CustomOAuth2SuccessHandler(jwtService, userRepository, redirectUri);
    }

    @Test
    @DisplayName("onAuthenticationSuccess - should redirect to frontend with JWT token")
    void onAuthenticationSuccess_shouldRedirectWithToken() throws Exception {
        // Given
        String email = "test@example.com";
        String token = "mocked-jwt-token";

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(email);
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(oAuth2User.getName()).thenReturn("google-id");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(jwtService.generateToken(email)).thenReturn(token);
        
        // Essential: Mock the URL encoding behavior of HttpServletResponse
        // DefaultRedirectStrategy calls this before sendRedirect
        when(response.encodeRedirectURL(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Then
        String expectedTargetUrl = redirectUri + "?token=" + token;
        verify(response).sendRedirect(expectedTargetUrl);
        verify(userRepository).save(any());
    }
}
