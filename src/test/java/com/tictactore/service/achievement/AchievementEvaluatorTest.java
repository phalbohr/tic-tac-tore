package com.tictactore.service.achievement;

import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.service.achievement.evaluator.CleanSheetEvaluator;
import com.tictactore.service.achievement.evaluator.DefenseWallEvaluator;
import com.tictactore.service.achievement.evaluator.FirstWinEvaluator;
import com.tictactore.service.achievement.evaluator.GenerousHostEvaluator;
import com.tictactore.service.achievement.evaluator.GooseEggEvaluator;
import com.tictactore.service.achievement.evaluator.HeartbreakerEvaluator;
import com.tictactore.service.achievement.evaluator.MatchesPlayedEvaluator;
import com.tictactore.service.achievement.evaluator.SieveDefenseEvaluator;
import com.tictactore.service.achievement.evaluator.StrikerGoalsEvaluator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Achievement Evaluator Unit Tests")
class AchievementEvaluatorTest {

    private final FirstWinEvaluator firstWinEvaluator = new FirstWinEvaluator();
    private final MatchesPlayedEvaluator matchesPlayedEvaluator = new MatchesPlayedEvaluator();
    private final CleanSheetEvaluator cleanSheetEvaluator = new CleanSheetEvaluator();
    private final StrikerGoalsEvaluator strikerGoalsEvaluator = new StrikerGoalsEvaluator();
    private final DefenseWallEvaluator defenseWallEvaluator = new DefenseWallEvaluator();
    private final GooseEggEvaluator gooseEggEvaluator = new GooseEggEvaluator();
    private final GenerousHostEvaluator generousHostEvaluator = new GenerousHostEvaluator();
    private final SieveDefenseEvaluator sieveDefenseEvaluator = new SieveDefenseEvaluator();
    private final HeartbreakerEvaluator heartbreakerEvaluator = new HeartbreakerEvaluator();

    @Nested
    @DisplayName("FirstWinEvaluator")
    class FirstWinTests {

        @Test
        @DisplayName("should return true when player has at least one historical win")
        void shouldReturnTrueWhenPlayerHasHistoricalWin() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 5, 1, 10, 2);

