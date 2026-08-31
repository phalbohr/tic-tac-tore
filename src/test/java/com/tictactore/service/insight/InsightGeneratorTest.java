package com.tictactore.service.insight;

import com.tictactore.dto.AchievementDto;
import com.tictactore.dto.InsightCategory;
import com.tictactore.dto.InsightImportance;
import com.tictactore.dto.InsightType;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.achievement.PlayerStatsContext;
import com.tictactore.service.insight.generator.BestPartnershipInsightGenerator;
import com.tictactore.service.insight.generator.FormTrendInsightGenerator;
import com.tictactore.service.insight.generator.MilestoneProximityInsightGenerator;
import com.tictactore.service.insight.generator.PositionalMasteryInsightGenerator;
import com.tictactore.service.insight.generator.WinStreakInsightGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Story 7.5: Insight Generators Unit Tests")
class InsightGeneratorTest {

    private final UUID playerId = UUID.randomUUID();
    private final UUID partnerId = UUID.randomUUID();
    private final UUID opponent1Id = UUID.randomUUID();
    private final UUID opponent2Id = UUID.randomUUID();

    @Mock
    private UserRepository userRepository;

    @Nested
    @DisplayName("WIN_STREAK Generator Tests (AC2)")
    class WinStreakGeneratorTests {

        private final WinStreakInsightGenerator generator = new WinStreakInsightGenerator();

        @Test
        @DisplayName("[P0] [AC2] should generate WIN_STREAK insight when player has >= 3 consecutive recent wins")
        void shouldGenerateWinStreakInsight_whenConsecutiveWinsGe3() {
            var matches = createRecentConsecutiveMatches(playerId, 4, true);
            var stats = new PlayerStatsContext(playerId, 10, 7, 15, 5);

            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isPresent();
            var insight = result.get();
            assertThat(insight.type()).isEqualTo(InsightType.WIN_STREAK);
            assertThat(insight.category()).isEqualTo(InsightCategory.STREAK);
            assertThat(insight.importance()).isEqualTo(InsightImportance.HIGH);
            assertThat(insight.params()).containsEntry("streak", 4);
        }

