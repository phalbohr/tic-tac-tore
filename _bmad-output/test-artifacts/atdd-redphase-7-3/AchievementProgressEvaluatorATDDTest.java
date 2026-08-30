package com.tictactore.service.achievement;

import com.tictactore.service.achievement.evaluator.CleanSheetEvaluator;
import com.tictactore.service.achievement.evaluator.DefenseWallEvaluator;
import com.tictactore.service.achievement.evaluator.FirstWinEvaluator;
import com.tictactore.service.achievement.evaluator.GooseEggEvaluator;
import com.tictactore.service.achievement.evaluator.MatchesPlayedEvaluator;
import com.tictactore.service.achievement.evaluator.StrikerGoalsEvaluator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Story 7.3: Award Wall and Progress Tracking — Evaluator Progress ATDD Scaffolds (TDD Red Phase).
 *
 * AC2: Dynamic evaluation of progress for progressive milestones (MATCHES_10, STRIKER_50, DEFENSE_WALL, FIRST_WIN)
 * AC3: Non-progressive evaluators return hasProgress=false, current=0, target=0
 */
@Disabled("ATDD Red-Phase Scaffolds: Enable during Story 7.3 Task 1 & Task 3 implementation")
@DisplayName("Story 7.3: Achievement Evaluator Progress ATDD Tests")
class AchievementProgressEvaluatorATDDTest {

    private final MatchesPlayedEvaluator matchesPlayedEvaluator = new MatchesPlayedEvaluator();
    private final StrikerGoalsEvaluator strikerGoalsEvaluator = new StrikerGoalsEvaluator();
    private final DefenseWallEvaluator defenseWallEvaluator = new DefenseWallEvaluator();
    private final FirstWinEvaluator firstWinEvaluator = new FirstWinEvaluator();
    private final CleanSheetEvaluator cleanSheetEvaluator = new CleanSheetEvaluator();
    private final GooseEggEvaluator gooseEggEvaluator = new GooseEggEvaluator();

    @Nested
    @DisplayName("MATCHES_10 Evaluator Progress (AC2)")
    class MatchesPlayedProgressTests {

        @Test
        @DisplayName("[P0] [AC2] should calculate partial progress (e.g. 4/10 matches)")
        void shouldReturnPartialProgressForMatchesPlayed() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 4, 2, 8, 2);

            var progress = matchesPlayedEvaluator.getProgress(userId, stats);

            assertThat(progress.hasProgress()).isTrue();
            assertThat(progress.current()).isEqualTo(4L);
            assertThat(progress.target()).isEqualTo(10L);
        }

        @Test
        @DisplayName("[P0] [AC2] should cap current progress at milestone threshold when exceeded")
        void shouldCapProgressAtThresholdWhenExceeded() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 15, 8, 20, 5);

            var progress = matchesPlayedEvaluator.getProgress(userId, stats);

            assertThat(progress.hasProgress()).isTrue();
            assertThat(progress.current()).isEqualTo(10L);
            assertThat(progress.target()).isEqualTo(10L);
        }

        @Test
        @DisplayName("[P1] [AC2] should return 0/10 progress when player has 0 matches")
        void shouldReturnZeroProgressForZeroMatches() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 0, 0, 0, 0);

            var progress = matchesPlayedEvaluator.getProgress(userId, stats);

            assertThat(progress.hasProgress()).isTrue();
            assertThat(progress.current()).isEqualTo(0L);
            assertThat(progress.target()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("STRIKER_50 Evaluator Progress (AC2)")
    class StrikerGoalsProgressTests {

        @Test
        @DisplayName("[P0] [AC2] should calculate attacker goals progress (e.g. 23/50 goals)")
        void shouldReturnGoalsProgressForStriker() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 10, 5, 23, 3);

            var progress = strikerGoalsEvaluator.getProgress(userId, stats);

            assertThat(progress.hasProgress()).isTrue();
            assertThat(progress.current()).isEqualTo(23L);
            assertThat(progress.target()).isEqualTo(50L);
        }

        @Test
        @DisplayName("[P0] [AC2] should cap goals progress at 50 when exceeded")
        void shouldCapGoalsProgressAtThreshold() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 30, 20, 65, 5);

            var progress = strikerGoalsEvaluator.getProgress(userId, stats);

            assertThat(progress.hasProgress()).isTrue();
            assertThat(progress.current()).isEqualTo(50L);
            assertThat(progress.target()).isEqualTo(50L);
        }
    }

    @Nested
    @DisplayName("DEFENSE_WALL Evaluator Progress (AC2)")
    class DefenseWallProgressTests {

        @Test
        @DisplayName("[P0] [AC2] should calculate defender matches progress (e.g. 7/10 matches as defender)")
        void shouldReturnDefenderMatchesProgress() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 12, 6, 15, 7);

            var progress = defenseWallEvaluator.getProgress(userId, stats);

            assertThat(progress.hasProgress()).isTrue();
            assertThat(progress.current()).isEqualTo(7L);
            assertThat(progress.target()).isEqualTo(10L);
        }

        @Test
        @DisplayName("[P0] [AC2] should cap defender matches at 10 when threshold met")
        void shouldCapDefenderMatchesProgress() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 20, 10, 15, 14);

            var progress = defenseWallEvaluator.getProgress(userId, stats);

            assertThat(progress.hasProgress()).isTrue();
            assertThat(progress.current()).isEqualTo(10L);
            assertThat(progress.target()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("FIRST_WIN Evaluator Progress (AC2)")
    class FirstWinProgressTests {

        @Test
        @DisplayName("[P0] [AC2] should return 0/1 progress when player has 0 wins")
        void shouldReturnZeroProgressWhenNoWins() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 5, 0, 10, 2);

            var progress = firstWinEvaluator.getProgress(userId, stats);

            assertThat(progress.hasProgress()).isTrue();
            assertThat(progress.current()).isEqualTo(0L);
            assertThat(progress.target()).isEqualTo(1L);
        }

        @Test
        @DisplayName("[P0] [AC2] should return 1/1 progress when player has at least 1 win")
        void shouldReturnOneProgressWhenWinsExist() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 5, 3, 10, 2);

            var progress = firstWinEvaluator.getProgress(userId, stats);

            assertThat(progress.hasProgress()).isTrue();
            assertThat(progress.current()).isEqualTo(1L);
            assertThat(progress.target()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("Non-Progressive Evaluators Default Progress (AC3)")
    class NonProgressiveDefaultTests {

        @Test
        @DisplayName("[P1] [AC3] CLEAN_SHEET should return hasProgress=false via default getProgress")
        void cleanSheetShouldReturnNoProgress() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 10, 5, 20, 5);

            var progress = cleanSheetEvaluator.getProgress(userId, stats);

            assertThat(progress.hasProgress()).isFalse();
            assertThat(progress.current()).isEqualTo(0L);
            assertThat(progress.target()).isEqualTo(0L);
        }

        @Test
        @DisplayName("[P1] [AC3] GOOSE_EGG should return hasProgress=false via default getProgress")
        void gooseEggShouldReturnNoProgress() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 10, 5, 20, 5);

            var progress = gooseEggEvaluator.getProgress(userId, stats);

            assertThat(progress.hasProgress()).isFalse();
            assertThat(progress.current()).isEqualTo(0L);
            assertThat(progress.target()).isEqualTo(0L);
        }
    }
}
