package com.tictactore.repository;

import com.tictactore.model.MatchFormat;
import com.tictactore.model.PointDistribution;
import com.tictactore.model.PositionSwapRule;
import com.tictactore.model.RestartRule;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.model.SideSwapRule;
import com.tictactore.model.WinByTwoRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("RuleConfigurationRepository DataJpa Tests")
class RuleConfigurationRepositoryTest {

    @Autowired
    private RuleConfigurationRepository repository;

    private final UUID userA = UUID.randomUUID();
    private final UUID userB = UUID.randomUUID();

    private RuleConfiguration createRule(String name, RuleConfigurationType type, UUID createdBy) {
        return RuleConfiguration.builder()
                .name(name)
                .type(type)
                .matchFormat(MatchFormat.BEST_OF_N)
                .goalLimit(5)
                .gameLimit(3)
                .gamesToWin(2)
                .winByTwoRule(WinByTwoRule.NONE)
                .absoluteScoreCap(null)
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
                .createdBy(createdBy)
                .build();
    }

    @Test
    @DisplayName("findByTypeOrCreatedByOrderByCreatedAtDesc should return presets and user custom rules")
    void shouldFindPresetsAndUserRules() {
        RuleConfiguration preset = repository.save(createRule("Preset Test", RuleConfigurationType.PRESET, UUID.fromString("00000000-0000-0000-0000-000000000000")));
        RuleConfiguration userARule = repository.save(createRule("User A Rule", RuleConfigurationType.CUSTOM, userA));
        repository.save(createRule("User B Rule", RuleConfigurationType.CUSTOM, userB));

        List<RuleConfiguration> result = repository.findByTypeOrCreatedByOrderByCreatedAtDesc(RuleConfigurationType.PRESET, userA);

        assertThat(result)
                .extracting(RuleConfiguration::getName)
                .contains(preset.getName(), userARule.getName())
                .doesNotContain("User B Rule");
    }

    @Test
    @DisplayName("findByCreatedByOrderByCreatedAtDesc should return only rules created by specific user")
    void shouldFindByCreatedBy() {
        RuleConfiguration userARule1 = repository.save(createRule("User A Rule 1", RuleConfigurationType.CUSTOM, userA));
        RuleConfiguration userARule2 = repository.save(createRule("User A Rule 2", RuleConfigurationType.CUSTOM, userA));
        repository.save(createRule("User B Rule", RuleConfigurationType.CUSTOM, userB));

        List<RuleConfiguration> result = repository.findByCreatedByOrderByCreatedAtDesc(userA);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RuleConfiguration::getId).containsExactlyInAnyOrder(userARule1.getId(), userARule2.getId());
    }

    @Test
    @DisplayName("countByCreatedBy should return accurate count of custom templates for user")
    void shouldCountByCreatedBy() {
        repository.save(createRule("Rule 1", RuleConfigurationType.CUSTOM, userA));
        repository.save(createRule("Rule 2", RuleConfigurationType.CUSTOM, userA));

        long count = repository.countByCreatedBy(userA);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("existsByCreatedByAndNameIgnoreCase should match template names case-insensitively")
    void shouldCheckExistenceCaseInsensitively() {
        repository.save(createRule("Fast Foosball", RuleConfigurationType.CUSTOM, userA));

        boolean existsExact = repository.existsByCreatedByAndNameIgnoreCase(userA, "Fast Foosball");
        boolean existsLower = repository.existsByCreatedByAndNameIgnoreCase(userA, "fast foosball");
        boolean existsUpper = repository.existsByCreatedByAndNameIgnoreCase(userA, "FAST FOOSBALL");
        boolean existsOtherUser = repository.existsByCreatedByAndNameIgnoreCase(userB, "Fast Foosball");

        assertThat(existsExact).isTrue();
        assertThat(existsLower).isTrue();
        assertThat(existsUpper).isTrue();
        assertThat(existsOtherUser).isFalse();
    }
}
