package com.tictactore.service;

import com.tictactore.dto.MatchResponse;
import com.tictactore.dto.PagedResponse;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.impl.MatchServiceImpl;
import com.tictactore.service.operation.MatchOperation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchService getMatchHistory Unit Tests")
class MatchServiceGetMatchHistoryTest {

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
        private com.tictactore.repository.PlayerGroupRepository playerGroupRepository;

        @InjectMocks
        private MatchServiceImpl matchService;

        private UUID currentUserId;
        private UUID opponentId;
        private UUID ruleConfigId;

        @BeforeEach
        void setUp() {
                currentUserId = UUID.randomUUID();
                opponentId = UUID.randomUUID();
                ruleConfigId = UUID.randomUUID();
        }

        @Test
        void shouldReturnEmptyPagedResponse_whenCurrentUserIdIsNull() {
                PagedResponse<MatchResponse> response = matchService.getMatchHistory(null, "CONFIRMED", null, null,
                                null, 0, 10);

                assertThat(response.content()).isEmpty();
                assertThat(response.totalElements()).isZero();
        }

        @Test
        void shouldReturnPagedMatchHistoryWithResolvedUserNames_whenMatchesFound() {
                UUID matchId = UUID.randomUUID();
                Match match = Match.builder()
                                .id(matchId)
                                .idempotencyKey("match-1")
                                .creatorId(currentUserId)
                                .teamAAttackerId(currentUserId)
                                .teamBAttackerId(opponentId)
                                .status("CONFIRMED")
                                .createdAt(Instant.now())
                                .build();
                match.addGame(Game.builder().gameOrder(1).teamAScore(10).teamBScore(5).build());

                Page<Match> page = new PageImpl<>(List.of(match), PageRequest.of(0, 10), 1);
                when(matchRepository.findMatchHistory(eq(currentUserId), eq("CONFIRMED"), eq(opponentId),
                                eq(ruleConfigId), eq("1v1"), any()))
                                .thenReturn(page);

                User currentUser = User.builder().id(currentUserId).nickname("Alice").build();
                User opponent = User.builder().id(opponentId).nickname("Bob").build();
                when(userRepository.findAllById(any())).thenReturn(List.of(currentUser, opponent));

                PagedResponse<MatchResponse> response = matchService.getMatchHistory(
                                currentUserId, "CONFIRMED", opponentId, ruleConfigId, "1v1", 0, 10);

                assertThat(response.content()).hasSize(1);
                assertThat(response.content().get(0).id()).isEqualTo(matchId);
                assertThat(response.content().get(0).teamAAttackerNickname()).isEqualTo("Alice");
                assertThat(response.content().get(0).teamBAttackerNickname()).isEqualTo("Bob");
                assertThat(response.totalElements()).isEqualTo(1L);
                assertThat(response.totalPages()).isEqualTo(1);
        }

        @Test
        void shouldMaskDeletedUserAsRetiredPlayer_whenNicknameStartsWithExPlayer() {
                UUID matchId = UUID.randomUUID();
                Match match = Match.builder()
                                .id(matchId)
                                .creatorId(currentUserId)
                                .teamAAttackerId(currentUserId)
                                .teamBAttackerId(opponentId)
                                .status("CONFIRMED")
                                .createdAt(Instant.now())
                                .build();

                Page<Match> page = new PageImpl<>(List.of(match), PageRequest.of(0, 10), 1);
                when(matchRepository.findMatchHistory(eq(currentUserId), eq("CONFIRMED"), any(), any(), any(), any()))
                                .thenReturn(page);

                User currentUser = User.builder().id(currentUserId).nickname("Alice").build();
                User retiredOpponent = User.builder().id(opponentId).nickname("ex-player-9999")
                                .email("deleted@example.com").avatar("http://avatar.png").build();
                when(userRepository.findAllById(any())).thenReturn(List.of(currentUser, retiredOpponent));

                PagedResponse<MatchResponse> response = matchService.getMatchHistory(
                                currentUserId, "CONFIRMED", null, null, null, 0, 10);

                assertThat(response.content()).hasSize(1);
                assertThat(response.content().get(0).teamBAttackerNickname()).isEqualTo("Retired Player");
                assertThat(response.content().get(0).teamBAttackerAvatar()).isNull();
        }

        @Test
        void shouldNeverLeakEmailWhenNicknameIsBlank_andNormalizeBlankMatchTypeToNull() {
                UUID matchId = UUID.randomUUID();
                Match match = Match.builder()
                                .id(matchId)
                                .creatorId(currentUserId)
                                .teamAAttackerId(currentUserId)
                                .teamBAttackerId(opponentId)
                                .status("CONFIRMED")
                                .createdAt(Instant.now())
                                .build();

                Page<Match> page = new PageImpl<>(List.of(match), PageRequest.of(0, 10), 1);
                when(matchRepository.findMatchHistory(eq(currentUserId), eq("CONFIRMED"), any(), any(), eq(null),
                                any()))
                                .thenReturn(page);

                User currentUser = User.builder().id(currentUserId).nickname("Alice").build();
                User noNickUser = User.builder().id(opponentId).nickname("").email("secret_user@example.com").build();
                when(userRepository.findAllById(any())).thenReturn(List.of(currentUser, noNickUser));

                PagedResponse<MatchResponse> response = matchService.getMatchHistory(
                                currentUserId, "CONFIRMED", null, null, "   ", 0, 10);

                assertThat(response.content()).hasSize(1);
                assertThat(response.content().get(0).teamBAttackerNickname()).isEqualTo("Retired Player");
                assertThat(response.content().get(0).teamBAttackerNickname()).doesNotContain("secret_user@example.com");
        }

        @Test
        void shouldThrowAccessDeniedExceptionWhenFilteringByGroupOwnedByAnotherUser() {
                UUID foreignCreatorId = UUID.randomUUID();
                UUID groupId = UUID.randomUUID();
                com.tictactore.model.PlayerGroup foreignGroup = com.tictactore.model.PlayerGroup.builder()
                                .id(groupId)
                                .name("Secret Group")
                                .creatorId(foreignCreatorId)
                                .build();
                when(playerGroupRepository.findById(groupId)).thenReturn(java.util.Optional.of(foreignGroup));

                org.assertj.core.api.Assertions.assertThatThrownBy(() -> matchService.getMatchHistory(
                                currentUserId, "CONFIRMED", null, groupId, null, null, 0, 10))
                                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
        }

        @Test
        void shouldReturnEmptyPageImmediatelyWhenGroupHasNoMembers() {
                UUID groupId = UUID.randomUUID();
                com.tictactore.model.PlayerGroup emptyGroup = com.tictactore.model.PlayerGroup.builder()
                                .id(groupId)
                                .name("Empty Squad")
                                .creatorId(currentUserId)
                                .members(java.util.Set.of())
                                .build();
                when(playerGroupRepository.findById(groupId)).thenReturn(java.util.Optional.of(emptyGroup));

                PagedResponse<MatchResponse> response = matchService.getMatchHistory(
                                currentUserId, "CONFIRMED", null, groupId, null, null, 0, 10);

                assertThat(response.content()).isEmpty();
                assertThat(response.totalElements()).isZero();
        }
}
