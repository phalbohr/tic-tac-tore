package com.tictactore.repository;

import com.tictactore.model.Match;
import com.tictactore.model.PointDistribution;
import com.tictactore.model.PositionSwapRule;
import com.tictactore.model.RegistrationStatus;
import com.tictactore.model.RestartRule;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.model.SideSwapRule;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TournamentMatchRepository Deadline Query ATDD Tests")
class TournamentMatchRepositoryDeadlineTest {

    @Autowired
    private TournamentMatchRepository tournamentMatchRepository;

    @Autowired
    private TournamentRegistrationRepository registrationRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RuleConfigurationRepository ruleConfigurationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User player1;
    private User player2;
    private Tournament tournament;
    private TournamentRegistration reg1;
    private TournamentRegistration reg2;

    @BeforeEach
    void setUp() {
        player1 = userRepository.save(User.builder().email("p1@example.com").nickname("PlayerOne").build());
        player2 = userRepository.save(User.builder().email("p2@example.com").nickname("PlayerTwo").build());

        var ruleConfig = ruleConfigurationRepository.save(RuleConfiguration.builder()
                .name("Standard 5-Point")
                .type(RuleConfigurationType.PRESET)
                .goalLimit(5)
                .gameLimit(1)
                .winByTwo(false)
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
                .createdBy(player1.getId())
                .build());

        tournament = tournamentRepository.save(Tournament.builder()
                .name("Cup Tournament")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .ruleConfiguration(ruleConfig)
                .minParticipants(2)
                .maxParticipants(8)
                .registrationDeadline(Instant.now().plus(7, ChronoUnit.DAYS))
                .status(TournamentStatus.IN_PROGRESS)
                .creator(player1)
                .build());

        reg1 = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player1)
                .status(RegistrationStatus.CONFIRMED)
                .seed(1)
                .strengthScore(0.8)
                .build());

        reg2 = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player2)
                .status(RegistrationStatus.CONFIRMED)
                .seed(2)
                .strengthScore(0.6)
                .build());
    }

    @Test
    @DisplayName("[P0] Should find in-progress tournament match linked to expired pending core match")
    void shouldFindExpiredUnconfirmedMatches_whenMatchCreatedAtIsOlderThanDeadline() {
        var expiredMatch = entityManager.persist(Match.builder()
                .creatorId(player1.getId())
                .opponentId(player2.getId())
                .status(Match.STATUS_PENDING_APPROVAL)
                .createdAt(Instant.now().minus(49, ChronoUnit.HOURS))
                .build());
        var tournamentMatch = tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.IN_PROGRESS)
                .match(expiredMatch)
                .build());
        entityManager.flush();
        entityManager.clear();

        var deadline = Instant.now().minus(48, ChronoUnit.HOURS);
        var results = tournamentMatchRepository.findExpiredUnconfirmedMatches(
                TournamentStatus.IN_PROGRESS,
                TournamentMatchStatus.IN_PROGRESS,
                Match.STATUS_PENDING_APPROVAL,
                Match.STATUS_PARTIALLY_CONFIRMED,
                deadline
        );

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(tournamentMatch.getId());
    }

    @Test
    @DisplayName("[P0] Should ignore tournament match if core match createdAt is within deadline window")
    void shouldNotFindMatch_whenMatchCreatedAtIsWithinDeadline() {
        var freshMatch = entityManager.persist(Match.builder()
                .creatorId(player1.getId())
                .opponentId(player2.getId())
                .status(Match.STATUS_PENDING_APPROVAL)
                .createdAt(Instant.now().minus(10, ChronoUnit.HOURS))
                .build());
        tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.IN_PROGRESS)
                .match(freshMatch)
                .build());
        entityManager.flush();
        entityManager.clear();

        var deadline = Instant.now().minus(48, ChronoUnit.HOURS);
        var results = tournamentMatchRepository.findExpiredUnconfirmedMatches(
                TournamentStatus.IN_PROGRESS,
                TournamentMatchStatus.IN_PROGRESS,
                Match.STATUS_PENDING_APPROVAL,
                Match.STATUS_PARTIALLY_CONFIRMED,
                deadline
        );

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("[P1] Should ignore tournament match when tournament is no longer IN_PROGRESS")
    void shouldNotFindMatch_whenTournamentIsNotInProgress() {
        tournament.setStatus(TournamentStatus.COMPLETED);
        tournamentRepository.save(tournament);
        var expiredMatch = entityManager.persist(Match.builder()
                .creatorId(player1.getId())
                .opponentId(player2.getId())
                .status(Match.STATUS_PENDING_APPROVAL)
                .createdAt(Instant.now().minus(49, ChronoUnit.HOURS))
                .build());
        tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.IN_PROGRESS)
                .match(expiredMatch)
                .build());
        entityManager.flush();
        entityManager.clear();

        var deadline = Instant.now().minus(48, ChronoUnit.HOURS);
        var results = tournamentMatchRepository.findExpiredUnconfirmedMatches(
                TournamentStatus.IN_PROGRESS,
                TournamentMatchStatus.IN_PROGRESS,
                Match.STATUS_PENDING_APPROVAL,
                Match.STATUS_PARTIALLY_CONFIRMED,
                deadline
        );

        assertThat(results).isEmpty();
    }
}
