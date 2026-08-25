package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.ApplicationProperties;
import com.tictactore.dto.ProfileDto;
import com.tictactore.dto.UpdateProfileRequest;
import com.tictactore.exception.GlobalExceptionHandler;
import com.tictactore.model.User;
import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController ATDD Specifications — Default Team and Rule Template (Story 6.2)")
class UserControllerATDDTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @Mock
    private TokenRevocationService tokenRevocationService;

    @Mock
    private JwtService jwtService;

    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private UserController userController;

    private final UUID userId = UUID.fromString("50f4a8e2-888e-4f10-9173-67c8cbcf8f3a");
    private final UUID validGroupId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID validRulePresetId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID validCustomRuleId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final UUID foreignGroupId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private final UUID foreignRuleId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");

    private User principalUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        principalUser = User.builder()
                .id(userId)
                .email("player@example.com")
                .nickname("ProFoosballer")
                .avatar("avatar-1")
                .language("EN")
                .tutorialCompleted(true)
                .defaultGroupId(validGroupId)
                .defaultRuleConfigurationId(validRulePresetId)
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principalUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("AC 1 & AC 2: GET /api/v1/profile/me - Default Preferences Exposure")
    class GetProfileDefaults {

        @Test
        @DisplayName("GET /api/v1/profile/me should return defaultGroupId and defaultRuleConfigurationId from principal")
        void shouldReturnDefaultPreferencesFromPrincipal() throws Exception {
            mockMvc.perform(get("/api/v1/profile/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nickname").value("ProFoosballer"))
                    .andExpect(jsonPath("$.defaultGroupId").value(validGroupId.toString()))
                    .andExpect(jsonPath("$.defaultRuleConfigurationId").value(validRulePresetId.toString()));
        }

        @Test
        @DisplayName("GET /api/v1/profile/me should return null defaults when not set on principal")
        void shouldReturnNullDefaultsWhenNotSet() throws Exception {
            User userWithoutDefaults = User.builder()
                    .id(userId)
                    .email("player@example.com")
                    .nickname("ProFoosballer")
                    .avatar("avatar-1")
                    .language("EN")
                    .tutorialCompleted(true)
                    .defaultGroupId(null)
                    .defaultRuleConfigurationId(null)
                    .build();
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userWithoutDefaults,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);

            mockMvc.perform(get("/api/v1/profile/me")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.defaultGroupId").doesNotExist())
                    .andExpect(jsonPath("$.defaultRuleConfigurationId").doesNotExist());
        }
    }

    @Nested
    @DisplayName("AC 2: PATCH /api/v1/profile/me - Update and Validate Default Preferences")
    class PatchProfileDefaults {

        @Test
        @DisplayName("PATCH /api/v1/profile/me should successfully persist valid defaultGroupId and custom defaultRuleConfigurationId")
        void shouldPersistValidGroupAndRuleDefaults() throws Exception {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .defaultGroupId(validGroupId)
                    .defaultRuleConfigurationId(validCustomRuleId)
                    .build();

            User updatedUser = User.builder()
                    .id(userId)
                    .email("player@example.com")
                    .nickname("ProFoosballer")
                    .avatar("avatar-1")
                    .language("EN")
                    .tutorialCompleted(true)
                    .defaultGroupId(validGroupId)
                    .defaultRuleConfigurationId(validCustomRuleId)
                    .build();

            when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class)))
                    .thenReturn(updatedUser);

            mockMvc.perform(patch("/api/v1/profile/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.defaultGroupId").value(validGroupId.toString()))
                    .andExpect(jsonPath("$.defaultRuleConfigurationId").value(validCustomRuleId.toString()));
        }

        @Test
        @DisplayName("PATCH /api/v1/profile/me should allow clearing defaults by sending null")
        void shouldClearDefaultsWhenNullProvided() throws Exception {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .defaultGroupId(null)
                    .defaultRuleConfigurationId(null)
                    .build();

            User updatedUser = User.builder()
                    .id(userId)
                    .email("player@example.com")
                    .nickname("ProFoosballer")
                    .avatar("avatar-1")
                    .language("EN")
                    .tutorialCompleted(true)
                    .defaultGroupId(null)
                    .defaultRuleConfigurationId(null)
                    .build();

            when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class)))
                    .thenReturn(updatedUser);

            mockMvc.perform(patch("/api/v1/profile/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"defaultGroupId\": null, \"defaultRuleConfigurationId\": null}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.defaultGroupId").doesNotExist())
                    .andExpect(jsonPath("$.defaultRuleConfigurationId").doesNotExist());
        }

        @Test
        @DisplayName("PATCH /api/v1/profile/me should return 400 Bad Request when defaultGroupId belongs to another user")
        void shouldReturn400WhenDefaultGroupBelongsToAnotherUser() throws Exception {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .defaultGroupId(foreignGroupId)
                    .build();

            when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class)))
                    .thenThrow(new IllegalArgumentException("Selected player group does not exist or does not belong to the user"));

            mockMvc.perform(patch("/api/v1/profile/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Selected player group does not exist or does not belong to the user"));
        }

        @Test
        @DisplayName("PATCH /api/v1/profile/me should return 400 Bad Request when defaultRuleConfigurationId belongs to another user and is not a preset")
        void shouldReturn400WhenDefaultRuleBelongsToAnotherUser() throws Exception {
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .defaultRuleConfigurationId(foreignRuleId)
                    .build();

            when(userService.updateProfile(eq(userId), any(UpdateProfileRequest.class)))
                    .thenThrow(new IllegalArgumentException("Selected rule configuration does not exist or is not accessible"));

            mockMvc.perform(patch("/api/v1/profile/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Selected rule configuration does not exist or is not accessible"));
        }
    }
}
