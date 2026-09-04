package com.tictactore.service.tournament;

import com.tictactore.model.Match;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentStatus;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.service.operation.MatchOperation;
import com.tictactore.service.tournament.impl.TournamentConfirmationDeadlineServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentConfirmationDeadlineService Unit Tests")
class TournamentConfirmationDeadlineServiceTest {

    @Mock
    private TournamentMatchRepository tournamentMatchRepository;

    @Mock
    private MatchOperation matchOperation;

    private TournamentConfirmationDeadlineService deadlineService;

    @BeforeEach
    void setUp() {
        deadlineService = new TournamentConfirmationDeadlineServiceImpl(
                tournamentMatchRepository,
                matchOperation,
                48
        );
    }

    @Test
    @DisplayName("Should process expired matches by auto-confirming and saving via MatchOperation")
    void shouldProcessExpiredMatchesSuccessfully() {
        var matchId = UUID.randomUUID();
        var coreMatch = Match.builder()
                .id(matchId)
                .creatorId(UUID.randomUUID())
                .teamAAttackerId(UUID.randomUUID())
                .teamBAttackerId(UUID.randomUUID())
                .status(Match.STATUS_PENDING_APPROVAL)
                .createdAt(Instant.now().minus(50, ChronoUnit.HOURS))
                .build();
        var tournamentMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(Tournament.builder().id(UUID.randomUUID()).status(TournamentStatus.IN_PROGRESS).build())
                .status(TournamentMatchStatus.IN_PROGRESS)
                .match(coreMatch)
                .build();

        when(tournamentMatchRepository.findExpiredUnconfirmedMatches(
                eq(TournamentStatus.IN_PROGRESS),
                eq(TournamentMatchStatus.IN_PROGRESS),
                eq(Match.STATUS_PENDING_APPROVAL),
                eq(Match.STATUS_PARTIALLY_CONFIRMED),
                any(Instant.class),
                any(Pageable.class)
        )).thenReturn(List.of(tournamentMatch));

        int processed = deadlineService.processExpiredConfirmationDeadlines();

        assertThat(processed).isEqualTo(1);
        assertThat(coreMatch.getStatus()).isEqualTo(Match.STATUS_CONFIRMED);
        assertThat(coreMatch.getConfirmedAt()).isNotNull();
        verify(matchOperation).saveMatch(coreMatch);
    }

    @Test
    @DisplayName("Should return 0 when no expired matches exist")
    void shouldReturnZero_whenNoExpiredMatchesFound() {
        when(tournamentMatchRepository.findExpiredUnconfirmedMatches(
                eq(TournamentStatus.IN_PROGRESS),
                eq(TournamentMatchStatus.IN_PROGRESS),
                eq(Match.STATUS_PENDING_APPROVAL),
                eq(Match.STATUS_PARTIALLY_CONFIRMED),
                any(Instant.class),
                any(Pageable.class)
        )).thenReturn(Collections.emptyList());

        int processed = deadlineService.processExpiredConfirmationDeadlines();

        assertThat(processed).isZero();
    }

    @Test
    @DisplayName("Should isolate errors per match and continue processing subsequent expired matches")
    void shouldIsolateErrors_whenSingleMatchFails() {
        var failingCoreMatch = Match.builder()
                .id(UUID.randomUUID())
                .creatorId(UUID.randomUUID())
                .teamAAttackerId(UUID.randomUUID())
                .teamBAttackerId(UUID.randomUUID())
                .status(Match.STATUS_PENDING_APPROVAL)
                .createdAt(Instant.now().minus(50, ChronoUnit.HOURS))
                .build();
        var failingTournamentMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(Tournament.builder().id(UUID.randomUUID()).status(TournamentStatus.IN_PROGRESS).build())
                .status(TournamentMatchStatus.IN_PROGRESS)
                .match(failingCoreMatch)
                .build();

        var successCoreMatch = Match.builder()
                .id(UUID.randomUUID())
                .creatorId(UUID.randomUUID())
                .teamAAttackerId(UUID.randomUUID())
                .teamBAttackerId(UUID.randomUUID())
                .status(Match.STATUS_PENDING_APPROVAL)
                .createdAt(Instant.now().minus(50, ChronoUnit.HOURS))
                .build();
        var successTournamentMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(Tournament.builder().id(UUID.randomUUID()).status(TournamentStatus.IN_PROGRESS).build())
                .status(TournamentMatchStatus.IN_PROGRESS)
                .match(successCoreMatch)
                .build();

        when(tournamentMatchRepository.findExpiredUnconfirmedMatches(
                eq(TournamentStatus.IN_PROGRESS),
                eq(TournamentMatchStatus.IN_PROGRESS),
                eq(Match.STATUS_PENDING_APPROVAL),
                eq(Match.STATUS_PARTIALLY_CONFIRMED),
                any(Instant.class),
                any(Pageable.class)
        )).thenReturn(List.of(failingTournamentMatch, successTournamentMatch));

        doThrow(new RuntimeException("Database lock error"))
                .when(matchOperation).saveMatch(failingCoreMatch);

        int processed = deadlineService.processExpiredConfirmationDeadlines();

        assertThat(processed).isEqualTo(1);
        assertThat(successCoreMatch.getStatus()).isEqualTo(Match.STATUS_CONFIRMED);
        verify(matchOperation).saveMatch(failingCoreMatch);
        verify(matchOperation).saveMatch(successCoreMatch);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -48})
    @DisplayName("Should throw IllegalArgumentException when deadlineHours is non-positive")
    void shouldThrowException_whenDeadlineHoursIsNonPositive(int invalidDeadlineHours) {
        assertThatThrownBy(() -> new TournamentConfirmationDeadlineServiceImpl(
                tournamentMatchRepository,
                matchOperation,
                invalidDeadlineHours
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Deadline hours must be greater than 0");
    }
}