            var result = firstWinEvaluator.evaluate(userId, null, stats);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return true when player won the current match")
        void shouldReturnTrueWhenPlayerWonCurrentMatch() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game = Game.builder().teamAScore(10).teamBScore(5).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game))
                    .build();
            var stats = new PlayerStatsContext(userId, 0, 0, 0, 0);

            var result = firstWinEvaluator.evaluate(userId, match, stats);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when player lost and has zero historical wins")
        void shouldReturnFalseWhenPlayerLost() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game = Game.builder().teamAScore(3).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game))
                    .build();
            var stats = new PlayerStatsContext(userId, 1, 0, 3, 0);

            var result = firstWinEvaluator.evaluate(userId, match, stats);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("MatchesPlayedEvaluator")
    class MatchesPlayedTests {

        @Test
        @DisplayName("should return true when player participated in 10 or more matches")
        void shouldReturnTrueWhenMatchesGte10() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 10, 5, 20, 4);

            var result = matchesPlayedEvaluator.evaluate(userId, null, stats);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when player has played fewer than 10 matches")
        void shouldReturnFalseWhenMatchesLt10() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 9, 3, 15, 2);

            var result = matchesPlayedEvaluator.evaluate(userId, null, stats);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("CleanSheetEvaluator")
    class CleanSheetTests {

        @Test
        @DisplayName("should return true when winning team conceded zero goals across all games")
        void shouldReturnTrueWhenConcededZeroGoals() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(5).teamBScore(0).build();
            var game2 = Game.builder().teamAScore(5).teamBScore(0).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2))
                    .build();

            var result = cleanSheetEvaluator.evaluate(userId, match, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when opponent scored at least one goal")
        void shouldReturnFalseWhenOpponentScored() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().teamAScore(5).teamBScore(1).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1))
                    .build();

            var result = cleanSheetEvaluator.evaluate(userId, match, null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("StrikerGoalsEvaluator")
    class StrikerGoalsTests {

        @Test
        @DisplayName("should return true when total attacker goals are at least 50")
        void shouldReturnTrueWhenGoalsGte50() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 15, 10, 50, 2);

            var result = strikerGoalsEvaluator.evaluate(userId, null, stats);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when total attacker goals are under 50")
        void shouldReturnFalseWhenGoalsLt50() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 15, 10, 49, 2);

            var result = strikerGoalsEvaluator.evaluate(userId, null, stats);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("DefenseWallEvaluator")
    class DefenseWallTests {

        @Test
        @DisplayName("should return true when player played at least 10 matches as defender")
        void shouldReturnTrueWhenDefenderMatchesGte10() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 12, 6, 10, 10);

            var result = defenseWallEvaluator.evaluate(userId, null, stats);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when defender matches are under 10")
        void shouldReturnFalseWhenDefenderMatchesLt10() {
            var userId = UUID.randomUUID();
            var stats = new PlayerStatsContext(userId, 12, 6, 10, 9);

            var result = defenseWallEvaluator.evaluate(userId, null, stats);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("GooseEggEvaluator")
    class GooseEggTests {

        @Test
        @DisplayName("should return true when player team scored 0 points in any game and opponent scored points")
        void shouldReturnTrueWhenPlayerTeamScoredZeroInAnyGame() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(10).teamBScore(5).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(0).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2))
                    .build();

            var result = gooseEggEvaluator.evaluate(userId, match, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return true when player is on Team B and scored 0 points")
        void shouldReturnTrueWhenPlayerOnTeamBScoredZero() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(10).teamBScore(0).build();
            var match = Match.builder()
                    .teamAAttackerId(oppId)
                    .teamBAttackerId(userId)
                    .games(List.of(game1))
                    .build();

            var result = gooseEggEvaluator.evaluate(userId, match, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when player team scored at least 1 point in all games")
        void shouldReturnFalseWhenPlayerTeamScoredPointsInAllGames() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(1).teamBScore(10).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(3).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2))
                    .build();

            var result = gooseEggEvaluator.evaluate(userId, match, null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when player team won with opponent scoring 0")
        void shouldReturnFalseWhenPlayerTeamWonWithOpponentZero() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(10).teamBScore(0).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1))
                    .build();

            var result = gooseEggEvaluator.evaluate(userId, match, null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when both teams scored 0 (unplayed/draw)")
        void shouldReturnFalseWhenBothTeamsScoredZero() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(0).teamBScore(0).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1))
                    .build();

            var result = gooseEggEvaluator.evaluate(userId, match, null);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("GenerousHostEvaluator")
    class GenerousHostTests {

        @Test
        @DisplayName("should return true when opponent team scored 10 or more points in a single game")
        void shouldReturnTrueWhenOpponentScoredTenOrMoreInSingleGame() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game = Game.builder().gameOrder(1).teamAScore(2).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game))
                    .build();

            var result = generousHostEvaluator.evaluate(userId, match, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when opponent conceded fewer than 10 points in each game")
        void shouldReturnFalseWhenOpponentScoredLessThanTenInAllGames() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(8).teamBScore(9).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(7).teamBScore(9).build();
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
    @DisplayName("SieveDefenseEvaluator")
    class SieveDefenseTests {

        @Test
        @DisplayName("should return true when player is Defender and conceded 15 or more goals across match")
        void shouldReturnTrueWhenDefenderConcededFifteenOrMoreGoals() {
            var defenderId = UUID.randomUUID();
            var attackerId = UUID.randomUUID();
            var oppAttackerId = UUID.randomUUID();
            var oppDefenderId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(10).teamBScore(8).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(7).teamBScore(10).build();
            var game3 = Game.builder().gameOrder(3).teamAScore(8).teamBScore(10).build();
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
        @DisplayName("should return false when player is Attacker even if 15 goals conceded")
        void shouldReturnFalseWhenPlayerIsAttackerNotDefender() {
            var attackerId = UUID.randomUUID();
            var defenderId = UUID.randomUUID();
            var oppAttackerId = UUID.randomUUID();
            var oppDefenderId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(5).teamBScore(10).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(5).teamBScore(10).build();
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
        @DisplayName("should return false when Defender conceded fewer than 15 goals")
        void shouldReturnFalseWhenDefenderConcededUnderFifteenGoals() {
            var defenderId = UUID.randomUUID();
            var attackerId = UUID.randomUUID();
            var oppAttackerId = UUID.randomUUID();
            var oppDefenderId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(10).teamBScore(7).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(10).teamBScore(7).build();
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
    @DisplayName("HeartbreakerEvaluator")
    class HeartbreakerTests {

        @Test
        @DisplayName("should return true when player team lost deciding game by exactly 1 goal")
        void shouldReturnTrueWhenPlayerLostDecidingGameByOneGoal() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(10).teamBScore(5).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(5).teamBScore(10).build();
            var game3 = Game.builder().gameOrder(3).teamAScore(9).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2, game3))
                    .build();

            var result = heartbreakerEvaluator.evaluate(userId, match, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return true when games in match are not in list index order but ordered by gameOrder")
        void shouldReturnTrueWhenGamesOutOfOrderInList() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(10).teamBScore(5).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(5).teamBScore(10).build();
            var game3 = Game.builder().gameOrder(3).teamAScore(9).teamBScore(10).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game3, game1, game2))
                    .build();

            var result = heartbreakerEvaluator.evaluate(userId, match, null);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when player team won the match")
        void shouldReturnFalseWhenPlayerTeamWonMatch() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(10).teamBScore(9).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(10).teamBScore(8).build();
            var match = Match.builder()
                    .teamAAttackerId(userId)
                    .teamBAttackerId(oppId)
                    .games(List.of(game1, game2))
                    .build();

            var result = heartbreakerEvaluator.evaluate(userId, match, null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should return false when player team lost deciding game by 2 or more goals")
        void shouldReturnFalseWhenPlayerLostDecidingGameByTwoOrMoreGoals() {
            var userId = UUID.randomUUID();
            var oppId = UUID.randomUUID();
            var game1 = Game.builder().gameOrder(1).teamAScore(10).teamBScore(5).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(5).teamBScore(10).build();
            var game3 = Game.builder().gameOrder(3).teamAScore(8).teamBScore(10).build();
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
