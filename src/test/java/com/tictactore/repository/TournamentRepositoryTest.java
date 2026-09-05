package com.tictactore.repository;

import com.tictactore.model.MatchFormat;
import com.tictactore.model.PointDistribution;
import com.tictactore.model.PositionSwapRule;
import com.tictactore.model.RestartRule;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.model.SideSwapRule;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import com.tictactore.model.WinByTwoRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TournamentRepository Data JPA Tests")
class TournamentRepositoryTest {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RuleConfigurationRepository ruleConfigurationRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User creator;
    private RuleConfiguration ruleConfig;

    @BeforeEach
    void setUp() {
        creator = userRepository.save(User.builder().email("creator@example.com").nickname("Creator").build());

        ruleConfig = ruleConfigurationRepository.save(RuleConfiguration.builder()
                .name("Standard Preset")
                .type(RuleConfigurationType.PRESET)
                .matchFormat(MatchFormat.BEST_OF_N)
                .goalLimit(5)
                .gameLimit(1)
                .gamesToWin(1)
                .winByTwoRule(WinByTwoRule.NONE)
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
                .createdBy(creator.getId())
                .build());
    }

    @Test
    @DisplayName("Should query completed tournaments with pagination")
    void shouldFindCompletedTournamentsPaginated() {
        var completed1 = tournamentRepository.save(Tournament.builder()
                .name("Completed Cup 1")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .ruleConfiguration(ruleConfig)
                .minParticipants(2)
                .maxParticipants(4)
                .registrationDeadline(Instant.now().minus(2, ChronoUnit.HOURS))
                .status(TournamentStatus.COMPLETED)
                .creator(creator)
                .build());

        var active1 = tournamentRepository.save(Tournament.builder()
                .name("Active Championship")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .ruleConfiguration(ruleConfig)
                .minParticipants(2)
                .maxParticipants(4)
                .registrationDeadline(Instant.now().plus(2, ChronoUnit.HOURS))
                .status(TournamentStatus.IN_PROGRESS)
                .creator(creator)
                .build());

        var completed2 = tournamentRepository.save(Tournament.builder()
                .name("Completed Cup 2")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .ruleConfiguration(ruleConfig)
                .minParticipants(2)
                .maxParticipants(4)
                .registrationDeadline(Instant.now().minus(1, ChronoUnit.HOURS))
                .status(TournamentStatus.COMPLETED)
                .creator(creator)
                .build());

        entityManager.flush();
        entityManager.clear();

        var pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        var resultPage = tournamentRepository.findByStatus(TournamentStatus.COMPLETED, pageable);

        assertThat(resultPage.getTotalElements()).isEqualTo(2);
        assertThat(resultPage.getContent())
                .extracting(Tournament::getName)
                .containsExactlyInAnyOrder("Completed Cup 1", "Completed Cup 2");
    }

    @Test
    @DisplayName("Should query all tournaments ordered by createdAt descending with pagination")
    void shouldFindAllByOrderByCreatedAtDescPaginated() {
        tournamentRepository.save(Tournament.builder()
                .name("First Created")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .ruleConfiguration(ruleConfig)
                .minParticipants(2)
                .maxParticipants(4)
                .registrationDeadline(Instant.now().plus(2, ChronoUnit.HOURS))
                .status(TournamentStatus.REGISTRATION_OPEN)
                .creator(creator)
                .build());

        tournamentRepository.save(Tournament.builder()
                .name("Second Created")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .ruleConfiguration(ruleConfig)
                .minParticipants(2)
                .maxParticipants(4)
                .registrationDeadline(Instant.now().plus(3, ChronoUnit.HOURS))
                .status(TournamentStatus.IN_PROGRESS)
                .creator(creator)
                .build());

        entityManager.flush();
        entityManager.clear();

        var pageable = PageRequest.of(0, 10);
        var resultPage = tournamentRepository.findAllByOrderByCreatedAtDesc(pageable);

        assertThat(resultPage.getContent()).hasSize(2);
    }
}
