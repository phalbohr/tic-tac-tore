package com.tictactore.controller;

import com.tictactore.dto.UpdateProfileRequest;
import com.tictactore.model.User;
import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.security.JwtAuthenticationFilter;
import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({com.tictactore.config.SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("UserController ATDD Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private TokenRevocationService tokenRevocationService;

    @MockBean
    private CustomOAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("PATCH /me - should update language and nickname")
    void patchMe_shouldUpdateLanguageAndNickname() throws Exception {
        UUID userId = UUID.randomUUID();
        User principal = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("oldNickname")
                .build();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        User updatedUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("newNickname")
                .language("DE")
                .avatar("avatar-url")
                .build();
        when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class))).thenReturn(updatedUser);

        var result = mockMvc.perform(patch("/api/v1/profile/me")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"newNickname\",\"language\":\"DE\"}"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("newNickname"))
                .andExpect(jsonPath("$.language").value("DE"))
                .andExpect(jsonPath("$.avatar").value("avatar-url"));
    }

    @Test
    @DisplayName("PATCH /me - should return 400 when cooldown not passed")
    void patchMe_shouldReturn400_whenCooldownNotPassed() throws Exception {
        UUID userId = UUID.randomUUID();
        User principal = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("oldNickname")
                .build();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class)))
                .thenThrow(new com.tictactore.exception.ValidationException("Nickname can only be changed once every 30 days"));

        var result = mockMvc.perform(patch("/api/v1/profile/me")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"newNickname\"}"));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nickname can only be changed once every 30 days"));
    }

    @Test
    @DisplayName("DELETE /me - should return 204 and revoke token when authenticated")
    void deleteAccount_shouldReturn204AndRevokeToken_whenAuthenticated() throws Exception {
        UUID userId = UUID.randomUUID();
        User principal = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("oldNickname")
                .build();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        String token = "test-jwt-token";
        when(jwtService.extractToken(any())).thenReturn(token);

        var result = mockMvc.perform(delete("/api/v1/profile/me")
                        .header("Authorization", "Bearer " + token)
                        .with(authentication(auth))
                        .with(csrf()));

        result.andExpect(status().isNoContent());
        verify(userService).deleteAccount(userId);
        verify(tokenRevocationService).revoke(token);
    }

    @Test
    @DisplayName("DELETE /me - should return 401 when unauthenticated")
    void deleteAccount_shouldReturn401_whenUnauthenticated() throws Exception {
        var result = mockMvc.perform(delete("/api/v1/profile/me")
                        .with(csrf()));

        result.andExpect(status().isUnauthorized());
    }
}
