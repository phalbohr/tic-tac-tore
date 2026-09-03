package com.tictactore.repository;

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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TournamentMatchRepository Data JPA Tests")
class TournamentMatchRepositoryTest {

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
    void shouldSaveAndQueryMatchesByTournamentIdAndRound() {
        var match = tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .seed1(1)
                .seed2(2)
                .status(TournamentMatchStatus.READY)
                .build());
        entityManager.flush();
        entityManager.clear();

        var matches = tournamentMatchRepository.findByTournamentIdAndRoundOrderByMatchOrderAsc(tournament.getId(), 1);

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).getId()).isEqualTo(match.getId());
        assertThat(matches.get(0).getStatus()).isEqualTo(TournamentMatchStatus.READY);
        assertThat(matches.get(0).getSeed1()).isEqualTo(1);
        assertThat(matches.get(0).getSeed2()).isEqualTo(2);
    }

    @Test
    void shouldQueryMatchesByStatus() {
        tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.READY)
                .build());
        tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(2)
                .matchOrder(1)
                .status(TournamentMatchStatus.PENDING)
                .build());
        entityManager.flush();
        entityManager.clear();

        var readyMatches = tournamentMatchRepository.findByTournamentIdAndStatus(tournament.getId(), TournamentMatchStatus.READY);
        var pendingMatches = tournamentMatchRepository.findByTournamentIdAndStatus(tournament.getId(), TournamentMatchStatus.PENDING);

        assertThat(readyMatches).hasSize(1);
        assertThat(pendingMatches).hasSize(1);
    }

    @Test
    void shouldQueryMatchesByParticipantRegistrationId() {
        tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.READY)
                .build());
        entityManager.flush();
        entityManager.clear();

        var matches = tournamentMatchRepository.findByParticipantRegistrationId(tournament.getId(), reg1.getId());

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).getParticipant1().getId()).isEqualTo(reg1.getId());
    }

    @Test
    void shouldFindTournamentsByStatusAndRegistrationDeadline() {
        tournament.setStatus(TournamentStatus.REGISTRATION_OPEN);
        tournament.setRegistrationDeadline(Instant.now().minus(1, ChronoUnit.HOURS));
        tournamentRepository.save(tournament);
        entityManager.flush();
        entityManager.clear();

        List<Tournament> result = tournamentRepository.findByStatusAndRegistrationDeadlineLessThanEqual(
                TournamentStatus.REGISTRATION_OPEN,
                Instant.now()
        );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(tournament.getId());
    }

    @Test
    void shouldSaveAndQueryMatchWithPartnersAndStubs() {
        var player3 = userRepository.save(User.builder().email("p3@example.com").nickname("PlayerThree").build());
        var player4 = userRepository.save(User.builder().email("p4@example.com").nickname("PlayerFour").build());
        var reg3 = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player3)
                .status(RegistrationStatus.CONFIRMED)
                .seed(3)
                .strengthScore(0.5)
                .build());
        var reg4 = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player4)
                .status(RegistrationStatus.CONFIRMED)
                .seed(4)
                .strengthScore(0.4)
                .build());
        var match = tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant1Partner(reg3)
                .participant2(reg2)
                .participant2Partner(reg4)
                .isParticipant1Stub(false)
                .isParticipant2Stub(true)
                .status(TournamentMatchStatus.READY)
                .build());
        entityManager.flush();
        entityManager.clear();

        var foundMatch = tournamentMatchRepository.findById(match.getId()).orElseThrow();

        assertThat(foundMatch.getParticipant1Partner()).isNotNull();
        assertThat(foundMatch.getParticipant1Partner().getId()).isEqualTo(reg3.getId());
        assertThat(foundMatch.getParticipant2Partner()).isNotNull();
        assertThat(foundMatch.getParticipant2Partner().getId()).isEqualTo(reg4.getId());
        assertThat(foundMatch.isParticipant1Stub()).isFalse();
        assertThat(foundMatch.isParticipant2Stub()).isTrue();
    }

    @Test
    void shouldFindMatchesByAnyParticipantRegistrationIdWhenPartner() {
        var player3 = userRepository.save(User.builder().email("p3_any@example.com").nickname("PlayerThreeAny").build());
        var player4 = userRepository.save(User.builder().email("p4_any@example.com").nickname("PlayerFourAny").build());
        var reg3 = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player3)
                .status(RegistrationStatus.CONFIRMED)
                .seed(3)
                .strengthScore(0.5)
                .build());
        var reg4 = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player4)
                .status(RegistrationStatus.CONFIRMED)
                .seed(4)
                .strengthScore(0.4)
                .build());
        tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant1Partner(reg3)
                .participant2(reg2)
                .participant2Partner(reg4)
                .status(TournamentMatchStatus.READY)
                .build());
        entityManager.flush();
        entityManager.clear();

        var matchesAsPartner1 = tournamentMatchRepository.findByAnyParticipantRegistrationId(tournament.getId(), reg3.getId());
        var matchesAsPartner2 = tournamentMatchRepository.findByAnyParticipantRegistrationId(tournament.getId(), reg4.getId());
        var matchesAsPrimary1 = tournamentMatchRepository.findByAnyParticipantRegistrationId(tournament.getId(), reg1.getId());

        assertThat(matchesAsPartner1).hasSize(1);
        assertThat(matchesAsPartner2).hasSize(1);
        assertThat(matchesAsPrimary1).hasSize(1);
    }

    @Test
    void shouldFindMatchesByTournamentIdAndStatusIn() {
        tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.READY)
                .build());
        tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(2)
                .matchOrder(1)
                .status(TournamentMatchStatus.PENDING)
                .build());
        tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(3)
                .matchOrder(1)
                .status(TournamentMatchStatus.COMPLETED)
                .build());
        entityManager.flush();
        entityManager.clear();

        var activeMatches = tournamentMatchRepository.findByTournamentIdAndStatusIn(
                tournament.getId(),
                List.of(TournamentMatchStatus.READY, TournamentMatchStatus.PENDING)
        );

        assertThat(activeMatches).hasSize(2);
    }

    @Test
    void shouldFindMatchByLinkedMatchId() {
        var matchEntity = entityManager.persist(com.tictactore.model.Match.builder()
                .creatorId(player1.getId())
                .teamAAttackerId(player1.getId())
                .teamBAttackerId(player2.getId())
                .status(com.tictactore.model.Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build());
        var tournamentMatch = tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.COMPLETED)
                .match(matchEntity)
                .build());
        entityManager.flush();
        entityManager.clear();

        var result = tournamentMatchRepository.findByMatchId(matchEntity.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(tournamentMatch.getId());
    }

    @Test
    void shouldFindActiveMatchesForParticipants() {
        var player3 = userRepository.save(User.builder().email("p3_active@example.com").nickname("PlayerThreeActive").build());
        var player4 = userRepository.save(User.builder().email("p4_active@example.com").nickname("PlayerFourActive").build());
        var reg3 = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player3)
                .status(RegistrationStatus.CONFIRMED)
                .seed(3)
                .strengthScore(0.5)
                .build());
        var reg4 = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player4)
                .status(RegistrationStatus.CONFIRMED)
                .seed(4)
                .strengthScore(0.4)
                .build());
        tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant1Partner(reg3)
                .participant2(reg2)
                .participant2Partner(reg4)
                .status(TournamentMatchStatus.IN_PROGRESS)
                .build());
        tournamentMatchRepository.save(TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(2)
                .participant1(reg3)
                .participant2(reg4)
                .status(TournamentMatchStatus.READY)
                .build());
        entityManager.flush();
        entityManager.clear();

        var activeForReg1 = tournamentMatchRepository.findActiveMatchesForParticipants(
                tournament.getId(),
                TournamentMatchStatus.IN_PROGRESS,
                List.of(reg1.getId())
        );
        var activeForReg4 = tournamentMatchRepository.findActiveMatchesForParticipants(
                tournament.getId(),
                TournamentMatchStatus.IN_PROGRESS,
                List.of(reg4.getId())
        );
        var activeForNonBusy = tournamentMatchRepository.findActiveMatchesForParticipants(
                tournament.getId(),
                TournamentMatchStatus.IN_PROGRESS,
                List.of(UUID.randomUUID())
        );

        assertThat(activeForReg1).hasSize(1);
        assertThat(activeForReg4).hasSize(1);
        assertThat(activeForNonBusy).isEmpty();
    }
}
