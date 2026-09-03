package com.tictactore.service;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.GameDto;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.DuplicatePlayerException;
import com.tictactore.exception.DuplicatePositionException;
import com.tictactore.exception.InvalidMatchScoreException;
import com.tictactore.exception.InvalidPositionException;
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

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private com.tictactore.repository.TournamentMatchRepository tournamentMatchRepository;

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
                    List.of(new GameDto(10, 8, p1, p2, p3, p4), new GameDto(10, 6, p1, p2, p3, p4)),
                    null, null
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
        void shouldLinkTournamentMatch_whenTournamentMatchIdProvided() {
            var tournamentMatchId = UUID.randomUUID();
            var tournamentMatch = com.tictactore.model.TournamentMatch.builder().id(tournamentMatchId).build();
            CreateMatchRequest request = new CreateMatchRequest(
                    "idempotency-tm",
                    p1, p1, null, p3, null,
                    List.of(new GameDto(10, 8, null, null, null, null)),
                    null, null, tournamentMatchId
            );

            when(matchRepository.findByIdempotencyKey("idempotency-tm")).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).build(),
                    User.builder().id(p3).build()
            ));
            Match saved = Match.builder()
                    .id(UUID.randomUUID())
                    .idempotencyKey("idempotency-tm")
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p3)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();
            when(matchOperation.saveMatch(any(Match.class))).thenReturn(saved);
            when(tournamentMatchRepository.findById(tournamentMatchId)).thenReturn(Optional.of(tournamentMatch));

            MatchResponse response = matchService.createMatch(request);

            assertThat(response).isNotNull();
            assertThat(tournamentMatch.getMatch()).isEqualTo(saved);
            verify(tournamentMatchRepository).save(tournamentMatch);
        }

        @Test
        @DisplayName("[P1] Should throw DuplicatePlayerException when same player selected in multiple positions")
        void shouldRejectDuplicatePlayers() {
            // Given
            CreateMatchRequest request = new CreateMatchRequest(
                    "idempotency-456",
                    p1, p1, p1, p3, p4, // p1 duplicated
                    List.of(new GameDto(10, 8, p1, p1, p3, p4)),
                    null, null
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
                    List.of(new GameDto(101, 8, p1, p2, p3, p4)), null, null
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
                    List.of(new GameDto(10, 8, p1, p2, p3, p4)),
                    null, null
            );

            when(matchRepository.findByIdempotencyKey("idempotency-999")).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(User.builder().id(p1).build()));

            // When / Then
            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(ParticipantNotFoundException.class)
                    .hasMessageContaining("Player not found with ID");

            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P1] Should throw InvalidPositionException when 1v1 match contains positional data")
        void shouldReject1v1PositionalData() {
            var request = new CreateMatchRequest(
                    "idempotency-pos-1",
                    p1, p1, null, p3, null,
                    List.of(new GameDto(10, 8, p1, null, null, null)),                    null, null
            );

            when(matchRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).build(),
                    User.builder().id(p3).build()
            ));

            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(InvalidPositionException.class)
                    .hasMessageContaining("1v1 matches must not contain positional data");

            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P1] Should throw InvalidPositionException when attacker IDs are null")
        void shouldRejectNullAttackerIds() {
            var request = new CreateMatchRequest(
                    "idempotency-null-attacker",
                    p1, null, p2, p3, p4,
                    List.of(new GameDto(10, 8, null, p2, p3, p4)),
                    null, null
            );

            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(InvalidPositionException.class)
                    .hasMessageContaining("Attacker IDs must not be null");

            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P1] Should throw InvalidPositionException when 2v2 match game lacks positional data")
        void shouldReject2v2MissingPositionalData() {
            var request = new CreateMatchRequest(
                    "idempotency-pos-2",
                    p1, p1, p2, p3, p4,
                    List.of(new GameDto(10, 8, p1, p2, null, p4)),
                    null, null
            );

            givenFourPlayersExist();

            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(InvalidPositionException.class)
                    .hasMessageContaining("2v2 games must contain positional data");

            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P1] Should throw DuplicatePositionException when 2v2 game contains duplicate positional players")
        void shouldReject2v2DuplicatePositionalData() {
            var request = new CreateMatchRequest(
                    "idempotency-pos-3",
                    p1, p1, p2, p3, p4,
                    List.of(new GameDto(10, 8, p1, p1, p3, p4)),
                    null, null
            );

            givenFourPlayersExist();

            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(DuplicatePositionException.class)
                    .hasMessageContaining("Same player selected in multiple positions");

            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P1] Should allow creatorId even when creator does not belong to match participants")
        void shouldAllowCreatorNotParticipant() {
            var nonParticipantCreator = UUID.randomUUID();
            var request = new CreateMatchRequest(
                    "idempotency-1000",
                    nonParticipantCreator, p1, p2, p3, p4,
                    List.of(new GameDto(10, 8, p1, p2, p3, p4)),
                    null, null
            );

            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).email("p1@test.com").build(),
                    User.builder().id(p2).email("p2@test.com").build(),
                    User.builder().id(p3).email("p3@test.com").build(),
                    User.builder().id(p4).email("p4@test.com").build()
            ));
            when(matchOperation.saveMatch(any())).thenAnswer(invocation -> invocation.getArgument(0));

            var response = matchService.createMatch(request);

            assertThat(response).isNotNull();
            assertThat(response.creatorId()).isEqualTo(nonParticipantCreator);
        }


        @Test
        @DisplayName("[P1] Should throw InvalidPositionException when 2v2 game players do not match match players")
        void shouldReject2v2MismatchMatchPlayers() {
            var p5 = UUID.randomUUID();
            var request = new CreateMatchRequest(
                    "idempotency-pos-4",
                    p1, p1, p2, p3, p4,
                    List.of(new GameDto(10, 8, p1, p2, p3, p5)),
                    null, null
            );

            givenFourPlayersExist();

            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(InvalidPositionException.class)
                    .hasMessageContaining("Game players must match match players");

            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P1] Should throw InvalidPositionException when Team A player is assigned to Team B position")
        void shouldReject2v2SwappingTeamPositions() {
            var request = new CreateMatchRequest(
                    "idempotency-pos-5",
                    p1, p1, p2, p3, p4,
                    List.of(new GameDto(10, 8, p1, p3, p2, p4)),
                    null, null
            );

            givenFourPlayersExist();

            assertThatThrownBy(() -> matchService.createMatch(request))
                    .isInstanceOf(InvalidPositionException.class)
                    .hasMessageContaining("Team A players cannot be assigned to Team B positions");

            verifyNoInteractions(matchOperation);
        }

    @Nested
    @DisplayName("Duplicate Detection Tests")
    class DuplicateDetectionTests {

        @Test
        @DisplayName("[P1] Should set isDuplicateWarning = true when identical match exists on same UTC day")
        void shouldSetDuplicateWarningWhenIdenticalMatchExists() {
            CreateMatchRequest request = new CreateMatchRequest(
                    "dup-1",
                    p1, p1, null, p3, null,
                    List.of(new GameDto(10, 8, null, null, null, null)),                    null, null
            );

            Match savedMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p3)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            Match duplicate = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p3)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            when(matchRepository.findByIdempotencyKey("dup-1")).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).build(),
                    User.builder().id(p3).build()
            ));
            when(matchOperation.saveMatch(any())).thenReturn(savedMatch);
            when(matchRepository.findDuplicatesOnDate(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(duplicate));

            matchService.createMatch(request);

            verify(pushNotificationService).sendConfirmationRequest(eq(savedMatch), anyList(), eq(true));
        }

        @Test
        @DisplayName("[P1] Should set isDuplicateWarning = false when no duplicates exist on same day")
        void shouldNotSetDuplicateWarningWhenNoDuplicates() {
            CreateMatchRequest request = new CreateMatchRequest(
                    "dup-2",
                    p1, p1, null, p3, null,
                    List.of(new GameDto(10, 8, null, null, null, null)),                    null, null
            );

            Match savedMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p3)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            when(matchRepository.findByIdempotencyKey("dup-2")).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).build(),
                    User.builder().id(p3).build()
            ));
            when(matchOperation.saveMatch(any())).thenReturn(savedMatch);
            when(matchRepository.findDuplicatesOnDate(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(savedMatch));

            matchService.createMatch(request);

            verify(pushNotificationService).sendConfirmationRequest(eq(savedMatch), anyList(), eq(false));
        }

        @Test
        @DisplayName("[P1] Should resolve opposing team as recipients in 2v2 duplicate detection")
        void shouldResolveOpposingTeamAsRecipientsIn2v2() {
            CreateMatchRequest request = new CreateMatchRequest(
                    "dup-3",
                    p1,
                    p1, p2,
                    p3, p4,
                    List.of(new GameDto(10, 8, p1, p2, p3, p4)),
                    null, null
            );

            when(matchRepository.findByIdempotencyKey("dup-3")).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).build(),
                    User.builder().id(p2).build(),
                    User.builder().id(p3).build(),
                    User.builder().id(p4).build()
            ));

            Match savedMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            Match duplicate = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            when(matchOperation.saveMatch(any())).thenReturn(savedMatch);
            when(matchRepository.findDuplicatesOnDate(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(duplicate));

            matchService.createMatch(request);

            verify(pushNotificationService).sendConfirmationRequest(eq(savedMatch), anyList(), eq(true));
        }

        @Test
        @DisplayName("[P1] Should not fail match creation when push notification dispatch throws exception")
        void shouldNotFailMatchCreationOnPushError() {
            CreateMatchRequest request = new CreateMatchRequest(
                    "dup-4",
                    p1, p1, null, p3, null,
                    List.of(new GameDto(10, 8, null, null, null, null)),                    null, null
            );

            when(matchRepository.findByIdempotencyKey("dup-4")).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).build(),
                    User.builder().id(p3).build()
            ));
            when(matchOperation.saveMatch(any())).thenAnswer(inv -> {
                Match m = inv.getArgument(0);
                m.setId(UUID.randomUUID());
                return m;
            });
            when(matchRepository.findDuplicatesOnDate(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of());
            doThrow(new RuntimeException("Push service down")).when(pushNotificationService).sendConfirmationRequest(any(), anyList(), anyBoolean());

            MatchResponse response = matchService.createMatch(request);

            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo("PENDING_APPROVAL");
        }
    }

        private void givenFourPlayersExist() {
            when(matchRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(p1).build(),
                    User.builder().id(p2).build(),
                    User.builder().id(p3).build(),
                    User.builder().id(p4).build()
            ));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/matches/{id}/confirm Specs")
    class MatchConfirmationTests {

        @Test
        @DisplayName("[P0] Should confirm match when opponent confirms pending match")
        void shouldConfirmMatch_whenOpponentConfirmsPendingMatch() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            var confirmedMatch = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("CONFIRMED")
                    .confirmedByUserId(p2)
                    .confirmedAt(Instant.now())
                    .createdAt(Instant.now())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchOperation.confirmMatch(any(Match.class), eq(p2))).thenReturn(confirmedMatch);

            var response = matchService.confirmMatch(matchId, p2, "idem-1");

            assertThat(response.status()).isEqualTo("CONFIRMED");
            assertThat(response.confirmedByUserId()).isEqualTo(p2);
            verify(matchOperation).confirmMatch(match, p2);
        }

        @Test
        @DisplayName("[P1] Should throw UnauthorizedMatchActionException when creator tries to confirm")
        void shouldThrowUnauthorized_whenCreatorTriesToConfirm() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("PENDING_APPROVAL")
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchOperation.confirmMatch(any(Match.class), eq(p1)))
                    .thenThrow(new com.tictactore.exception.UnauthorizedMatchActionException("User " + p1 + " is not an opponent for match " + matchId));

            assertThatThrownBy(() -> matchService.confirmMatch(matchId, p1, null))
                    .isInstanceOf(com.tictactore.exception.UnauthorizedMatchActionException.class);
        }

        @Test
        @DisplayName("[P1] Should throw UnauthorizedMatchActionException when non-participant tries to confirm")
        void shouldThrowUnauthorized_whenNonParticipantTriesToConfirm() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("PENDING_APPROVAL")
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchOperation.confirmMatch(any(Match.class), eq(p3)))
                    .thenThrow(new com.tictactore.exception.UnauthorizedMatchActionException("User " + p3 + " is not an opponent for match " + matchId));

            assertThatThrownBy(() -> matchService.confirmMatch(matchId, p3, null))
                    .isInstanceOf(com.tictactore.exception.UnauthorizedMatchActionException.class);
        }

        @Test
        @DisplayName("[P1] Should return confirmed match when already confirmed by same opponent (idempotency)")
        void shouldReturnConfirmedMatch_whenAlreadyConfirmedBySameOpponent() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("CONFIRMED")
                    .confirmedByUserId(p2)
                    .confirmedAt(Instant.now())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

            var response = matchService.confirmMatch(matchId, p2, "idem-1");

            assertThat(response.status()).isEqualTo("CONFIRMED");
            assertThat(response.confirmedByUserId()).isEqualTo(p2);
            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P1] Should throw InvalidMatchStateException when match is already confirmed by someone else")
        void shouldThrowInvalidState_whenAlreadyConfirmedByOther() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("CONFIRMED")
                    .confirmedByUserId(p2)
                    .confirmedAt(Instant.now())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

            assertThatThrownBy(() -> matchService.confirmMatch(matchId, p4, null))
                    .isInstanceOf(com.tictactore.exception.InvalidMatchStateException.class);

            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P0] Should return pending matches for participant user")
        void shouldReturnPendingMatchesForParticipantUser() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .games(java.util.List.of())
                    .build();

            when(matchRepository.findByStatusIn(List.of(Match.STATUS_PENDING_APPROVAL, Match.STATUS_PARTIALLY_CONFIRMED))).thenReturn(java.util.List.of(match));

            var result = matchService.getPendingMatches(p2);

            assertThat(result.count()).isEqualTo(1);
            assertThat(result.matches().get(0).id()).isEqualTo(matchId);
        }

        @Test
        @DisplayName("[P1] Should return pending match when user already confirmed in PARTIALLY_CONFIRMED match (idempotency)")
        void shouldReturnPartiallyConfirmedMatch_whenAlreadyConfirmedBySameOpponent() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status("PARTIALLY_CONFIRMED")
                    .confirmedByOpponentIds(p2.toString())
                    .createdAt(Instant.now())
                    .games(java.util.List.of())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

            var response = matchService.confirmMatch(matchId, p2, "idem-partial-1");

            assertThat(response.status()).isEqualTo("PARTIALLY_CONFIRMED");
            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P1] Should confirm 2v2 standard match and notify remaining opponent when first opponent confirms")
        void shouldEnterPartiallyConfirmedAndNotify_whenFirstDoublesStandardOpponentConfirms() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status("PENDING_APPROVAL")
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .createdAt(Instant.now())
                    .games(java.util.List.of())
                    .build();

            var partiallyConfirmed = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status("PARTIALLY_CONFIRMED")
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .confirmedByOpponentIds(p3.toString())
                    .createdAt(Instant.now())
                    .games(java.util.List.of())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchOperation.confirmMatch(any(Match.class), eq(p3))).thenReturn(partiallyConfirmed);
            when(userRepository.findAllById(List.of(p4))).thenReturn(
                    List.of(User.builder().id(p4).build()));
            when(userRepository.findById(p3)).thenReturn(
                    Optional.of(User.builder().id(p3).build()));

            var response = matchService.confirmMatch(matchId, p3, "idem-2v2-partial");

            assertThat(response.status()).isEqualTo("PARTIALLY_CONFIRMED");
            assertThat(response.confirmedByOpponentIds()).containsExactly(p3);
            verify(matchOperation).confirmMatch(match, p3);
            verify(pushNotificationService).sendPartialConfirmationNotification(any(Match.class), anyList(), anyString());
        }

        @Test
        @DisplayName("[P1] Should confirm 2v2 standard match when second opponent confirms from PARTIALLY_CONFIRMED")
        void shouldConfirmMatch_whenSecondOpponentConfirmsFromPartiallyConfirmed() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status("PARTIALLY_CONFIRMED")
                    .confirmedByOpponentIds(p3.toString())
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .createdAt(Instant.now())
                    .games(java.util.List.of())
                    .build();

            var confirmedMatch = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status("CONFIRMED")
                    .confirmedByOpponentIds(p3.toString() + "," + p4.toString())
                    .confirmedByUserId(p4)
                    .confirmedAt(Instant.now())
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .createdAt(Instant.now())
                    .games(java.util.List.of())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchOperation.confirmMatch(any(Match.class), eq(p4))).thenReturn(confirmedMatch);

            var response = matchService.confirmMatch(matchId, p4, "idem-2v2-final");

            assertThat(response.status()).isEqualTo("CONFIRMED");
            verify(matchOperation).confirmMatch(match, p4);
        }

        @Test
        @DisplayName("[P0] AC1: Should set cooldownExpiresAt when 2v2 standard first opponent confirms")
        void shouldSetCooldown_when2v2StandardFirstOpponentConfirms() {
            var matchId = UUID.randomUUID();
            var beforeCooldown = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status("PENDING_APPROVAL")
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .createdAt(Instant.now())
                    .games(java.util.List.of())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(beforeCooldown));
            when(matchOperation.confirmMatch(any(Match.class), eq(p3))).thenAnswer(invocation -> {
                Match m = invocation.getArgument(0);
                m.confirmByOpponent(p3);
                return m;
            });

            var response = matchService.confirmMatch(matchId, p3, "idem-cooldown-set");

            assertThat(response.status()).isEqualTo("PARTIALLY_CONFIRMED");
            assertThat(response.cooldownExpiresAt()).isNotNull();
            assertThat(response.cooldownExpiresAt()).isAfter(Instant.now());
        }

        @Test
        @DisplayName("[P0] AC2: Should clear cooldownExpiresAt when second opponent confirms before expiry")
        void shouldClearCooldown_whenSecondOpponentConfirmsBeforeExpiry() {
            var matchId = UUID.randomUUID();
            var cooldownExpiresAt = Instant.now().plusSeconds(60);
            var partiallyConfirmed = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status("PARTIALLY_CONFIRMED")
                    .confirmedByOpponentIds(p3.toString())
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .cooldownExpiresAt(cooldownExpiresAt)
                    .createdAt(Instant.now())
                    .games(java.util.List.of())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(partiallyConfirmed));
            when(matchOperation.confirmMatch(any(Match.class), eq(p4))).thenAnswer(invocation -> {
                Match m = invocation.getArgument(0);
                m.confirmByOpponent(p4);
                return m;
            });

            var response = matchService.confirmMatch(matchId, p4, "idem-cooldown-clear");

            assertThat(response.status()).isEqualTo("CONFIRMED");
            assertThat(response.cooldownExpiresAt()).isNull();
        }

        @Test
        @DisplayName("[P0] AC4: Should not set cooldown for 1v1 participant match")
        void shouldNotSetCooldown_when1v1ParticipantConfirms() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status("PENDING_APPROVAL")
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .createdAt(Instant.now())
                    .games(java.util.List.of())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchOperation.confirmMatch(any(Match.class), eq(p2))).thenAnswer(invocation -> {
                Match m = invocation.getArgument(0);
                m.confirmByOpponent(p2);
                return m;
            });

            var response = matchService.confirmMatch(matchId, p2, "idem-1v1");

            assertThat(response.status()).isEqualTo("CONFIRMED");
            assertThat(response.cooldownExpiresAt()).isNull();
        }

        @Test
        @DisplayName("[P0] AC5: Should not modify cooldownExpiresAt when already confirmed")
        void shouldNotModifyCooldown_whenAlreadyConfirmed() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("CONFIRMED")
                    .confirmedByUserId(p2)
                    .confirmedAt(Instant.now())
                    .cooldownExpiresAt(Instant.now().plusSeconds(60))
                    .createdAt(Instant.now())
                    .games(java.util.List.of())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

            var response = matchService.confirmMatch(matchId, p2, "idem-idempotent");

            assertThat(response.status()).isEqualTo("CONFIRMED");
            assertThat(response.cooldownExpiresAt()).isNotNull();
            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P0] Should return rejected matches created by current user")
        void shouldReturnRejectedMatches_whenUserIsCreator() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("REJECTED")
                    .rejectedByUserId(p2)
                    .rejectedAt(Instant.now())
                    .rejectionReason("Wrong score")
                    .createdAt(Instant.now())
                    .games(List.of())
                    .build();

            when(matchRepository.findByStatusIn(List.of(Match.STATUS_PENDING_APPROVAL, Match.STATUS_PARTIALLY_CONFIRMED))).thenReturn(List.of());
            when(matchRepository.findByStatusAndCreatorId("REJECTED", p1)).thenReturn(List.of(match));

            var result = matchService.getPendingMatches(p1);

            assertThat(result.count()).isEqualTo(1);
            assertThat(result.matches().get(0).id()).isEqualTo(matchId);
            verify(matchRepository).findByStatusAndCreatorId("REJECTED", p1);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/matches/{id}/reject Specs")
    class MatchRejectionTests {

        @Test
        @DisplayName("[P0] Should reject match and send push notification to creator when opponent submits valid rejection")
        void shouldRejectMatch_whenOpponentSubmitsValidRejection() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            var rejectedMatch = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("REJECTED")
                    .rejectedByUserId(p2)
                    .rejectedAt(Instant.now())
                    .rejectionReason("Wrong score: Game 1 was 10-5")
                    .createdAt(Instant.now())
                    .build();

            var creatorUser = User.builder().id(p1).nickname("CreatorPlayer").build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchOperation.rejectMatch(eq(matchId), eq(p2), eq("Wrong score"), eq("Game 1 was 10-5"))).thenReturn(rejectedMatch);
            when(userRepository.findById(p1)).thenReturn(Optional.of(creatorUser));

            var request = new com.tictactore.dto.MatchRejectionRequest("Wrong score", "Game 1 was 10-5");
            var response = matchService.rejectMatch(matchId, p2, request, "idem-reject-1");

            assertThat(response.status()).isEqualTo("REJECTED");
            assertThat(response.rejectedByUserId()).isEqualTo(p2);
            assertThat(response.rejectionReason()).isEqualTo("Wrong score: Game 1 was 10-5");

            verify(matchOperation).rejectMatch(matchId, p2, "Wrong score", "Game 1 was 10-5");
            verify(pushNotificationService).sendRejectionNotification(rejectedMatch, creatorUser, "Wrong score: Game 1 was 10-5");
        }

        @Test
        @DisplayName("[P1] Should throw UnauthorizedMatchActionException when creator tries to reject")
        void shouldThrowUnauthorized_whenCreatorTriesToReject() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("PENDING_APPROVAL")
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchOperation.rejectMatch(eq(matchId), eq(p1), any(), any()))
                    .thenThrow(new com.tictactore.exception.UnauthorizedMatchActionException("User " + p1 + " is not an opponent for match " + matchId));

            var request = new com.tictactore.dto.MatchRejectionRequest("Wrong score", null);
            assertThatThrownBy(() -> matchService.rejectMatch(matchId, p1, request, null))
                    .isInstanceOf(com.tictactore.exception.UnauthorizedMatchActionException.class);
        }

        @Test
        @DisplayName("[P1] Should throw UnauthorizedMatchActionException when non-participant tries to reject")
        void shouldThrowUnauthorized_whenNonParticipantTriesToReject() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("PENDING_APPROVAL")
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));
            when(matchOperation.rejectMatch(eq(matchId), eq(p3), any(), any()))
                    .thenThrow(new com.tictactore.exception.UnauthorizedMatchActionException("User " + p3 + " is not an opponent for match " + matchId));

            var request = new com.tictactore.dto.MatchRejectionRequest("Wrong score", null);
            assertThatThrownBy(() -> matchService.rejectMatch(matchId, p3, request, null))
                    .isInstanceOf(com.tictactore.exception.UnauthorizedMatchActionException.class);
        }

        @Test
        @DisplayName("[P1] Should return rejected match when already rejected by same opponent (idempotency)")
        void shouldReturnRejectedMatch_whenAlreadyRejectedBySameOpponent() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("REJECTED")
                    .rejectedByUserId(p2)
                    .rejectedAt(Instant.now())
                    .rejectionReason("Wrong score")
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

            var request = new com.tictactore.dto.MatchRejectionRequest("Wrong score", null);
            var response = matchService.rejectMatch(matchId, p2, request, "idem-reject-1");

            assertThat(response.status()).isEqualTo("REJECTED");
            assertThat(response.rejectedByUserId()).isEqualTo(p2);
            verifyNoInteractions(matchOperation);
        }

        @Test
        @DisplayName("[P1] Should throw InvalidMatchStateException when match is already confirmed")
        void shouldThrowInvalidState_whenMatchIsAlreadyConfirmed() {
            var matchId = UUID.randomUUID();
            var match = Match.builder()
                    .id(matchId)
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .status("CONFIRMED")
                    .confirmedByUserId(p2)
                    .confirmedAt(Instant.now())
                    .build();

            when(matchRepository.findById(matchId)).thenReturn(Optional.of(match));

            var request = new com.tictactore.dto.MatchRejectionRequest("Wrong score", null);
            assertThatThrownBy(() -> matchService.rejectMatch(matchId, p2, request, null))
                    .isInstanceOf(com.tictactore.exception.InvalidMatchStateException.class);

            verifyNoInteractions(matchOperation);
        }
    }
}
