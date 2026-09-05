package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.dto.RuleConfigurationResponse;
import com.tictactore.exception.GlobalExceptionHandler;
import com.tictactore.model.MatchFormat;
import com.tictactore.model.PointDistribution;
import com.tictactore.model.PositionSwapRule;
import com.tictactore.model.RestartRule;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.model.SideSwapRule;
import com.tictactore.model.User;
import com.tictactore.model.WinByTwoRule;
import com.tictactore.service.RuleConfigurationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("RuleConfigurationController ATDD Specifications — Create Rule Template")
class RuleConfigurationControllerATDDTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private RuleConfigurationService ruleConfigurationService;

    @InjectMocks
    private RuleConfigurationController ruleConfigurationController;

    private final UUID userId = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private final UUID presetId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID customRuleId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ruleConfigurationController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        User mockUser = User.builder()
                .id(userId)
                .email("player@example.com")
                .nickname("ProFoosballer")
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                mockUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private RuleConfigurationResponse createSamplePresetResponse() {
        return RuleConfigurationResponse.builder()
                .id(presetId)
                .name("ITSF Standard Matchplay")
                .type(RuleConfigurationType.PRESET)
                .createdBy(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .matchFormat(MatchFormat.BEST_OF_N)
                .goalLimit(5)
                .gameLimit(3)
                .gamesToWin(2)
                .winByTwoRule(WinByTwoRule.DECISIVE_GAME_ONLY)
                .absoluteScoreCap(8)
                .timeoutsPerGame(2)
                .timeoutDurationSeconds(30)
                .possessionLimit5BarSeconds(10)
                .possessionLimitOtherSeconds(15)
                .sideSwapRule(SideSwapRule.BETWEEN_GAMES)
                .restartRule(RestartRule.CONCEDING_TEAM)
                .spinningAllowed(false)
                .aerialsAllowed(false)
                .positionSwapRule(PositionSwapRule.BETWEEN_GAMES)
                .pointDistribution(PointDistribution.WIN_LOSS_3_0)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private RuleConfigurationResponse createSampleCustomResponse() {
        return RuleConfigurationResponse.builder()
                .id(customRuleId)
                .name("Office Fast 7")
                .type(RuleConfigurationType.CUSTOM)
                .createdBy(userId)
                .matchFormat(MatchFormat.BEST_OF_N)
                .goalLimit(7)
                .gameLimit(1)
                .gamesToWin(1)
                .winByTwoRule(WinByTwoRule.NONE)
                .absoluteScoreCap(null)
                .timeoutsPerGame(1)
                .timeoutDurationSeconds(20)
                .possessionLimit5BarSeconds(10)
                .possessionLimitOtherSeconds(15)
                .sideSwapRule(SideSwapRule.NONE)
                .restartRule(RestartRule.CONCEDING_TEAM)
                .spinningAllowed(false)
                .aerialsAllowed(false)
                .positionSwapRule(PositionSwapRule.FREE)
                .pointDistribution(PointDistribution.WIN_LOSS_2_0)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private RuleConfigurationRequest createValidCustomRequest() {
        return RuleConfigurationRequest.builder()
                .name("Office Fast 7")
                .matchFormat(MatchFormat.BEST_OF_N)
                .goalLimit(7)
                .gameLimit(1)
                .gamesToWin(1)
                .winByTwoRule(WinByTwoRule.NONE)
                .absoluteScoreCap(null)
                .timeoutsPerGame(1)
                .timeoutDurationSeconds(20)
                .possessionLimit5BarSeconds(10)
                .possessionLimitOtherSeconds(15)
                .sideSwapRule(SideSwapRule.NONE)
                .restartRule(RestartRule.CONCEDING_TEAM)
                .spinningAllowed(false)
                .aerialsAllowed(false)
                .positionSwapRule(PositionSwapRule.FREE)
                .pointDistribution(PointDistribution.WIN_LOSS_2_0)
                .build();
    }

    @Nested
    @DisplayName("AC 1: Query Available Rule Configurations")
    class QueryRuleConfigurations {

        @Test
        @DisplayName("GET /api/v1/rule-configurations should return presets and user-owned custom templates")
        void shouldReturnPresetsAndUserCustomRules() throws Exception {
            var preset = createSamplePresetResponse();
            var custom = createSampleCustomResponse();
            when(ruleConfigurationService.getAvailableRules(eq(userId), eq(null)))
                    .thenReturn(List.of(preset, custom));

            mockMvc.perform(get("/api/v1/rule-configurations"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("ITSF Standard Matchplay"))
                    .andExpect(jsonPath("$[0].type").value("PRESET"))
                    .andExpect(jsonPath("$[0].timeoutsPerGame").value(2))
                    .andExpect(jsonPath("$[0].sideSwapRule").value("BETWEEN_GAMES"))
                    .andExpect(jsonPath("$[1].name").value("Office Fast 7"))
                    .andExpect(jsonPath("$[1].type").value("CUSTOM"))
                    .andExpect(jsonPath("$[1].createdBy").value(userId.toString()));
        }

        @Test
        @DisplayName("GET /api/v1/rule-configurations?type=PRESET should filter by PRESET type")
        void shouldFilterByPresetType() throws Exception {
            var preset = createSamplePresetResponse();
            when(ruleConfigurationService.getAvailableRules(eq(userId), eq(RuleConfigurationType.PRESET)))
                    .thenReturn(List.of(preset));

            mockMvc.perform(get("/api/v1/rule-configurations").param("type", "PRESET"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].type").value("PRESET"));
        }

        @Test
        @DisplayName("GET /api/v1/rule-configurations/{id} should return single template when accessible")
        void shouldReturnRuleById_whenOwnedOrPreset() throws Exception {
            var custom = createSampleCustomResponse();
            when(ruleConfigurationService.getRuleById(eq(userId), eq(customRuleId)))
                    .thenReturn(custom);

            mockMvc.perform(get("/api/v1/rule-configurations/{id}", customRuleId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(customRuleId.toString()))
                    .andExpect(jsonPath("$.name").value("Office Fast 7"))
                    .andExpect(jsonPath("$.goalLimit").value(7))
                    .andExpect(jsonPath("$.positionSwapRule").value("FREE"));
        }

        @Test
        @DisplayName("GET /api/v1/rule-configurations/{id} should return 403 Forbidden when owned by another user")
        void shouldReturn403_whenRuleBelongsToAnotherUser() throws Exception {
            var foreignRuleId = UUID.randomUUID();
            when(ruleConfigurationService.getRuleById(eq(userId), eq(foreignRuleId)))
                    .thenThrow(new AccessDeniedException("Access denied to foreign rule configuration"));

            mockMvc.perform(get("/api/v1/rule-configurations/{id}", foreignRuleId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("AC 2 & AC 3: Create Custom Rule Configuration")
    class CreateRuleConfiguration {

        @Test
        @DisplayName("POST /api/v1/rule-configurations should create immutable custom template")
        void shouldCreateCustomRule_whenRequestValid() throws Exception {
            var request = createValidCustomRequest();
            var response = createSampleCustomResponse();
            when(ruleConfigurationService.createCustomRule(eq(userId), any(RuleConfigurationRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/rule-configurations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(customRuleId.toString()))
                    .andExpect(jsonPath("$.name").value("Office Fast 7"))
                    .andExpect(jsonPath("$.type").value("CUSTOM"))
                    .andExpect(jsonPath("$.createdBy").value(userId.toString()))
                    .andExpect(jsonPath("$.possessionLimit5BarSeconds").value(10))
                    .andExpect(jsonPath("$.pointDistribution").value("WIN_LOSS_2_0"));
        }

        @Test
        @DisplayName("POST /api/v1/rule-configurations should return 400 when user quota exceeded (max 20)")
        void shouldReturn400_whenQuotaExceeded() throws Exception {
            var request = createValidCustomRequest();
            when(ruleConfigurationService.createCustomRule(eq(userId), any(RuleConfigurationRequest.class)))
                    .thenThrow(new IllegalArgumentException("Custom rule template quota exceeded (maximum 20 templates per user)"));

            mockMvc.perform(post("/api/v1/rule-configurations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/rule-configurations should return 400 when template name is duplicate for creator")
        void shouldReturn400_whenNameIsDuplicate() throws Exception {
            var request = createValidCustomRequest();
            when(ruleConfigurationService.createCustomRule(eq(userId), any(RuleConfigurationRequest.class)))
                    .thenThrow(new IllegalArgumentException("Rule template with name 'Office Fast 7' already exists"));

            mockMvc.perform(post("/api/v1/rule-configurations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("AC 5: Delete Custom Rule Configuration")
    class DeleteRuleConfiguration {

        @Test
        @DisplayName("DELETE /api/v1/rule-configurations/{id} should return 204 No Content for owned custom template")
        void shouldDeleteCustomRule_whenOwnedByUser() throws Exception {
            doNothing().when(ruleConfigurationService).deleteCustomRule(eq(userId), eq(customRuleId));

            mockMvc.perform(delete("/api/v1/rule-configurations/{id}", customRuleId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /api/v1/rule-configurations/{id} should return 403 Forbidden when attempting to delete system preset")
        void shouldReturn403_whenDeletingSystemPreset() throws Exception {
            doThrow(new AccessDeniedException("System presets cannot be deleted"))
                    .when(ruleConfigurationService).deleteCustomRule(eq(userId), eq(presetId));

            mockMvc.perform(delete("/api/v1/rule-configurations/{id}", presetId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE /api/v1/rule-configurations/{id} should return 403 Forbidden when deleting template owned by another user")
        void shouldReturn403_whenDeletingOtherUserRule() throws Exception {
            var foreignRuleId = UUID.randomUUID();
            doThrow(new AccessDeniedException("Cannot delete custom rule template owned by another user"))
                    .when(ruleConfigurationService).deleteCustomRule(eq(userId), eq(foreignRuleId));

            mockMvc.perform(delete("/api/v1/rule-configurations/{id}", foreignRuleId))
                    .andExpect(status().isForbidden());
        }
    }
}
