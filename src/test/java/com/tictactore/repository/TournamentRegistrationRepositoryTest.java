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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TournamentRegistrationRepository Data JPA Tests")
class TournamentRegistrationRepositoryTest {

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
    private User player3;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        player1 = userRepository.save(User.builder().email("p1@example.com").nickname("PlayerOne").build());
        player2 = userRepository.save(User.builder().email("p2@example.com").nickname("PlayerTwo").build());
        player3 = userRepository.save(User.builder().email("p3@example.com").nickname("PlayerThree").build());

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
                .name("Spring Championship")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.TWO_VS_TWO_FIXED_TEAMS)
                .ruleConfiguration(ruleConfig)
                .minParticipants(4)
                .maxParticipants(16)
                .registrationDeadline(Instant.now().plus(7, ChronoUnit.DAYS))
                .status(TournamentStatus.REGISTRATION_OPEN)
                .creator(player1)
                .build());
    }

    @Test
    void shouldSaveAndFindRegistrationByTournamentId() {
        var registration = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player1)
                .partner(player2)
                .status(RegistrationStatus.PENDING_CONFIRMATION)
                .build());
        entityManager.flush();
        entityManager.clear();

        var list = registrationRepository.findByTournamentId(tournament.getId());

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(registration.getId());
        assertThat(list.get(0).getPlayer().getNickname()).isEqualTo("PlayerOne");
        assertThat(list.get(0).getPartner().getNickname()).isEqualTo("PlayerTwo");
    }

    @Test
    void shouldFilterByTournamentIdAndStatus() {
        registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player1)
                .partner(player2)
                .status(RegistrationStatus.CONFIRMED)
                .build());
        registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player3)
                .partner(null)
                .status(RegistrationStatus.PENDING_CONFIRMATION)
                .build());
        entityManager.flush();
        entityManager.clear();

        var confirmedList = registrationRepository.findByTournamentIdAndStatus(tournament.getId(), RegistrationStatus.CONFIRMED);
        var pendingCount = registrationRepository.countByTournamentIdAndStatus(tournament.getId(), RegistrationStatus.PENDING_CONFIRMATION);

        assertThat(confirmedList).hasSize(1);
        assertThat(confirmedList.get(0).getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
        assertThat(pendingCount).isEqualTo(1L);
    }

    @Test
    void shouldFindActiveUserRegistrationWhenUserIsPlayerOrPartner() {
        var reg = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player1)
                .partner(player2)
                .status(RegistrationStatus.CONFIRMED)
                .build());
        entityManager.flush();
        entityManager.clear();

        var activeForPlayer = registrationRepository.findActiveUserRegistration(
                tournament.getId(),
                player1.getId(),
                Set.of(RegistrationStatus.CONFIRMED, RegistrationStatus.PENDING_CONFIRMATION)
        );
        var activeForPartner = registrationRepository.findActiveUserRegistration(
                tournament.getId(),
                player2.getId(),
                Set.of(RegistrationStatus.CONFIRMED, RegistrationStatus.PENDING_CONFIRMATION)
        );

        assertThat(activeForPlayer).isPresent();
        assertThat(activeForPlayer.get().getId()).isEqualTo(reg.getId());
        assertThat(activeForPartner).isPresent();
        assertThat(activeForPartner.get().getId()).isEqualTo(reg.getId());
    }

    @Test
    void shouldFindPendingInvitationsForUser() {
        var invite = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player1)
                .partner(player2)
                .status(RegistrationStatus.PENDING_CONFIRMATION)
                .build());
        entityManager.flush();
        entityManager.clear();

        var pendingInvites = registrationRepository.findPendingInvitationsForUser(player2.getId());

        assertThat(pendingInvites).hasSize(1);
        assertThat(pendingInvites.get(0).getId()).isEqualTo(invite.getId());
        assertThat(pendingInvites.get(0).getPartner().getId()).isEqualTo(player2.getId());
    }

    @Test
    void shouldFindByIdWithDetails() {
        var reg = registrationRepository.save(TournamentRegistration.builder()
                .tournament(tournament)
                .player(player1)
                .partner(player2)
                .status(RegistrationStatus.CONFIRMED)
                .build());
        entityManager.flush();
        entityManager.clear();

        var found = registrationRepository.findByIdWithDetails(reg.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTournament().getName()).isEqualTo("Spring Championship");
        assertThat(found.get().getPlayer().getEmail()).isEqualTo("p1@example.com");
        assertThat(found.get().getPartner().getEmail()).isEqualTo("p2@example.com");
    }
}
