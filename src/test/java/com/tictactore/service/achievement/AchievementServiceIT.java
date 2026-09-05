package com.tictactore.service.achievement;

import com.tictactore.model.Achievement;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.AchievementRepository;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.PlayerAchievementRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.AchievementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@DisplayName("AchievementService Integration Tests — Story 7.2 Anti-Achievements")
class AchievementServiceIT {

    @MockBean
    private org.redisson.api.RedissonClient redissonClient;

    @MockBean
    private com.tictactore.service.TokenRevocationService tokenRevocationService;

    @Autowired
    private AchievementService achievementService;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private PlayerAchievementRepository playerAchievementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    private User playerA;
    private User playerADefender;
    private User playerB;
    private User playerBDefender;

    @BeforeEach
    void setUp() {
        playerA = userRepository.save(User.builder().email("playera@example.com").nickname("PlayerA").build());
        playerADefender = userRepository
                .save(User.builder().email("playeradef@example.com").nickname("PlayerADef").build());
        playerB = userRepository.save(User.builder().email("playerb@example.com").nickname("PlayerB").build());
        playerBDefender = userRepository
                .save(User.builder().email("playerbdef@example.com").nickname("PlayerBDef").build());

        seedAntiAchievementsIfMissing();
    }

    private void seedAntiAchievementsIfMissing() {
        if (achievementRepository.findByCode("GOOSE_EGG").isEmpty()) {
            achievementRepository.save(Achievement.builder().code("GOOSE_EGG").category("ANTI_ACHIEVEMENT")
                    .nameKey("achievements.goose_egg.title").descriptionKey("achievements.goose_egg.description")
                    .icon("egg").build());
        }
        if (achievementRepository.findByCode("GENEROUS_HOST").isEmpty()) {
            achievementRepository.save(Achievement.builder().code("GENEROUS_HOST").category("ANTI_ACHIEVEMENT")
                    .nameKey("achievements.generous_host.title")
                    .descriptionKey("achievements.generous_host.description").icon("volunteer_activism").build());
        }
        if (achievementRepository.findByCode("SIEVE_DEFENSE").isEmpty()) {
            achievementRepository.save(Achievement.builder().code("SIEVE_DEFENSE").category("ANTI_ACHIEVEMENT")
                    .nameKey("achievements.sieve_defense.title")
                    .descriptionKey("achievements.sieve_defense.description").icon("water_drop").build());
        }
        if (achievementRepository.findByCode("HEARTBREAKER").isEmpty()) {
            achievementRepository.save(Achievement.builder().code("HEARTBREAKER").category("ANTI_ACHIEVEMENT")
                    .nameKey("achievements.heartbreaker.title").descriptionKey("achievements.heartbreaker.description")
                    .icon("heart_broken").build());
        }
    }

    @Nested
    @DisplayName("Anti-Achievement Evaluation Tests")
    class AntiAchievementSpecs {

        @Test
        @DisplayName("[P0] [AC1] should award GOOSE_EGG when player team lost any game with 0 points scored")
        void shouldAwardGooseEgg_whenPlayerTeamScoredZeroInGame() {
            var game = Game.builder()
                    .gameOrder(1)
                    .teamAScore(0)
                    .teamBScore(10)
                    .teamAAttackerId(playerA.getId())
                    .teamBAttackerId(playerB.getId())
                    .build();
            var match = matchRepository.save(Match.builder()
                    .creatorId(playerA.getId())
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .createdAt(Instant.now())
                    .teamAAttackerId(playerA.getId())
                    .teamBAttackerId(playerB.getId())
                    .status("CONFIRMED")
                    .games(List.of(game))
                    .build());
            game.setMatch(match);

            achievementService.evaluateMatchAchievements(match.getId(), List.of(playerA.getId(), playerB.getId()));

            var playerAAchievements = playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(playerA.getId());
            var playerBAchievements = playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(playerB.getId());

            assertThat(playerAAchievements).extracting(pa -> pa.getAchievement().getCode()).contains("GOOSE_EGG");
            assertThat(playerBAchievements).extracting(pa -> pa.getAchievement().getCode()).doesNotContain("GOOSE_EGG");
        }

        @Test
        @DisplayName("[P0] [AC2] should award GENEROUS_HOST when opponent scored 10 or more points in a single game")
        void shouldAwardGenerousHost_whenOpponentScoredTenOrMore() {
            var game = Game.builder()
                    .gameOrder(1)
                    .teamAScore(3)
                    .teamBScore(10)
                    .teamAAttackerId(playerA.getId())
                    .teamBAttackerId(playerB.getId())
                    .build();
            var match = matchRepository.save(Match.builder()
                    .creatorId(playerA.getId())
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .createdAt(Instant.now())
                    .teamAAttackerId(playerA.getId())
                    .teamBAttackerId(playerB.getId())
                    .status("CONFIRMED")
                    .games(List.of(game))
                    .build());
            game.setMatch(match);

            achievementService.evaluateMatchAchievements(match.getId(), List.of(playerA.getId()));

            var playerAAchievements = playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(playerA.getId());

            assertThat(playerAAchievements).extracting(pa -> pa.getAchievement().getCode()).contains("GENEROUS_HOST");
        }

