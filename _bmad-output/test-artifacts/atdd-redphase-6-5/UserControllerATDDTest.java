package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.ApplicationProperties;
import com.tictactore.dto.UpdateProfileRequest;
import com.tictactore.model.User;
import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.security.JwtAuthenticationFilter;
import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({com.tictactore.config.SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("UserController ATDD Specifications — Pool Notifications Preference (Story 6.5)")
class UserControllerATDDTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private final UUID userId = UUID.fromString("50f4a8e2-888e-4f10-9173-67c8cbcf8f3a");

    private User principalUser;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        principalUser = User.builder()
                .id(userId)
                .email("player@example.com")
                .nickname("ProFoosballer")
                .avatar("avatar-1")
                .language("EN")
                .tutorialCompleted(true)
                .poolNotificationsEnabled(true)
                .build();

        auth = new UsernamePasswordAuthenticationToken(
                principalUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        ApplicationProperties.Jwt jwtProps = new ApplicationProperties.Jwt();
        jwtProps.setExpiration(3600000L);
        org.mockito.Mockito.lenient().when(applicationProperties.getJwt()).thenReturn(jwtProps);
    }

    @Nested
    @DisplayName("GET /api/v1/profile/me - Pool Notifications Preference Exposure (AC 4)")
    class GetProfilePoolPreferences {

        @Test
        @DisplayName("[P0] GET /api/v1/profile/me should return poolNotificationsEnabled true by default")
        void shouldReturnPoolNotificationsEnabledTrue() throws Exception {
            mockMvc.perform(get("/api/v1/profile/me")
                            .with(authentication(auth))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nickname").value("ProFoosballer"))
                    .andExpect(jsonPath("$.poolNotificationsEnabled").value(true));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/profile/me - Update Pool Notifications Preference (AC 4)")
    class PatchProfilePoolPreferences {

        @Test
        @DisplayName("[P0] PATCH /api/v1/profile/me should update poolNotificationsEnabled to false")
        void shouldUpdatePoolNotificationsEnabledToFalse() throws Exception {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .poolNotificationsEnabled(false)
                    .build();
            User updatedUser = User.builder()
                    .id(userId)
                    .email("player@example.com")
                    .nickname("ProFoosballer")
                    .avatar("avatar-1")
                    .language("EN")
                    .tutorialCompleted(true)
                    .poolNotificationsEnabled(false)
                    .build();
            when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class)))
                    .thenReturn(updatedUser);

            mockMvc.perform(patch("/api/v1/profile/me")
                            .with(authentication(auth))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.poolNotificationsEnabled").value(false));
        }

        @Test
        @DisplayName("[P0] PATCH /api/v1/profile/me should update poolNotificationsEnabled to true")
        void shouldUpdatePoolNotificationsEnabledToTrue() throws Exception {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .poolNotificationsEnabled(true)
                    .build();
            User updatedUser = User.builder()
                    .id(userId)
                    .email("player@example.com")
                    .nickname("ProFoosballer")
                    .avatar("avatar-1")
                    .language("EN")
                    .tutorialCompleted(true)
                    .poolNotificationsEnabled(true)
                    .build();
            when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class)))
                    .thenReturn(updatedUser);

            mockMvc.perform(patch("/api/v1/profile/me")
                            .with(authentication(auth))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.poolNotificationsEnabled").value(true));
        }
    }
}
