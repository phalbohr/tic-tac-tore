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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TournamentMatchRepository ATDD Specifications")
class TournamentMatchRepositoryATDDTest {

    @Autowired
    private TournamentMatchRepository tournamentMatchRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Tournament tournament;
    private TournamentRegistration reg1;
    private TournamentRegistration reg2;
    private TournamentRegistration reg3;
    private TournamentRegistration reg4;
    private TournamentMatch match1;
    private TournamentMatch match2;

    @BeforeEach
    void setUp() {
        var ruleConfig = ruleConfigurationRepositoryEntity();
        entityManager.persist(ruleConfig);

        var player1 = User.builder().email("p1@example.com").nickname("Player1").build();
        var player2 = User.builder().email("p2@example.com").nickname("Player2").build();
        var player3 = User.builder().email("p3@example.com").nickname("Player3").build();
        var player4 = User.builder().email("p4@example.com").nickname("Player4").build();
        entityManager.persist(player1);
        entityManager.persist(player2);
        entityManager.persist(player3);
        entityManager.persist(player4);

        tournament = Tournament.builder()
                .title("Async Tournament")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS)
                .status(TournamentStatus.IN_PROGRESS)
                .creator(player1)
                .ruleConfiguration(ruleConfig)
                .registrationDeadline(Instant.now().plus(1, ChronoUnit.DAYS))
                .minParticipants(4)
                .maxParticipants(8)
                .build();
        entityManager.persist(tournament);

        reg1 = TournamentRegistration.builder().tournament(tournament).player(player1).status(RegistrationStatus.CONFIRMED).build();
        reg2 = TournamentRegistration.builder().tournament(tournament).player(player2).status(RegistrationStatus.CONFIRMED).build();
        reg3 = TournamentRegistration.builder().tournament(tournament).player(player3).status(RegistrationStatus.CONFIRMED).build();
        reg4 = TournamentRegistration.builder().tournament(tournament).player(player4).status(RegistrationStatus.CONFIRMED).build();
        entityManager.persist(reg1);
        entityManager.persist(reg2);
        entityManager.persist(reg3);
        entityManager.persist(reg4);

        match1 = TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant1Partner(reg2)
                .participant2(reg3)
                .participant2Partner(reg4)
                .status(TournamentMatchStatus.IN_PROGRESS)
                .build();
        entityManager.persist(match1);

        match2 = TournamentMatch.builder()
                .tournament(tournament)
                .round(1)
                .matchOrder(2)
                .participant1(reg1)
                .participant2(reg3)
                .status(TournamentMatchStatus.READY)
                .build();
        entityManager.persist(match2);

        entityManager.flush();
    }

    private RuleConfiguration ruleConfigurationRepositoryEntity() {
        return RuleConfiguration.builder()
                .name("Standard Rules")
                .type(RuleConfigurationType.CUSTOM)
                .pointsToWin(10)
                .pointDifferenceToWin(2)
                .maxPoints(15)
                .pointsForWin(3)
                .pointsForDraw(1)
                .pointsForLoss(0)
                .sideSwapRule(SideSwapRule.EVERY_ROUND)
                .positionSwapRule(PositionSwapRule.AFTER_SERVE)
                .restartRule(RestartRule.STANDARD)
                .pointDistribution(PointDistribution.STANDARD)
                .build();
    }

    @Test
    @DisplayName("findActiveMatchesForParticipants should find in-progress matches matching any participant or partner")
    void shouldFindActiveMatches_forParticipants() {
        var activeMatches = tournamentMatchRepository.findActiveMatchesForParticipants(
                tournament.getId(),
                TournamentMatchStatus.IN_PROGRESS,
                List.of(reg2.getId())
        );

        assertThat(activeMatches).hasSize(1);
        assertThat(activeMatches.get(0).getId()).isEqualTo(match1.getId());
    }

    @Test
    @DisplayName("findActiveMatchesForParticipants should return empty list when no active matches match participants")
    void shouldReturnEmptyList_whenNoActiveMatchesFound() {
        var nonActiveRegId = UUID.randomUUID();

        var activeMatches = tournamentMatchRepository.findActiveMatchesForParticipants(
                tournament.getId(),
                TournamentMatchStatus.IN_PROGRESS,
                List.of(nonActiveRegId)
        );

        assertThat(activeMatches).isEmpty();
    }

    @Test
    @DisplayName("findByMatchId should return linked TournamentMatch when match is associated")
    void shouldFindByMatchId_whenMatchIsLinked() {
        var coreMatch = Match.builder()
                .creatorId(reg1.getPlayer().getId())
                .teamAAttackerId(reg1.getPlayer().getId())
                .teamBAttackerId(reg3.getPlayer().getId())
                .status("CONFIRMED")
                .build();
        entityManager.persist(coreMatch);
        match1.setMatch(coreMatch);
        entityManager.persist(match1);
        entityManager.flush();

        var found = tournamentMatchRepository.findByMatchId(coreMatch.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(match1.getId());
    }
}
