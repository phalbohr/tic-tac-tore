package com.tictactore.service;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.GameDto;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Match Duplicate Detection ATDD Tests")
class MatchServiceDuplicateDetectionATDDTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MatchOperation matchOperation;

    @Mock
    private PushNotificationService pushNotificationService;

    private MatchServiceImpl matchService;

    @BeforeEach
    void setUp() {
        matchService = new MatchServiceImpl(
                matchRepository,
                userRepository,
                matchOperation,
                pushNotificationService
        );
    }

    @Nested
    @DisplayName("Same-day Duplicate Detection (1v1)")
    class SameDayDuplicateDetection1v1 {

        @Test
        @DisplayName("[P0] Should flag isDuplicateWarning = true when match submitted with identical participants on same UTC calendar day")
        void shouldFlagDuplicateMatchWarningOnSameUtcDay() {
            UUID playerA = UUID.randomUUID();
            UUID playerB = UUID.randomUUID();

            User userA = User.builder().id(playerA).nickname("Player A").build();
            User userB = User.builder().id(playerB).nickname("Player B").build();

            when(userRepository.findAllById(any())).thenReturn(List.of(userA, userB));

            Match savedMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(playerA)
                    .teamAAttackerId(playerA)
                    .teamBAttackerId(playerB)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            when(matchOperation.saveMatch(any())).thenReturn(savedMatch);

            Match existingDuplicate = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(playerA)
                    .teamAAttackerId(playerA)
                    .teamBAttackerId(playerB)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            when(matchRepository.findDuplicatesOnDate(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(existingDuplicate, savedMatch));

            CreateMatchRequest request = new CreateMatchRequest(
                    "key-1",
                    playerA,
                    playerA, null,
                    playerB, null,
                    List.of(new GameDto(10, 8, null, null, null, null))
            );

            matchService.createMatch(request);

            verify(pushNotificationService).sendConfirmationRequest(eq(savedMatch), anyList(), eq(true));
        }

        @Test
        @DisplayName("[P1] Should NOT flag duplicate warning when no existing matches on same day")
        void shouldNotFlagDuplicateWhenNoExistingMatches() {
            UUID playerA = UUID.randomUUID();
            UUID playerB = UUID.randomUUID();

            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(playerA).build(),
                    User.builder().id(playerB).build()
            ));

            Match savedMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(playerA)
                    .teamAAttackerId(playerA)
                    .teamBAttackerId(playerB)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            when(matchOperation.saveMatch(any())).thenReturn(savedMatch);
            when(matchRepository.findDuplicatesOnDate(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(savedMatch));

            CreateMatchRequest request = new CreateMatchRequest(
                    "key-1",
                    playerA,
                    playerA, null,
                    playerB, null,
                    List.of(new GameDto(10, 8, null, null, null, null))
            );

            matchService.createMatch(request);

            verify(pushNotificationService).sendConfirmationRequest(eq(savedMatch), anyList(), eq(false));
        }

        @Test
        @DisplayName("[P1] Should NOT flag duplicate warning when existing match has different participants")
        void shouldNotFlagDuplicateWhenParticipantsDiffer() {
            UUID playerA = UUID.randomUUID();
            UUID playerB = UUID.randomUUID();
            UUID playerC = UUID.randomUUID();

            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(playerA).build(),
                    User.builder().id(playerB).build()
            ));

            Match savedMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(playerA)
                    .teamAAttackerId(playerA)
                    .teamBAttackerId(playerB)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            Match differentMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(playerA)
                    .teamAAttackerId(playerA)
                    .teamBAttackerId(playerC)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            when(matchOperation.saveMatch(any())).thenReturn(savedMatch);
            when(matchRepository.findDuplicatesOnDate(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(differentMatch));

            CreateMatchRequest request = new CreateMatchRequest(
                    "key-1",
                    playerA,
                    playerA, null,
                    playerB, null,
                    List.of(new GameDto(10, 8, null, null, null, null))
            );

            matchService.createMatch(request);

            verify(pushNotificationService).sendConfirmationRequest(eq(savedMatch), anyList(), eq(false));
        }
    }

    @Nested
    @DisplayName("Same-day Duplicate Detection (2v2)")
    class SameDayDuplicateDetection2v2 {

        @Test
        @DisplayName("[P1] Should flag isDuplicateWarning = true when 2v2 match has identical participants on same day")
        void shouldFlagDuplicateWarningOnSameUtcDay2v2() {
            UUID playerA = UUID.randomUUID();
            UUID playerB = UUID.randomUUID();
            UUID playerC = UUID.randomUUID();
            UUID playerD = UUID.randomUUID();

            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(playerA).build(),
                    User.builder().id(playerB).build(),
                    User.builder().id(playerC).build(),
                    User.builder().id(playerD).build()
            ));

            Match savedMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(playerA)
                    .teamAAttackerId(playerA)
                    .teamADefenderId(playerB)
                    .teamBAttackerId(playerC)
                    .teamBDefenderId(playerD)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            when(matchOperation.saveMatch(any())).thenReturn(savedMatch);

            Match existingDuplicate = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(playerA)
                    .teamAAttackerId(playerA)
                    .teamADefenderId(playerB)
                    .teamBAttackerId(playerC)
                    .teamBDefenderId(playerD)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            when(matchRepository.findDuplicatesOnDate(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(existingDuplicate, savedMatch));

            CreateMatchRequest request = new CreateMatchRequest(
                    "key-2",
                    playerA,
                    playerA, playerB,
                    playerC, playerD,
                    List.of(new GameDto(10, 8, playerA, playerB, playerC, playerD))
            );

            matchService.createMatch(request);

            verify(pushNotificationService).sendConfirmationRequest(eq(savedMatch), anyList(), eq(true));
        }

        @Test
        @DisplayName("[P1] Should NOT flag duplicate warning when 2v2 match has different team composition")
        void shouldNotFlagDuplicateWhenTeamCompositionDiffers() {
            UUID playerA = UUID.randomUUID();
            UUID playerB = UUID.randomUUID();
            UUID playerC = UUID.randomUUID();
            UUID playerD = UUID.randomUUID();

            when(userRepository.findAllById(any())).thenReturn(List.of(
                    User.builder().id(playerA).build(),
                    User.builder().id(playerB).build(),
                    User.builder().id(playerC).build(),
                    User.builder().id(playerD).build()
            ));

            Match savedMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(playerA)
                    .teamAAttackerId(playerA)
                    .teamADefenderId(playerB)
                    .teamBAttackerId(playerC)
                    .teamBDefenderId(playerD)
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            Match differentMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .creatorId(playerA)
                    .teamAAttackerId(playerA)
                    .teamADefenderId(playerB)
                    .teamBAttackerId(playerC)
                    .teamBDefenderId(UUID.randomUUID())
                    .status("PENDING_APPROVAL")
                    .createdAt(Instant.now())
                    .build();

            when(matchOperation.saveMatch(any())).thenReturn(savedMatch);
            when(matchRepository.findDuplicatesOnDate(any(), any(), any(), any(), any(), any()))
                    .thenReturn(List.of(differentMatch));

            CreateMatchRequest request = new CreateMatchRequest(
                    "key-3",
                    playerA,
                    playerA, playerB,
                    playerC, playerD,
                    List.of(new GameDto(10, 8, playerA, playerB, playerC, playerD))
            );

            matchService.createMatch(request);

            verify(pushNotificationService).sendConfirmationRequest(eq(savedMatch), anyList(), eq(false));
        }
    }
}
