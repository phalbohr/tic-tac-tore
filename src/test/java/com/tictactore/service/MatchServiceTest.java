package com.tictactore.service;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.GameDto;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.DuplicatePlayerException;
import com.tictactore.exception.InvalidMatchScoreException;
import com.tictactore.exception.ParticipantNotFoundException;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchService Unit Tests")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchOperation matchOperation;

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
    @DisplayName("Match Creation Tests")
    class MatchCreationTests {

        @Test
        @DisplayName("[P0] Should save match with PENDING_APPROVAL status when 4 distinct players and valid game scores provided")
        void shouldCreateMatchSuccessfully() {
            // Given
            CreateMatchRequest request = new CreateMatchRequest(
                    "idempotency-123",
                    p1, p1, p2, p3, p4,
                    List.of(new GameDto(10, 8), new GameDto(10, 6))
            );

            when(matchRepository.findByIdempotencyKey("idempotency-123")).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).build(),
                    User.builder().id(p2).build(),
                    User.builder().id(p3).build(),
                    User.builder().id(p4).build()
            ));

            Match saved = Match.builder()
                    .id(UUID.randomUUID())
                    .idempotencyKey("idempotency-123")
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            when(matchOperation.saveMatch(any(Match.class))).thenReturn(saved);

            // When
            MatchResponse response = matchService.createMatch(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("PENDING_APPROVAL");
            assertThat(response.idempotencyKey()).isEqualTo("idempotency-123");
            verify(matchOperation).saveMatch(any(Match.class));
        }

        @Test
        @DisplayName("[P1] Should throw DuplicatePlayerException when same player selected in multiple positions")
        void shouldRejectDuplicatePlayers() {
            // Given
            CreateMatchRequest request = new CreateMatchRequest(
                    "idempotency-456",
                    p1, p1, p1, p3, p4, // p1 duplicated
                    List.of(new GameDto(10, 8))
            );

            // When / Then
            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(DuplicatePlayerException.class)
                    .hasMessageContaining("Same player selected in multiple positions");

            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P1] Should throw InvalidMatchScoreException when game scores exceed limits")
        void shouldRejectInvalidGameScores() {
            // Given
            CreateMatchRequest request = new CreateMatchRequest(
                    "idempotency-789",
                    p1, p1, p2, p3, p4,
                    List.of(new GameDto(101, 8)) // score > 100
            );

            when(matchRepository.findByIdempotencyKey("idempotency-789")).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).build(),
                    User.builder().id(p2).build(),
                    User.builder().id(p3).build(),
                    User.builder().id(p4).build()
            ));

            // When / Then
            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(InvalidMatchScoreException.class)
                    .hasMessageContaining("Game scores must be between 0 and 100");

            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P1] Should throw ParticipantNotFoundException when player ID does not exist in database")
        void shouldRejectNonExistentParticipant() {
            // Given
            CreateMatchRequest request = new CreateMatchRequest(
                    "idempotency-999",
                    p1, p1, p2, p3, p4,
                    List.of(new GameDto(10, 8))
            );

            when(matchRepository.findByIdempotencyKey("idempotency-999")).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(User.builder().id(p1).build()));

            // When / Then
            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(ParticipantNotFoundException.class)
                    .hasMessageContaining("Player not found with ID");

            verifyNoInteractions(matchOperation);
        }
    }
}
