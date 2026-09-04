package com.tictactore.service.tournament;

import com.tictactore.model.Game;
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
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.RuleConfigurationRepository;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRegistrationRepository;
import com.tictactore.repository.TournamentRepository;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("TournamentConfirmationDeadlineIT Component & Integration Tests")
class TournamentConfirmationDeadlineIT {

    @Autowired
    private TournamentConfirmationDeadlineService tournamentConfirmationDeadlineService;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TournamentMatchRepository tournamentMatchRepository;

    @Autowired
    private TournamentRegistrationRepository registrationRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RuleConfigurationRepository ruleConfigurationRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.redisson.api.RedissonClient redissonClient;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.springframework.data.redis.connection.RedisConnectionFactory redisConnectionFactory;

    private User player1;
    private User player2;
    private Tournament tournament;
    private TournamentRegistration reg1;
    private TournamentRegistration reg2;
    private TournamentMatch tournamentMatch;
    private Match coreMatch;

    @BeforeEach
    void setUp() {
        player1 = userRepository.save(User.builder().email("p1_deadline@example.com").nickname("PlayerOne").build());
        player2 = userRepository.save(User.builder().email("p2_deadline@example.com").nickname("PlayerTwo").build());

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
                .name("Final Cup Tournament")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .ruleConfiguration(ruleConfig)
                .minParticipants(2)
                .maxParticipants(2)
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

        coreMatch = matchRepository.save(Match.builder()
                .creatorId(player1.getId())
                .opponentId(player2.getId())
                .teamAAttackerId(player1.getId())
                .teamBAttackerId(player2.getId())
                .status(Match.STATUS_PENDING_APPROVAL)
                .createdAt(Instant.now().minus(49, ChronoUnit.HOURS))
                .games(List.of(
                        Game.builder()
                                .teamAScore(10)
                                .teamBScore(5)
                                .gameOrder(1)
                                .build()
                ))
                .build());

        tournamentMatch = tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .seed1(1)
                .seed2(2)
                .status(TournamentMatchStatus.IN_PROGRESS)
                .match(coreMatch)
                .build());
    }

    @Test
    @DisplayName("[P0] Should auto-confirm expired match, complete tournament match, award winner, and complete tournament")
    void shouldAutoConfirmAndCompleteTournament_whenConfirmationDeadlineExpires() {
        int processed = tournamentConfirmationDeadlineService.processExpiredConfirmationDeadlines();

        assertThat(processed).isEqualTo(1);

        var updatedCoreMatch = matchRepository.findById(coreMatch.getId()).orElseThrow();
        assertThat(updatedCoreMatch.getStatus()).isEqualTo(Match.STATUS_CONFIRMED);
        assertThat(updatedCoreMatch.getConfirmedAt()).isNotNull();

        var updatedTournamentMatch = tournamentMatchRepository.findById(tournamentMatch.getId()).orElseThrow();
        assertThat(updatedTournamentMatch.getStatus()).isEqualTo(TournamentMatchStatus.COMPLETED);
        assertThat(updatedTournamentMatch.getWinner()).isNotNull();
        assertThat(updatedTournamentMatch.getWinner().getId()).isEqualTo(reg1.getId());

        var updatedTournament = tournamentRepository.findById(tournament.getId()).orElseThrow();
        assertThat(updatedTournament.getStatus()).isEqualTo(TournamentStatus.COMPLETED);
    }
}
