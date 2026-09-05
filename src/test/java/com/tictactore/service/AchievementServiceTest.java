package com.tictactore.service;

import com.tictactore.model.Achievement;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.PlayerAchievement;
import com.tictactore.model.User;
import com.tictactore.repository.AchievementRepository;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.PlayerAchievementRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.achievement.AchievementEvaluator;
import com.tictactore.service.achievement.PlayerStatsContext;
import com.tictactore.service.impl.AchievementServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AchievementService Unit Tests")
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private PlayerAchievementRepository playerAchievementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchRepository matchRepository;

    @Spy
    private List<AchievementEvaluator> evaluators = new ArrayList<>();

    @InjectMocks
    private AchievementServiceImpl achievementService;

    @Nested
    @DisplayName("getPlayerAchievements")
    class GetPlayerAchievementsTests {

        @Test
        @DisplayName("should return summary with unlocked and locked badges and progress info")
        void shouldReturnSummaryWithUnlockedAndLockedBadges() {
            var playerId = UUID.randomUUID();
            var badge1 = Achievement.builder()
                    .id(UUID.randomUUID())
                    .code("FIRST_WIN")
                    .category("MILESTONE")
                    .nameKey("achievements.first_win.title")
                    .descriptionKey("achievements.first_win.description")
                    .icon("trophy")
                    .build();
            var badge2 = Achievement.builder()
                    .id(UUID.randomUUID())
                    .code("MATCHES_10")
                    .category("EXPERIENCE")
                    .nameKey("achievements.matches_10.title")
                    .descriptionKey("achievements.matches_10.description")
                    .icon("flame")
                    .build();
            var playerAchievement = PlayerAchievement.builder()
                    .id(UUID.randomUUID())
                    .achievement(badge1)
                    .unlockedAt(Instant.now())
                    .build();

            evaluators.add(new AchievementEvaluator() {
                @Override
                public String getAchievementCode() {
                    return "FIRST_WIN";
                }

                @Override
                public boolean evaluate(UUID userId, Match match, PlayerStatsContext stats) {
                    return true;
                }

                @Override
                public com.tictactore.service.achievement.ProgressInfo getProgress(UUID userId,
                        PlayerStatsContext stats) {
                    return new com.tictactore.service.achievement.ProgressInfo(1, 1, true);
                }
            });

            evaluators.add(new AchievementEvaluator() {
                @Override
                public String getAchievementCode() {
                    return "MATCHES_10";
                }

                @Override
                public boolean evaluate(UUID userId, Match match, PlayerStatsContext stats) {
                    return false;
                }

                @Override
                public com.tictactore.service.achievement.ProgressInfo getProgress(UUID userId,
                        PlayerStatsContext stats) {
                    return new com.tictactore.service.achievement.ProgressInfo(4, 10, true);
                }
            });

            when(achievementRepository.findAll()).thenReturn(List.of(badge1, badge2));
            when(playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(playerId))
                    .thenReturn(List.of(playerAchievement));
            when(matchRepository.countConfirmedMatchesByPlayerId(playerId)).thenReturn(4L);
            when(matchRepository.countConfirmedMatchesAsDefender(playerId)).thenReturn(2L);
            when(matchRepository.sumGoalsAsAttacker(playerId)).thenReturn(8L);
            when(matchRepository.findConfirmedMatchesByPlayerId(playerId)).thenReturn(List.of());

            var response = achievementService.getPlayerAchievements(playerId);

            assertThat(response.playerId()).isEqualTo(playerId);
            assertThat(response.totalUnlocked()).isEqualTo(1);
            assertThat(response.totalAvailable()).isEqualTo(2);
            assertThat(response.achievements()).hasSize(2);
            var firstWinDto = response.achievements().stream().filter(a -> a.code().equals("FIRST_WIN")).findFirst()
                    .orElseThrow();
            assertThat(firstWinDto.isUnlocked()).isTrue();
            assertThat(firstWinDto.unlockedAt()).isNotNull();
            assertThat(firstWinDto.hasProgress()).isTrue();
            assertThat(firstWinDto.currentProgress()).isEqualTo(1L);
            assertThat(firstWinDto.targetValue()).isEqualTo(1L);
            var matches10Dto = response.achievements().stream().filter(a -> a.code().equals("MATCHES_10")).findFirst()
                    .orElseThrow();
            assertThat(matches10Dto.isUnlocked()).isFalse();
            assertThat(matches10Dto.unlockedAt()).isNull();
            assertThat(matches10Dto.hasProgress()).isTrue();
            assertThat(matches10Dto.currentProgress()).isEqualTo(4L);
            assertThat(matches10Dto.targetValue()).isEqualTo(10L);
        }

        @Test
        @DisplayName("should cap currentProgress at targetValue - 1 when locked badge has reached target threshold")
        void shouldCapCurrentProgressWhenLockedBadgeReachesTarget() {
            var playerId = UUID.randomUUID();
            var badge = Achievement.builder()
                    .id(UUID.randomUUID())
                    .code("MATCHES_10")
                    .category("EXPERIENCE")
                    .nameKey("achievements.matches_10.title")
                    .descriptionKey("achievements.matches_10.description")
                    .icon("flame")
                    .build();

            evaluators.add(new AchievementEvaluator() {
                @Override
                public String getAchievementCode() {
                    return "MATCHES_10";
                }

                @Override
                public boolean evaluate(UUID userId, Match match, PlayerStatsContext stats) {
                    return false;
                }

                @Override
                public com.tictactore.service.achievement.ProgressInfo getProgress(UUID userId,
                        PlayerStatsContext stats) {
                    return new com.tictactore.service.achievement.ProgressInfo(10, 10, true);
                }
            });

            when(achievementRepository.findAll()).thenReturn(List.of(badge));
            when(playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(playerId)).thenReturn(List.of());
            when(matchRepository.countConfirmedMatchesByPlayerId(playerId)).thenReturn(10L);
            when(matchRepository.countConfirmedMatchesAsDefender(playerId)).thenReturn(0L);
            when(matchRepository.sumGoalsAsAttacker(playerId)).thenReturn(0L);
            when(matchRepository.findConfirmedMatchesByPlayerId(playerId)).thenReturn(List.of());

            var response = achievementService.getPlayerAchievements(playerId);

            assertThat(response.achievements()).hasSize(1);
            var matches10Dto = response.achievements().get(0);
            assertThat(matches10Dto.isUnlocked()).isFalse();
            assertThat(matches10Dto.hasProgress()).isTrue();
            assertThat(matches10Dto.currentProgress()).isEqualTo(9L);
            assertThat(matches10Dto.targetValue()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("evaluateMatchAchievements")
    class EvaluateMatchAchievementsTests {

        @Test
        @DisplayName("should award achievement when evaluator returns true and not yet unlocked")
        void shouldAwardAchievementWhenEvaluatorReturnsTrue() {
            var matchId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var user = User.builder().id(userId).nickname("Player1").build();
            var badge = Achievement.builder()
                    .id(UUID.randomUUID())
                    .code("FIRST_WIN")
                    .build();
            var match = Match.builder()
                    .id(matchId)
                    .teamAAttackerId(userId)
                    .teamBAttackerId(UUID.randomUUID())
                    .status("CONFIRMED")
                    .games(List.of(Game.builder().teamAScore(5).teamBScore(0).build()))
                    .build();

            AchievementEvaluator mockEvaluator = new AchievementEvaluator() {
                @Override
                public String getAchievementCode() {
                    return "FIRST_WIN";
                }

                @Override
                public boolean evaluate(UUID uId, Match m, PlayerStatsContext stats) {
                    return true;
                }
            };
            evaluators.add(mockEvaluator);

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(achievementRepository.findAll()).thenReturn(List.of(badge));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(userId)).thenReturn(List.of());
            when(playerAchievementRepository.existsByUserIdAndAchievementId(userId, badge.getId())).thenReturn(false);

            achievementService.evaluateMatchAchievements(matchId, List.of(userId));

            verify(playerAchievementRepository).save(any(PlayerAchievement.class));
        }

        @Test
        @DisplayName("should not award achievement if already unlocked")
        void shouldNotAwardAchievementIfAlreadyUnlocked() {
            var matchId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var user = User.builder().id(userId).nickname("Player1").build();
            var badge = Achievement.builder()
                    .id(UUID.randomUUID())
                    .code("FIRST_WIN")
                    .build();
            var existing = PlayerAchievement.builder()
                    .id(UUID.randomUUID())
                    .achievement(badge)
                    .build();
            var match = Match.builder().id(matchId).build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(achievementRepository.findAll()).thenReturn(List.of(badge));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(userId)).thenReturn(List.of(existing));

            achievementService.evaluateMatchAchievements(matchId, List.of(userId));

            verify(playerAchievementRepository, never()).save(any(PlayerAchievement.class));
        }

        @Test
        @DisplayName("should handle DataIntegrityViolationException gracefully on concurrent insert")
        void shouldHandleDataIntegrityViolationExceptionGracefully() {
            var matchId = UUID.randomUUID();
            var userId = UUID.randomUUID();
            var user = User.builder().id(userId).nickname("Player1").build();
            var badge = Achievement.builder()
                    .id(UUID.randomUUID())
                    .code("FIRST_WIN")
                    .build();
            var match = Match.builder()
                    .id(matchId)
                    .teamAAttackerId(userId)
                    .teamBAttackerId(UUID.randomUUID())
                    .status("CONFIRMED")
                    .build();

            AchievementEvaluator mockEvaluator = new AchievementEvaluator() {
                @Override
                public String getAchievementCode() {
                    return "FIRST_WIN";
                }

                @Override
                public boolean evaluate(UUID uId, Match m, PlayerStatsContext stats) {
                    return true;
                }
            };
            evaluators.add(mockEvaluator);

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(achievementRepository.findAll()).thenReturn(List.of(badge));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(userId)).thenReturn(List.of());
            when(playerAchievementRepository.existsByUserIdAndAchievementId(userId, badge.getId())).thenReturn(false);
            when(playerAchievementRepository.save(any(PlayerAchievement.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate key"));

            achievementService.evaluateMatchAchievements(matchId, List.of(userId));

            verify(playerAchievementRepository).save(any(PlayerAchievement.class));
        }
    }
}
