package com.tictactore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RuleConfigurationInitializer {

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initializePresets() {
        try {
            jdbcTemplate.update("""
                INSERT INTO rule_configuration (
                    id, name, type, match_format, goal_limit, game_limit, games_to_win, win_by_two_rule, absolute_score_cap,
                    timeouts_per_game, timeout_duration_seconds, possession_limit_5bar_seconds, possession_limit_other_seconds,
                    side_swap_rule, restart_rule, spinning_allowed, aerials_allowed,
                    position_swap_rule, point_distribution, created_by, created_at, version
                ) SELECT 
                    '50f4a8e2-888e-4f10-9173-67c8cbcf8f3a', 'ITSF Standard Matchplay', 'PRESET', 'BEST_OF_N', 5, 5, 3, 'DECISIVE_GAME_ONLY', 8,
                    2, 30, 10, 15,
                    'BETWEEN_GAMES', 'CONCEDING_TEAM', FALSE, FALSE,
                    'FREE', 'WIN_LOSS_3_0', '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, 0
                WHERE NOT EXISTS (SELECT 1 FROM rule_configuration WHERE id = '50f4a8e2-888e-4f10-9173-67c8cbcf8f3a')
            """);

            jdbcTemplate.update("""
                INSERT INTO rule_configuration (
                    id, name, type, match_format, goal_limit, game_limit, games_to_win, win_by_two_rule, absolute_score_cap,
                    timeouts_per_game, timeout_duration_seconds, possession_limit_5bar_seconds, possession_limit_other_seconds,
                    side_swap_rule, restart_rule, spinning_allowed, aerials_allowed,
                    position_swap_rule, point_distribution, created_by, created_at, version
                ) SELECT 
                    '1e4b85ab-2c84-4861-ba04-eb17a3a5e8dc', 'DTFB Standard', 'PRESET', 'FIXED_GAMES', 5, 2, 2, 'NONE', NULL,
                    2, 30, 10, 15,
                    'NONE', 'CONCEDING_TEAM', FALSE, FALSE,
                    'NEVER', 'ONE_POINT_PER_GAME_WON', '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, 0
                WHERE NOT EXISTS (SELECT 1 FROM rule_configuration WHERE id = '1e4b85ab-2c84-4861-ba04-eb17a3a5e8dc')
            """);
            log.info("Rule configuration system presets verified/initialized");
        } catch (Exception e) {
            log.warn("Could not seed rule configuration presets: {}", e.getMessage());
        }
    }
}