        @Test
        @DisplayName("[P1] [AC2] should return empty Optional when win streak is less than 3")
        void shouldReturnEmpty_whenWinStreakLessThan3() {
            var matches = createRecentConsecutiveMatches(playerId, 2, true);
            var stats = new PlayerStatsContext(playerId, 10, 5, 10, 4);

            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("[P1] [AC2] should return empty Optional when recent match was a loss")
        void shouldReturnEmpty_whenRecentMatchWasLoss() {
            var matches = new ArrayList<Match>();
            matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, 1, 2));
            matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, 2, 0));
            matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, 2, 0));
            matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, 2, 0));
            var stats = new PlayerStatsContext(playerId, 10, 5, 10, 4);

            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("FORM_TREND Generator Tests (AC2)")
    class FormTrendGeneratorTests {

        private final FormTrendInsightGenerator generator = new FormTrendInsightGenerator();

        @Test
        @DisplayName("[P0] [AC2] should generate FORM_TREND insight when recent win rate exceeds career by >= 15%")
        void shouldGenerateFormTrendInsight_whenRecentWinRateExceedsCareerByGe15Percent() {
            var matches = new ArrayList<Match>();
            for (var i = 0; i < 6; i++) {
                matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, 2, 0));
            }
            var stats = new PlayerStatsContext(playerId, 20, 10, 30, 8);

            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isPresent();
            var insight = result.get();
            assertThat(insight.type()).isEqualTo(InsightType.FORM_TREND);
            assertThat(insight.category()).isEqualTo(InsightCategory.TREND);
            assertThat(insight.importance()).isEqualTo(InsightImportance.HIGH);
            assertThat(insight.params()).containsKey("diff");
        }

        @Test
        @DisplayName("[P1] [AC2] should return empty Optional when form trend delta is below 15%")
        void shouldReturnEmpty_whenFormTrendDeltaBelow15Percent() {
            var matches = new ArrayList<Match>();
            for (var i = 0; i < 5; i++) {
                matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, i < 3 ? 2 : 0, i < 3 ? 0 : 2));
            }
            var stats = new PlayerStatsContext(playerId, 20, 11, 25, 10);

            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("POSITIONAL_MASTERY Generator Tests (AC2)")
    class PositionalMasteryGeneratorTests {

        private final PositionalMasteryInsightGenerator generator = new PositionalMasteryInsightGenerator();

        @Test
        @DisplayName("[P0] [AC2] should generate POSITIONAL_MASTERY insight when win rate delta >= 20% with min 5 matches each")
        void shouldGeneratePositionalMasteryInsight_whenDeltaGe20PercentAndMin5MatchesEach() {
            var matches = new ArrayList<Match>();
            for (var i = 0; i < 5; i++) {
                matches.add(createAttackerMatch(playerId, opponent1Id, true));
            }
            for (var i = 0; i < 5; i++) {
                matches.add(createDefenderMatch(playerId, partnerId, opponent1Id, opponent2Id, i < 2));
            }
            var stats = new PlayerStatsContext(playerId, 10, 7, 25, 5);

            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isPresent();
            var insight = result.get();
            assertThat(insight.type()).isEqualTo(InsightType.POSITIONAL_MASTERY);
            assertThat(insight.category()).isEqualTo(InsightCategory.POSITION);
            assertThat(insight.params()).containsEntry("favoredPosition", "Attacker");
        }

        @Test
        @DisplayName("[P1] [AC2] should return empty Optional when fewer than 5 matches played in one position")
        void shouldReturnEmpty_whenFewerThan5MatchesInPosition() {
            var matches = new ArrayList<Match>();
            for (var i = 0; i < 6; i++) {
                matches.add(createAttackerMatch(playerId, opponent1Id, true));
            }
            for (var i = 0; i < 3; i++) {
                matches.add(createDefenderMatch(playerId, partnerId, opponent1Id, opponent2Id, false));
            }
            var stats = new PlayerStatsContext(playerId, 9, 6, 20, 3);

            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("BEST_PARTNERSHIP Generator Tests (AC2)")
    class BestPartnershipGeneratorTests {

        @Test
        @DisplayName("[P0] [AC2] should generate BEST_PARTNERSHIP insight when 2v2 partner win rate >= 70% over >= 3 matches")
        void shouldGenerateBestPartnershipInsight_whenPartnerWinRateGe70PercentAndMatchesGe3() {
            var partnerUser = User.builder().id(partnerId).nickname("AceStriker").build();
            when(userRepository.findById(partnerId)).thenReturn(Optional.of(partnerUser));

            var generator = new BestPartnershipInsightGenerator(userRepository);
            var matches = new ArrayList<Match>();
            for (var i = 0; i < 4; i++) {
                matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, i < 3 ? 2 : 0, i < 3 ? 0 : 2));
            }
            var stats = new PlayerStatsContext(playerId, 15, 8, 20, 6);

            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isPresent();
            var insight = result.get();
            assertThat(insight.type()).isEqualTo(InsightType.BEST_PARTNERSHIP);
            assertThat(insight.category()).isEqualTo(InsightCategory.PARTNERSHIP);
            assertThat(insight.drillDownUrl()).isEqualTo("/statistics?tab=teams");
            assertThat(insight.params()).containsEntry("partnerId", partnerId);
            assertThat(insight.params()).containsEntry("partnerName", "AceStriker");
        }

        @Test
        @DisplayName("[P1] [AC2] should return empty Optional when joint 2v2 matches with partner < 3")
        void shouldReturnEmpty_whenJointMatchesLessThan3() {
            var generator = new BestPartnershipInsightGenerator(userRepository);
            var matches = new ArrayList<Match>();
            matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, 2, 0));
            matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, 2, 0));
            var stats = new PlayerStatsContext(playerId, 10, 5, 12, 4);

            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("MILESTONE_PROXIMITY Generator Tests (AC2)")
    class MilestoneProximityGeneratorTests {

        private final MilestoneProximityInsightGenerator generator = new MilestoneProximityInsightGenerator();

        @Test
        @DisplayName("[P0] [AC2] should generate MILESTONE_PROXIMITY insight when remaining progress <= 2 for locked progressive badge")
        void shouldGenerateMilestoneProximityInsight_whenRemainingProgressLe2() {
            var badge = new AchievementDto(
                    UUID.randomUUID(), "MATCHES_10", "EXPERIENCE",
                    "achievements.matches_10.title", "achievements.matches_10.description",
                    "flame", false, null, 8L, 10L, true
            );
            var stats = new PlayerStatsContext(playerId, 8, 4, 15, 4);

            var result = generator.generate(playerId, Collections.emptyList(), stats, List.of(badge));

            assertThat(result).isPresent();
            var insight = result.get();
            assertThat(insight.type()).isEqualTo(InsightType.MILESTONE_PROXIMITY);
            assertThat(insight.category()).isEqualTo(InsightCategory.MILESTONE);
            assertThat(insight.drillDownUrl()).isEqualTo("/cabinet");
            assertThat(insight.params()).containsEntry("badgeCode", "MATCHES_10");
            assertThat(insight.params()).containsEntry("remaining", 2L);
        }

        @Test
        @DisplayName("[P1] [AC2] should return empty Optional when badge is already unlocked or remaining > 2")
        void shouldReturnEmpty_whenBadgeUnlockedOrRemainingGt2() {
            var unlockedBadge = new AchievementDto(
                    UUID.randomUUID(), "MATCHES_10", "EXPERIENCE",
                    "achievements.matches_10.title", "achievements.matches_10.description",
                    "flame", true, null, 10L, 10L, true
            );
            var farBadge = new AchievementDto(
                    UUID.randomUUID(), "STRIKER_50", "SKILL",
                    "achievements.striker_50.title", "achievements.striker_50.description",
                    "target", false, null, 20L, 50L, true
            );
            var stats = new PlayerStatsContext(playerId, 10, 5, 20, 5);

            var result = generator.generate(playerId, Collections.emptyList(), stats, List.of(unlockedBadge, farBadge));

            assertThat(result).isEmpty();
        }
    }

    private List<Match> createRecentConsecutiveMatches(UUID pId, int count, boolean win) {
        var list = new ArrayList<Match>();
        for (var i = 0; i < count; i++) {
            list.add(createAttackerMatch(pId, opponent1Id, win));
        }
        return list;
    }

    private Match createAttackerMatch(UUID pId, UUID oppId, boolean win) {
        var match = Match.builder()
                .id(UUID.randomUUID())
                .creatorId(pId)
                .teamAAttackerId(pId)
                .teamBAttackerId(oppId)
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build();
        var game = Game.builder()
                .id(UUID.randomUUID())
                .gameOrder(1)
                .teamAScore(win ? 10 : 5)
                .teamBScore(win ? 5 : 10)
                .teamAAttackerId(pId)
                .teamBAttackerId(oppId)
                .build();
        match.addGame(game);
        return match;
    }

    private Match createDefenderMatch(UUID pId, UUID pPartnerId, UUID opp1Id, UUID opp2Id, boolean win) {
        var match = Match.builder()
                .id(UUID.randomUUID())
                .creatorId(pId)
                .teamAAttackerId(pPartnerId)
                .teamADefenderId(pId)
                .teamBAttackerId(opp1Id)
                .teamBDefenderId(opp2Id)
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build();
        var game = Game.builder()
                .id(UUID.randomUUID())
                .gameOrder(1)
                .teamAScore(win ? 10 : 5)
                .teamBScore(win ? 5 : 10)
                .teamAAttackerId(pPartnerId)
                .teamADefenderId(pId)
                .teamBAttackerId(opp1Id)
                .teamBDefenderId(opp2Id)
                .build();
        match.addGame(game);
        return match;
    }

    private Match createMatch(UUID pId, UUID pPartnerId, UUID opp1Id, UUID opp2Id, int scoreA, int scoreB) {
        var match = Match.builder()
                .id(UUID.randomUUID())
                .creatorId(pId)
                .teamAAttackerId(pId)
                .teamADefenderId(pPartnerId)
                .teamBAttackerId(opp1Id)
                .teamBDefenderId(opp2Id)
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build();
        var game = Game.builder()
                .id(UUID.randomUUID())
                .gameOrder(1)
                .teamAScore(scoreA)
                .teamBScore(scoreB)
                .teamAAttackerId(pId)
                .teamADefenderId(pPartnerId)
                .teamBAttackerId(opp1Id)
                .teamBDefenderId(opp2Id)
                .build();
        match.addGame(game);
        return match;
    }
}
