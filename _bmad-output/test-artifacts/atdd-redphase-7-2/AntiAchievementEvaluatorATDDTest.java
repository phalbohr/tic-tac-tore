package com.tictactore.service.achievement;

import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.service.achievement.evaluator.GenerousHostEvaluator;
import com.tictactore.service.achievement.evaluator.GooseEggEvaluator;
import com.tictactore.service.achievement.evaluator.HeartbreakerEvaluator;
import com.tictactore.service.achievement.evaluator.SieveDefenseEvaluator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Story 7.2: Anti-Achievement Evaluators ATDD Tests")
class AntiAchievementEvaluatorATDDTest {

    private final GooseEggEvaluator gooseEggEvaluator = new GooseEggEvaluator();
    private final GenerousHostEvaluator generousHostEvaluator = new GenerousHostEvaluator();
    private final SieveDefenseEvaluator sieveDefenseEvaluator = new SieveDefenseEvaluator();
    private final HeartbreakerEvaluator heartbreakerEvaluator = new HeartbreakerEvaluator();

    @Nested
    @DisplayName("GOOSE_EGG Evaluator (AC1)")
    class GooseEggTests {

        @Test
        @DisplayName("[P0] [AC1] should return true when player team scored 0 points in any game")
        void shouldReturnTrueWhenPlayerTeamScoredZeroInAnyGame() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(10).teamBScore(5).build();
            var game2 = Game.builder().teamAScore(0).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2))
                    .build();

            var result = gooseEggEvaluator.evaluate(userId, match, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[P1] [AC1] should return false when player team scored at least 1 point in all games")
        void shouldReturnFalseWhenPlayerTeamScoredPointsInAllGames() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(1).teamBScore(10).build();
            var game2 = Game.builder().teamAScore(3).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2))
                    .build();

            var result = gooseEggEvaluator.evaluate(userId, match, null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("GENEROUS_HOST Evaluator (AC2)")
    class GenerousHostTests {

        @Test
        @DisplayName("[P0] [AC2] should return true when opponent team scored 10 or more points in a single game")
        void shouldReturnTrueWhenOpponentScoredTenOrMoreInSingleGame() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game = Game.builder().teamAScore(2).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game))
                    .build();

            var result = generousHostEvaluator.evaluate(userId, match, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[P1] [AC2] should return false when opponent conceded fewer than 10 points in each game")
        void shouldReturnFalseWhenOpponentScoredLessThanTenInAllGames() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(8).teamBScore(9).build();
            var game2 = Game.builder().teamAScore(7).teamBScore(9).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2))
                    .build();

            var result = generousHostEvaluator.evaluate(userId, match, null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("SIEVE_DEFENSE Evaluator (AC3)")
    class SieveDefenseTests {

        @Test
        @DisplayName("[P0] [AC3] should return true when player is Defender and conceded 15 or more goals across match")
        void shouldReturnTrueWhenDefenderConcededFifteenOrMoreGoals() {
            var defenderId = UUID.randomUUID();
            var attackerId = UUID.randomUUID();
            var oppAttackerId = UUID.randomUUID();
            var oppDefenderId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(10).teamBScore(8).build();
            var game2 = Game.builder().teamAScore(7).teamBScore(10).build();
            var game3 = Game.builder().teamAScore(8).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(attackerId)
                    .teamADefenderId(defenderId)
                    .teamBAttackerId(oppAttackerId)
                    .teamBDefenderId(oppDefenderId)
                    .games(List.of(game1, game2, game3))
                    .build();

            var result = sieveDefenseEvaluator.evaluate(defenderId, match, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[P1] [AC3] should return false when player is Attacker even if 15 goals conceded")
        void shouldReturnFalseWhenPlayerIsAttackerNotDefender() {
            var attackerId = UUID.randomUUID();
            var defenderId = UUID.randomUUID();
            var oppAttackerId = UUID.randomUUID();
            var oppDefenderId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(5).teamBScore(10).build();
            var game2 = Game.builder().teamAScore(5).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(attackerId)
                    .teamADefenderId(defenderId)
                    .teamBAttackerId(oppAttackerId)
                    .teamBDefenderId(oppDefenderId)
                    .games(List.of(game1, game2))
                    .build();

            var result = sieveDefenseEvaluator.evaluate(attackerId, match, null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("[P1] [AC3] should return false when Defender conceded fewer than 15 goals")
        void shouldReturnFalseWhenDefenderConcededUnderFifteenGoals() {
            var defenderId = UUID.randomUUID();
            var attackerId = UUID.randomUUID();
            var oppAttackerId = UUID.randomUUID();
            var oppDefenderId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(10).teamBScore(7).build();
            var game2 = Game.builder().teamAScore(10).teamBScore(7).build();
            var match = Match.builder()
                    .teamAAttackerId(attackerId)
                    .teamADefenderId(defenderId)
                    .teamBAttackerId(oppAttackerId)
                    .teamBDefenderId(oppDefenderId)
                    .games(List.of(game1, game2))
                    .build();

            var result = sieveDefenseEvaluator.evaluate(defenderId, match, null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("HEARTBREAKER Evaluator (AC4)")
    class HeartbreakerTests {

        @Test
        @DisplayName("[P0] [AC4] should return true when player team lost deciding game by exactly 1 goal")
        void shouldReturnTrueWhenPlayerLostDecidingGameByOneGoal() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(10).teamBScore(5).build();
            var game2 = Game.builder().teamAScore(5).teamBScore(10).build();
            var game3 = Game.builder().teamAScore(9).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2, game3))
                    .build();

            var result = heartbreakerEvaluator.evaluate(userId, match, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[P1] [AC4] should return false when player team won the match")
        void shouldReturnFalseWhenPlayerTeamWonMatch() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(10).teamBScore(9).build();
            var game2 = Game.builder().teamAScore(10).teamBScore(8).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2))
                    .build();

            var result = heartbreakerEvaluator.evaluate(userId, match, null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("[P1] [AC4] should return false when player team lost deciding game by 2 or more goals")
        void shouldReturnFalseWhenPlayerLostDecidingGameByTwoOrMoreGoals() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(10).teamBScore(5).build();
            var game2 = Game.builder().teamAScore(5).teamBScore(10).build();
            var game3 = Game.builder().teamAScore(8).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2, game3))
                    .build();

            var result = heartbreakerEvaluator.evaluate(userId, match, null);

            assertThat(result).isFalse();
        }
    }
}