        @Test
        @DisplayName("[P0] [AC3] should award SIEVE_DEFENSE to defender conceding 15+ goals across match")
        void shouldAwardSieveDefense_whenDefenderConcededFifteenOrMoreGoals() {
            var game1 = Game.builder().gameOrder(1).teamAScore(10).teamBScore(8).teamAAttackerId(playerA.getId())
                    .teamADefenderId(playerADefender.getId()).teamBAttackerId(playerB.getId())
                    .teamBDefenderId(playerBDefender.getId()).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(5).teamBScore(10).teamAAttackerId(playerA.getId())
                    .teamADefenderId(playerADefender.getId()).teamBAttackerId(playerB.getId())
                    .teamBDefenderId(playerBDefender.getId()).build();
            var game3 = Game.builder().gameOrder(3).teamAScore(8).teamBScore(10).teamAAttackerId(playerA.getId())
                    .teamADefenderId(playerADefender.getId()).teamBAttackerId(playerB.getId())
                    .teamBDefenderId(playerBDefender.getId()).build();

            var match = matchRepository.save(Match.builder()
                    .creatorId(playerA.getId())
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .createdAt(Instant.now())
                    .teamAAttackerId(playerA.getId())
                    .teamADefenderId(playerADefender.getId())
                    .teamBAttackerId(playerB.getId())
                    .teamBDefenderId(playerBDefender.getId())
                    .status("CONFIRMED")
                    .games(List.of(game1, game2, game3))
                    .build());
            game1.setMatch(match);
            game2.setMatch(match);
            game3.setMatch(match);

            achievementService.evaluateMatchAchievements(match.getId(),
                    List.of(playerA.getId(), playerADefender.getId()));

            var defenderAchievements = playerAchievementRepository
                    .findByUserIdOrderByUnlockedAtDesc(playerADefender.getId());
            var attackerAchievements = playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(playerA.getId());

            assertThat(defenderAchievements).extracting(pa -> pa.getAchievement().getCode()).contains("SIEVE_DEFENSE");
            assertThat(attackerAchievements).extracting(pa -> pa.getAchievement().getCode())
                    .doesNotContain("SIEVE_DEFENSE");
        }

        @Test
        @DisplayName("[P0] [AC4] should award HEARTBREAKER when player team lost deciding game by exactly 1 goal")
        void shouldAwardHeartbreaker_whenPlayerLostDecidingGameByOneGoal() {
            var game1 = Game.builder().gameOrder(1).teamAScore(10).teamBScore(5).teamAAttackerId(playerA.getId())
                    .teamBAttackerId(playerB.getId()).build();
            var game2 = Game.builder().gameOrder(2).teamAScore(5).teamBScore(10).teamAAttackerId(playerA.getId())
                    .teamBAttackerId(playerB.getId()).build();
            var game3 = Game.builder().gameOrder(3).teamAScore(9).teamBScore(10).teamAAttackerId(playerA.getId())
                    .teamBAttackerId(playerB.getId()).build();

            var match = matchRepository.save(Match.builder()
                    .creatorId(playerA.getId())
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .createdAt(Instant.now())
                    .teamAAttackerId(playerA.getId())
                    .teamBAttackerId(playerB.getId())
                    .status("CONFIRMED")
                    .games(List.of(game1, game2, game3))
                    .build());
            game1.setMatch(match);
            game2.setMatch(match);
            game3.setMatch(match);

            achievementService.evaluateMatchAchievements(match.getId(), List.of(playerA.getId(), playerB.getId()));

            var playerAAchievements = playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(playerA.getId());
            var playerBAchievements = playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(playerB.getId());

            assertThat(playerAAchievements).extracting(pa -> pa.getAchievement().getCode()).contains("HEARTBREAKER");
            assertThat(playerBAchievements).extracting(pa -> pa.getAchievement().getCode())
                    .doesNotContain("HEARTBREAKER");
        }

        @Test
        @DisplayName("[P1] [AC1-AC4] should be idempotent when evaluated multiple times")
        void shouldBeIdempotent_whenEvaluatedMultipleTimes() {
            var game = Game.builder()
                    .gameOrder(1)
                    .teamAScore(0)
                    .teamBScore(10)
                    .teamAAttackerId(playerA.getId())
                    .teamBAttackerId(playerB.getId())
                    .build();
            var match = matchRepository.save(Match.builder()
                    .creatorId(playerA.getId())
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .createdAt(Instant.now())
                    .teamAAttackerId(playerA.getId())
                    .teamBAttackerId(playerB.getId())
                    .status("CONFIRMED")
                    .games(List.of(game))
                    .build());
            game.setMatch(match);

            achievementService.evaluateMatchAchievements(match.getId(), List.of(playerA.getId()));
            achievementService.evaluateMatchAchievements(match.getId(), List.of(playerA.getId()));

            var playerAAchievements = playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(playerA.getId());

            assertThat(playerAAchievements.stream().filter(pa -> pa.getAchievement().getCode().equals("GOOSE_EGG"))
                    .count()).isEqualTo(1);
        }
    }
}
