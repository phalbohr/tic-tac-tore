package com.tictactore.service;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.GameDto;
import com.tictactore.exception.DuplicatePlayerException;
import com.tictactore.exception.InvalidMatchScoreException;
import com.tictactore.exception.ParticipantNotFoundException;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.RateLimitService;
import com.tictactore.service.impl.MatchServiceImpl;
import com.tictactore.service.operation.MatchOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * ATDD Red-Phase Scaffolds for Match Creation Logic.
 * Story 2.4: Match Submission with Undo Window
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MatchService ATDD Specifications")
class MatchServiceATDDTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchOperation matchOperation;

    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private MatchServiceImpl matchService;

    private UUID p1, p2, p3, p4;

    @BeforeEach
    void setUp() {
        p1 = UUID.randomUUID();
        p2 = UUID.randomUUID();
        p3 = UUID.randomUUID();
        p4 = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Match Creation Specifications")
    class MatchCreationSpecs {

        @Test
        @DisplayName("[P0] Should save match with PENDING_APPROVAL status when 4 distinct players and valid game scores provided")
        void shouldCreateMatchSuccessfully() {
            var request = new CreateMatchRequest("key-123", p1, p1, p2, p3, p4, List.of(new GameDto(10, 8, p1, p2, p3, p4)), null, null);
            when(matchRepository.findByIdempotencyKey("key-123")).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).build(),
                    User.builder().id(p2).build(),
                    User.builder().id(p3).build(),
                    User.builder().id(p4).build()
            ));
            when(matchOperation.saveMatch(any(Match.class))).thenAnswer(invocation -> {
                Match m = invocation.getArgument(0);
                return Match.builder()
                        .id(UUID.randomUUID())
                        .idempotencyKey(m.getIdempotencyKey())
                        .creatorId(m.getCreatorId())
                        .teamAAttackerId(m.getTeamAAttackerId())
                        .teamADefenderId(m.getTeamADefenderId())
                        .teamBAttackerId(m.getTeamBAttackerId())
                        .teamBDefenderId(m.getTeamBDefenderId())
                        .status(m.getStatus())
                        .games(m.getGames())
                        .build();
            });

            var response = matchService.createMatch(request);
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("PENDING_APPROVAL");
        }

        @Test
        @DisplayName("[P1] Should throw DuplicatePlayerException when same player selected in multiple positions")
        void shouldRejectDuplicatePlayers() {
            var request = new CreateMatchRequest("key-123", p1, p1, p1, p3, p4, List.of(new GameDto(10, 8, p1, p1, p3, p4)), null, null);
            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(DuplicatePlayerException.class);
        }

        @Test
        @DisplayName("[P1] Should throw InvalidMatchScoreException when game scores exceed limits or have negative numbers")
        void shouldRejectInvalidGameScores() {
            var request = new CreateMatchRequest("key-123", p1, p1, p2, p3, p4, List.of(new GameDto(-1, 8, p1, p2, p3, p4)), null, null);
            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).build(),
                    User.builder().id(p2).build(),
                    User.builder().id(p3).build(),
                    User.builder().id(p4).build()
            ));
            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(InvalidMatchScoreException.class);
        }

        @Test
        @DisplayName("[P1] Should throw ParticipantNotFoundException when player ID does not exist in database")
        void shouldRejectNonExistentParticipant() {
            var request = new CreateMatchRequest("key-123", p1, p1, p2, p3, p4, List.of(new GameDto(10, 8, p1, p2, p3, p4)), null, null);
            when(userRepository.findAllById(any())).thenReturn(List.of(User.builder().id(p1).build()));
            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(ParticipantNotFoundException.class);
        }
    }
}
