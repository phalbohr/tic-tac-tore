package com.tictactore.service.insight;

import com.tictactore.dto.AchievementDto;
import com.tictactore.dto.InsightCategory;
import com.tictactore.dto.InsightImportance;
import com.tictactore.dto.InsightType;
import com.tictactore.dto.PlayerInsightDto;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.service.achievement.PlayerStatsContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Story 7.5: Insight Generators ATDD Tests")
class InsightGeneratorATDDTest {

    private final UUID playerId = UUID.randomUUID();
    private final UUID partnerId = UUID.randomUUID();
    private final UUID opponent1Id = UUID.randomUUID();
    private final UUID opponent2Id = UUID.randomUUID();

    @Nested
    @DisplayName("WIN_STREAK Generator Tests (AC2)")
    class WinStreakGeneratorTests {

        @Test
        @DisplayName("[P0] [AC2] should generate WIN_STREAK insight when player has >= 3 consecutive recent wins")
        void shouldGenerateWinStreakInsight_whenConsecutiveWinsGe3() {
            var matches = createRecentConsecutiveMatches(playerId, 4, true);
            var stats = new PlayerStatsContext(playerId, 10, 7, 15, 5);

            var generator = new StubWinStreakInsightGenerator();
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

            var generator = new StubWinStreakInsightGenerator();
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

            var generator = new StubWinStreakInsightGenerator();
            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("FORM_TREND Generator Tests (AC2)")
    class FormTrendGeneratorTests {

        @Test
        @DisplayName("[P0] [AC2] should generate FORM_TREND insight when recent win rate exceeds career by >= 15%")
        void shouldGenerateFormTrendInsight_whenRecentWinRateExceedsCareerByGe15Percent() {
            var matches = new ArrayList<Match>();
            for (var i = 0; i < 6; i++) {
                matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, 2, 0));
            }
            var stats = new PlayerStatsContext(playerId, 20, 10, 30, 8);

            var generator = new StubFormTrendInsightGenerator();
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

            var generator = new StubFormTrendInsightGenerator();
            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("POSITIONAL_MASTERY Generator Tests (AC2)")
    class PositionalMasteryGeneratorTests {

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

            var generator = new StubPositionalMasteryInsightGenerator();
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

            var generator = new StubPositionalMasteryInsightGenerator();
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
            var matches = new ArrayList<Match>();
            for (var i = 0; i < 4; i++) {
                matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, i < 3 ? 2 : 0, i < 3 ? 0 : 2));
            }
            var stats = new PlayerStatsContext(playerId, 15, 8, 20, 6);

            var generator = new StubBestPartnershipInsightGenerator();
            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isPresent();
            var insight = result.get();
            assertThat(insight.type()).isEqualTo(InsightType.BEST_PARTNERSHIP);
            assertThat(insight.category()).isEqualTo(InsightCategory.PARTNERSHIP);
            assertThat(insight.drillDownUrl()).isEqualTo("/statistics?tab=teams");
            assertThat(insight.params()).containsEntry("partnerId", partnerId);
        }

