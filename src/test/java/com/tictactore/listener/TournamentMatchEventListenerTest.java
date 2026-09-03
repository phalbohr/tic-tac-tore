package com.tictactore.listener;

import com.tictactore.event.MatchConfirmedEvent;
import com.tictactore.model.TournamentMatch;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.service.tournament.TournamentMatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentMatchEventListener Specifications")
class TournamentMatchEventListenerTest {

    @Mock
    private TournamentMatchRepository tournamentMatchRepository;

    @Mock
    private TournamentMatchService tournamentMatchService;

    @InjectMocks
    private TournamentMatchEventListener listener;

    private UUID matchId;
    private UUID tournamentMatchId;
    private TournamentMatch tournamentMatch;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
        tournamentMatchId = UUID.randomUUID();
        tournamentMatch = TournamentMatch.builder()
                .id(tournamentMatchId)
                .build();
    }

    @Test
    void shouldDelegateMatchCompletion_whenTournamentMatchFound() {
        when(tournamentMatchRepository.findByMatchId(matchId)).thenReturn(Optional.of(tournamentMatch));

        listener.handleMatchConfirmed(new MatchConfirmedEvent(matchId, List.of(UUID.randomUUID(), UUID.randomUUID())));

        verify(tournamentMatchService).completeMatch(tournamentMatchId, matchId);
    }

    @Test
    void shouldIgnoreEvent_whenNotTournamentMatch() {
        when(tournamentMatchRepository.findByMatchId(matchId)).thenReturn(Optional.empty());

        listener.handleMatchConfirmed(new MatchConfirmedEvent(matchId, List.of(UUID.randomUUID())));

        verify(tournamentMatchService, never()).completeMatch(any(), any());
    }

    @Test
    void shouldIgnoreNullEventOrMatchId() {
        listener.handleMatchConfirmed(null);
        listener.handleMatchConfirmed(new MatchConfirmedEvent(null, List.of()));

        verify(tournamentMatchService, never()).completeMatch(any(), any());
    }
}
