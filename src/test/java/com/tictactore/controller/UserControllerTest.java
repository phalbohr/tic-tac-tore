package com.tictactore.controller;

import com.tictactore.dto.UpdateProfileRequest;
import com.tictactore.model.User;
import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.security.JwtAuthenticationFilter;
import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.config.ApplicationProperties;
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
import org.springframework.security.test.context.support.WithAnonymousUser;

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

    @MockBean
    private ApplicationProperties applicationProperties;

    @Test
    @DisplayName("GET /me - should return profile from principal without calling userService")
    void getMyProfile_shouldReturnProfileFromPrincipal() throws Exception {
        UUID userId = UUID.randomUUID();
        User principal = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("testUser")
                .avatar("avatar.png")
                .language("EN")
                .tutorialCompleted(true)
                .build();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        var result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/profile/me")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("testUser"))
                .andExpect(jsonPath("$.avatar").value("avatar.png"))
                .andExpect(jsonPath("$.language").value("EN"))
                .andExpect(jsonPath("$.tutorialCompleted").value(true));

        verify(userService, org.mockito.Mockito.never()).getProfile(any());
    }

    @Test
    @DisplayName("PATCH /me - should update language and nickname and set cookie")
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
        when(jwtService.extractToken(any())).thenReturn("oldToken");
        when(jwtService.generateToken(updatedUser)).thenReturn("newToken");
        ApplicationProperties.Jwt jwtProps = new ApplicationProperties.Jwt();
        jwtProps.setExpiration(3600000L);
        when(applicationProperties.getJwt()).thenReturn(jwtProps);

        var result = mockMvc.perform(patch("/api/v1/profile/me")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"newNickname\",\"language\":\"DE\"}"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("newNickname"))
                .andExpect(jsonPath("$.language").value("DE"))
                .andExpect(jsonPath("$.avatar").value("avatar-url"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Set-Cookie", org.hamcrest.Matchers.containsString("TTT_TOKEN=newToken")));

        verify(tokenRevocationService).revoke("oldToken");
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
    @DisplayName("PATCH /me - should update avatar")
    void patchMe_shouldUpdateAvatar() throws Exception {
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
                .nickname("oldNickname")
                .avatar("ball-classic")
                .build();
        when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class))).thenReturn(updatedUser);
        when(jwtService.extractToken(any())).thenReturn(null);
        when(jwtService.generateToken(updatedUser)).thenReturn("newToken");
        ApplicationProperties.Jwt jwtProps = new ApplicationProperties.Jwt();
        jwtProps.setExpiration(3600000L);
        when(applicationProperties.getJwt()).thenReturn(jwtProps);

        var result = mockMvc.perform(patch("/api/v1/profile/me")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatar\":\"ball-classic\"}"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.avatar").value("ball-classic"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Set-Cookie", org.hamcrest.Matchers.containsString("TTT_TOKEN=newToken")));
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
    @DisplayName("PATCH /me - should return 400 when avatar is invalid")
    void patchMe_shouldReturn400_whenAvatarIsInvalid() throws Exception {
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

        var result = mockMvc.perform(patch("/api/v1/profile/me")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatar\":\"invalid-avatar-name\"}"));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid avatar selection"));
        verify(userService, org.mockito.Mockito.never()).updateProfile(any(), any());
    }

    @Test
    @DisplayName("PATCH /me - should return 400 when setting anonymous avatar")
    void patchMe_shouldReturn400_whenSettingAnonymousAvatar() throws Exception {
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

        var result = mockMvc.perform(patch("/api/v1/profile/me")
                        .with(authentication(auth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"avatar\":\"anonymous\"}"));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid avatar selection"));
        verify(userService, org.mockito.Mockito.never()).updateProfile(any(), any());
    }

    @Test
    @DisplayName("DELETE /me - should return 401 when unauthenticated")
    @WithAnonymousUser
    void deleteAccount_shouldReturn401_whenUnauthenticated() throws Exception {
        var result = mockMvc.perform(delete("/api/v1/profile/me")
                        .with(csrf()));

        result.andExpect(status().isUnauthorized());
    }
}