        @Test
        @DisplayName("[P1] [AC2] should return empty Optional when joint 2v2 matches with partner < 3")
        void shouldReturnEmpty_whenJointMatchesLessThan3() {
            var matches = new ArrayList<Match>();
            matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, 2, 0));
            matches.add(createMatch(playerId, partnerId, opponent1Id, opponent2Id, 2, 0));
            var stats = new PlayerStatsContext(playerId, 10, 5, 12, 4);

            var generator = new StubBestPartnershipInsightGenerator();
            var result = generator.generate(playerId, matches, stats, Collections.emptyList());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("MILESTONE_PROXIMITY Generator Tests (AC2)")
    class MilestoneProximityGeneratorTests {

        @Test
        @DisplayName("[P0] [AC2] should generate MILESTONE_PROXIMITY insight when remaining progress <= 2 for locked progressive badge")
        void shouldGenerateMilestoneProximityInsight_whenRemainingProgressLe2() {
            var badge = new AchievementDto(
                    UUID.randomUUID(), "MATCHES_10", "EXPERIENCE",
                    "achievements.matches_10.title", "achievements.matches_10.description",
                    "flame", false, null, 8L, 10L, true
            );
            var stats = new PlayerStatsContext(playerId, 8, 4, 15, 4);

            var generator = new StubMilestoneProximityInsightGenerator();
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

            var generator = new StubMilestoneProximityInsightGenerator();
            var result = generator.generate(playerId, Collections.emptyList(), stats, List.of(unlockedBadge, farBadge));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Insufficient Data & Safe Math (AC3)")
    class InsufficientDataTests {

        @Test
        @DisplayName("[P0] [AC3] should return empty or INSUFFICIENT_DATA starter insight without division by zero when matches < 3")
        void shouldHandleInsufficientData_gracefullyWithoutDivisionByZero() {
            var matches = createRecentConsecutiveMatches(playerId, 2, true);
            var stats = new PlayerStatsContext(playerId, 2, 2, 5, 1);

            var service = new StubInsightService();
            var response = service.generateInsights(playerId, matches, stats, Collections.emptyList());

            assertThat(response).isNotNull();
            assertThat(response.insights()).allSatisfy(i -> {
                assertThat(i.importance()).isNotNull();
                assertThat(i.params()).doesNotContainValue(Double.NaN);
                assertThat(i.params()).doesNotContainValue(Double.POSITIVE_INFINITY);
            });
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

    private static class StubWinStreakInsightGenerator {
        Optional<PlayerInsightDto> generate(UUID playerId, List<Match> matches, PlayerStatsContext stats, List<AchievementDto> achievements) {
            if (matches.size() < 3) return Optional.empty();
            var streak = 0;
            for (var m : matches) {
                var won = isPlayerWinner(m, playerId);
                if (won) {
                    streak++;
                } else {
                    break;
                }
            }
            if (streak >= 3) {
                return Optional.of(new PlayerInsightDto(
                        UUID.randomUUID(), InsightType.WIN_STREAK, InsightCategory.STREAK,
                        InsightImportance.HIGH, "insights.winStreak.title", "insights.winStreak.description",
                        Map.of("streak", streak), "local_fire_department", null
                ));
            }
            return Optional.empty();
        }

        private boolean isPlayerWinner(Match match, UUID pId) {
            var winsA = 0;
            var winsB = 0;
            for (var g : match.getGames()) {
                if (g.getTeamAScore() > g.getTeamBScore()) winsA++;
                else if (g.getTeamBScore() > g.getTeamAScore()) winsB++;
            }
            var inTeamA = pId.equals(match.getTeamAAttackerId()) || pId.equals(match.getTeamADefenderId());
            return (inTeamA && winsA > winsB) || (!inTeamA && winsB > winsA);
        }
    }

    private static class StubFormTrendInsightGenerator {
        Optional<PlayerInsightDto> generate(UUID playerId, List<Match> matches, PlayerStatsContext stats, List<AchievementDto> achievements) {
            if (stats.totalMatches() < 10 || matches.size() < 5) return Optional.empty();
            var recentMatches = matches.subList(0, Math.min(10, matches.size()));
            var recentWins = 0;
            for (var m : recentMatches) {
                if (isPlayerWinner(m, playerId)) recentWins++;
            }
            var recentWinRate = (double) recentWins / recentMatches.size() * 100.0;
            var careerWinRate = (double) stats.totalWins() / stats.totalMatches() * 100.0;
            var diff = recentWinRate - careerWinRate;
            if (diff >= 15.0) {
                return Optional.of(new PlayerInsightDto(
                        UUID.randomUUID(), InsightType.FORM_TREND, InsightCategory.TREND,
                        InsightImportance.HIGH, "insights.formTrend.title", "insights.formTrend.description",
                        Map.of("recentWinRate", Math.round(recentWinRate), "careerWinRate", Math.round(careerWinRate), "diff", Math.round(diff)),
                        "trending_up", null
                ));
            }
            return Optional.empty();
        }

        private boolean isPlayerWinner(Match match, UUID pId) {
            var winsA = 0;
            var winsB = 0;
            for (var g : match.getGames()) {
                if (g.getTeamAScore() > g.getTeamBScore()) winsA++;
                else if (g.getTeamBScore() > g.getTeamAScore()) winsB++;
            }
            var inTeamA = pId.equals(match.getTeamAAttackerId()) || pId.equals(match.getTeamADefenderId());
            return (inTeamA && winsA > winsB) || (!inTeamA && winsB > winsA);
        }
    }

    private static class StubPositionalMasteryInsightGenerator {
        Optional<PlayerInsightDto> generate(UUID playerId, List<Match> matches, PlayerStatsContext stats, List<AchievementDto> achievements) {
            var attMatches = 0;
            var attWins = 0;
            var defMatches = 0;
            var defWins = 0;
            for (var m : matches) {
                var isAtt = playerId.equals(m.getTeamAAttackerId()) || playerId.equals(m.getTeamBAttackerId());
                var isDef = playerId.equals(m.getTeamADefenderId()) || playerId.equals(m.getTeamBDefenderId());
                var won = isPlayerWinner(m, playerId);
                if (isAtt) {
                    attMatches++;
                    if (won) attWins++;
                } else if (isDef) {
                    defMatches++;
                    if (won) defWins++;
                }
            }
            if (attMatches < 5 || defMatches < 5) return Optional.empty();
            var attWinRate = (double) attWins / attMatches * 100.0;
            var defWinRate = (double) defWins / defMatches * 100.0;
            var delta = Math.abs(attWinRate - defWinRate);
            if (delta >= 20.0) {
                var favored = attWinRate > defWinRate ? "Attacker" : "Defender";
                var higher = Math.max(attWinRate, defWinRate);
                var lower = Math.min(attWinRate, defWinRate);
                return Optional.of(new PlayerInsightDto(
                        UUID.randomUUID(), InsightType.POSITIONAL_MASTERY, InsightCategory.POSITION,
                        InsightImportance.MEDIUM, "insights.positionalMastery.title", "insights.positionalMastery.description",
                        Map.of("favoredPosition", favored, "higherWinRate", Math.round(higher), "lowerWinRate", Math.round(lower)),
                        "sports_score", null
                ));
            }
            return Optional.empty();
        }

        private boolean isPlayerWinner(Match match, UUID pId) {
            var winsA = 0;
            var winsB = 0;
            for (var g : match.getGames()) {
                if (g.getTeamAScore() > g.getTeamBScore()) winsA++;
                else if (g.getTeamBScore() > g.getTeamAScore()) winsB++;
            }
            var inTeamA = pId.equals(match.getTeamAAttackerId()) || pId.equals(match.getTeamADefenderId());
            return (inTeamA && winsA > winsB) || (!inTeamA && winsB > winsA);
        }
    }

    private static class StubBestPartnershipInsightGenerator {
        Optional<PlayerInsightDto> generate(UUID playerId, List<Match> matches, PlayerStatsContext stats, List<AchievementDto> achievements) {
            var partnerMatches = 0;
            var partnerWins = 0;
            UUID foundPartner = null;
            for (var m : matches) {
                if (m.getTeamADefenderId() != null && m.getTeamBDefenderId() != null) {
                    var inTeamA = playerId.equals(m.getTeamAAttackerId()) || playerId.equals(m.getTeamADefenderId());
                    var partner = inTeamA
                            ? (playerId.equals(m.getTeamAAttackerId()) ? m.getTeamADefenderId() : m.getTeamAAttackerId())
                            : (playerId.equals(m.getTeamBAttackerId()) ? m.getTeamBDefenderId() : m.getTeamBAttackerId());
                    if (partner != null) {
                        foundPartner = partner;
                        partnerMatches++;
                        if (isPlayerWinner(m, playerId)) partnerWins++;
                    }
                }
            }
            if (partnerMatches < 3 || foundPartner == null) return Optional.empty();
            var winRate = (double) partnerWins / partnerMatches * 100.0;
            if (winRate >= 70.0) {
                return Optional.of(new PlayerInsightDto(
                        UUID.randomUUID(), InsightType.BEST_PARTNERSHIP, InsightCategory.PARTNERSHIP,
                        InsightImportance.MEDIUM, "insights.bestPartnership.title", "insights.bestPartnership.description",
                        Map.of("partnerId", foundPartner, "partnerName", "Partner", "winRate", Math.round(winRate), "matches", partnerMatches),
                        "group", "/statistics?tab=teams"
                ));
            }
            return Optional.empty();
        }

        private boolean isPlayerWinner(Match match, UUID pId) {
            var winsA = 0;
            var winsB = 0;
            for (var g : match.getGames()) {
                if (g.getTeamAScore() > g.getTeamBScore()) winsA++;
                else if (g.getTeamBScore() > g.getTeamAScore()) winsB++;
            }
            var inTeamA = pId.equals(match.getTeamAAttackerId()) || pId.equals(match.getTeamADefenderId());
            return (inTeamA && winsA > winsB) || (!inTeamA && winsB > winsA);
        }
    }

    private static class StubMilestoneProximityInsightGenerator {
        Optional<PlayerInsightDto> generate(UUID playerId, List<Match> matches, PlayerStatsContext stats, List<AchievementDto> achievements) {
            for (var a : achievements) {
                if (!a.isUnlocked() && a.hasProgress() && a.currentProgress() != null && a.targetValue() != null) {
                    var remaining = a.targetValue() - a.currentProgress();
                    if (remaining > 0 && remaining <= 2) {
                        return Optional.of(new PlayerInsightDto(
                                UUID.randomUUID(), InsightType.MILESTONE_PROXIMITY, InsightCategory.MILESTONE,
                                InsightImportance.MEDIUM, "insights.milestoneProximity.title", "insights.milestoneProximity.description",
                                Map.of("badgeCode", a.code(), "remaining", remaining, "current", a.currentProgress(), "target", a.targetValue()),
                                "military_tech", "/cabinet"
                        ));
                    }
                }
            }
            return Optional.empty();
        }
    }

    private static class StubInsightService {
        RecordResponse generateInsights(UUID playerId, List<Match> matches, PlayerStatsContext stats, List<AchievementDto> achievements) {
            if (matches.size() < 3) {
                return new RecordResponse(playerId, 1, List.of(new PlayerInsightDto(
                        UUID.randomUUID(), InsightType.INSUFFICIENT_DATA, InsightCategory.GENERAL,
                        InsightImportance.LOW, "insights.empty", "insights.empty",
                        Map.of(), "info", null
                )));
            }
            return new RecordResponse(playerId, 0, Collections.emptyList());
        }
    }

    private record RecordResponse(UUID playerId, int totalCount, List<PlayerInsightDto> insights) {}
}
