package com.tictactore.service;

import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.dto.RuleConfigurationResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.MatchFormat;
import com.tictactore.model.PointDistribution;
import com.tictactore.model.PositionSwapRule;
import com.tictactore.model.RestartRule;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.model.SideSwapRule;
import com.tictactore.model.WinByTwoRule;
import com.tictactore.repository.RuleConfigurationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleConfigurationServiceTest {

    @Mock
    private RuleConfigurationOperation operation;

    @Mock
    private RuleConfigurationRepository repository;

    @InjectMocks
    private RuleConfigurationService service;

    private final UUID userId = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private final UUID foreignUserId = UUID.fromString("99999999-9999-9999-9999-999999999999");
    private final UUID ruleId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private RuleConfiguration samplePreset;
    private RuleConfiguration sampleCustom;
    private RuleConfigurationRequest sampleRequest;

    @BeforeEach
    void setUp() {
        samplePreset = RuleConfiguration.builder()
                .id(UUID.randomUUID())
                .name("ITSF Standard Matchplay")
                .type(RuleConfigurationType.PRESET)
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
                .createdBy(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .createdAt(OffsetDateTime.now())
                .build();

        sampleCustom = RuleConfiguration.builder()
                .id(ruleId)
                .name("Office Fast 7")
                .type(RuleConfigurationType.CUSTOM)
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
                .createdBy(userId)
                .createdAt(OffsetDateTime.now())
                .build();

        sampleRequest = RuleConfigurationRequest.builder()
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

    @Test
    @DisplayName("getAvailableRules should return all presets and user custom rules when type is null")
    void shouldReturnAllAvailableRules_whenTypeIsNull() {
        when(repository.findByTypeOrCreatedByOrderByCreatedAtDesc(RuleConfigurationType.PRESET, userId))
                .thenReturn(List.of(samplePreset, sampleCustom));

        List<RuleConfigurationResponse> result = service.getAvailableRules(userId, null);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("ITSF Standard Matchplay");
        assertThat(result.get(1).name()).isEqualTo("Office Fast 7");
    }

    @Test
    @DisplayName("getAvailableRules should return only presets when type is PRESET")
    void shouldReturnPresets_whenTypeIsPreset() {
        when(repository.findByTypeOrderByCreatedAtDesc(RuleConfigurationType.PRESET))
                .thenReturn(List.of(samplePreset));

        List<RuleConfigurationResponse> result = service.getAvailableRules(userId, RuleConfigurationType.PRESET);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(RuleConfigurationType.PRESET);
    }

    @Test
    @DisplayName("getAvailableRules should return only user custom rules when type is CUSTOM")
    void shouldReturnCustomRules_whenTypeIsCustom() {
        when(repository.findByCreatedByOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(sampleCustom));

        List<RuleConfigurationResponse> result = service.getAvailableRules(userId, RuleConfigurationType.CUSTOM);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(RuleConfigurationType.CUSTOM);
    }

    @Test
    @DisplayName("getRuleById should return preset rule for user")
    void shouldReturnPresetRuleById() {
        when(repository.findById(samplePreset.getId())).thenReturn(Optional.of(samplePreset));

        RuleConfigurationResponse result = service.getRuleById(userId, samplePreset.getId());

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("ITSF Standard Matchplay");
    }

    @Test
    @DisplayName("getRuleById should return user-owned custom rule")
    void shouldReturnCustomRuleById_whenOwned() {
        when(repository.findById(ruleId)).thenReturn(Optional.of(sampleCustom));

        RuleConfigurationResponse result = service.getRuleById(userId, ruleId);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Office Fast 7");
    }

    @Test
    @DisplayName("getRuleById should throw ResourceNotFoundException when rule does not exist")
    void shouldThrowNotFound_whenRuleDoesNotExist() {
        when(repository.findById(ruleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRuleById(userId, ruleId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getRuleById should throw AccessDeniedException when rule belongs to another user")
    void shouldThrowAccessDenied_whenRuleBelongsToAnotherUser() {
        RuleConfiguration foreignCustom = RuleConfiguration.builder()
                .id(ruleId)
                .name("Secret Custom")
                .type(RuleConfigurationType.CUSTOM)
                .createdBy(foreignUserId)
                .build();
        when(repository.findById(ruleId)).thenReturn(Optional.of(foreignCustom));

        assertThatThrownBy(() -> service.getRuleById(userId, ruleId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("createCustomRule should delegate to operation and return response")
    void shouldCreateCustomRule_whenValid() {
        when(operation.createCustomRule(eq(sampleRequest), eq(userId))).thenReturn(sampleCustom);

        RuleConfigurationResponse result = service.createCustomRule(userId, sampleRequest);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Office Fast 7");
        verify(operation).createCustomRule(sampleRequest, userId);
    }

    @Test
    @DisplayName("deleteCustomRule should delete custom rule owned by user")
    void shouldDeleteCustomRule_whenOwned() {
        when(repository.findById(ruleId)).thenReturn(Optional.of(sampleCustom));

        service.deleteCustomRule(userId, ruleId);

        verify(operation).deleteCustomRule(ruleId);
    }

    @Test
    @DisplayName("deleteCustomRule should throw AccessDeniedException when trying to delete system preset")
    void shouldThrowAccessDenied_whenDeletingPreset() {
        when(repository.findById(samplePreset.getId())).thenReturn(Optional.of(samplePreset));

        assertThatThrownBy(() -> service.deleteCustomRule(userId, samplePreset.getId()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("presets cannot be deleted");
    }

    @Test
    @DisplayName("deleteCustomRule should throw AccessDeniedException when deleting another user's rule")
    void shouldThrowAccessDenied_whenDeletingOtherUserRule() {
        RuleConfiguration foreignCustom = RuleConfiguration.builder()
                .id(ruleId)
                .name("Secret Custom")
                .type(RuleConfigurationType.CUSTOM)
                .createdBy(foreignUserId)
                .build();
        when(repository.findById(ruleId)).thenReturn(Optional.of(foreignCustom));

        assertThatThrownBy(() -> service.deleteCustomRule(userId, ruleId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("owned by another user");
    }
}
