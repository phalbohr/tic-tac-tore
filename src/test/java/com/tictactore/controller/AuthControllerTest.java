package com.tictactore.controller;

import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.security.JwtAuthenticationFilter;
import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({com.tictactore.config.SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("AuthController Tests")
class AuthControllerTest {

    private static final String ENDPOINT_LOGOUT = "/api/auth/logout";
    private static final String TOKEN_TEST = "test-token";
    private static final String MAX_AGE_ZERO = "Max-Age=0";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenRevocationService tokenRevocationService;

    @MockBean
    private CustomOAuth2SuccessHandler oAuth2SuccessHandler;
    
    @MockBean
    private JwtService jwtService;

    @MockBean
    private com.tictactore.repository.UserRepository userRepository;

    @MockBean
    private com.tictactore.config.ApplicationProperties properties;

    @Test
    @WithMockUser
    @DisplayName("Logout Without CSRF - should return Forbidden status")
    void logout_withoutCsrf_returnsForbidden() throws Exception {
        var result = mockMvc.perform(post(ENDPOINT_LOGOUT)
                .cookie(new Cookie(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME, TOKEN_TEST)));

        result.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    @DisplayName("Logout With CSRF - should revoke token and clear auth cookie")
    void logout_withCsrf_revokesTokenAndClearsCookie() throws Exception {
        when(jwtService.extractToken(any())).thenReturn(TOKEN_TEST);

        var result = mockMvc.perform(post(ENDPOINT_LOGOUT)
                .cookie(new Cookie(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME, TOKEN_TEST))
                .with(csrf()));

        result.andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME + "=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(MAX_AGE_ZERO)));
        verify(tokenRevocationService).revoke(TOKEN_TEST);
    }
}
