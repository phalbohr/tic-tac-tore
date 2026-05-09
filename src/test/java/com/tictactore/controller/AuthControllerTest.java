package com.tictactore.controller;

import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.security.JwtAuthenticationFilter;
import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
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
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({com.tictactore.config.SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenRevocationService tokenRevocationService;

    // Need these to load SecurityConfig successfully
    @MockBean
    private CustomOAuth2SuccessHandler oAuth2SuccessHandler;
    
    @MockBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void logout_withoutCsrf_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME, "test-token")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void logout_withCsrf_revokesTokenAndClearsCookie() throws Exception {
        String token = "test-token";
        
        mockMvc.perform(post("/api/auth/logout")
                .cookie(new Cookie(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME, token))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString(CustomOAuth2SuccessHandler.AUTH_COOKIE_NAME + "=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        verify(tokenRevocationService).revoke(token);
    }
}
