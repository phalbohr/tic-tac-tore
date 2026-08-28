package com.tictactore.service;

import com.tictactore.dto.CreatePoolRequest;
import com.tictactore.dto.PoolResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.MatchmakingPool;
import com.tictactore.model.MatchType;
import com.tictactore.model.PoolParticipant;
import com.tictactore.model.PoolParticipantRole;
import com.tictactore.model.PoolStatus;
import com.tictactore.model.SkillLevel;
import com.tictactore.model.StartCondition;
import com.tictactore.model.User;
import com.tictactore.repository.MatchmakingPoolRepository;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PoolServiceImpl ATDD Unit Tests — Join & Active Pools Specs")
class PoolServiceTest {

    @Mock
    private MatchmakingPoolRepository matchmakingPoolRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PoolServiceImpl poolService;

    private UUID creatorId;
    private User creatorUser;
    private UUID joinerId;
    private User joinerUser;

    @BeforeEach
    void setUp() {
        creatorId = UUID.randomUUID();
        creatorUser = User.builder()
                .id(creatorId)
                .email("host@example.com")
                .nickname("HostPlayer")
                .avatar("avatar-1")
                .build();

        joinerId = UUID.randomUUID();
        joinerUser = User.builder()
                .id(joinerId)
                .email("joiner@example.com")
                .nickname("JoinerPlayer")
                .avatar("avatar-2")
                .build();
    }

    @Nested
    @DisplayName("getActivePools Business Logic Specs (AC 1)")
    class GetActivePoolsSpecs {

        @Test
        @DisplayName("Should return all open pools mapped to PoolResponse ordered by createdAt desc")
        void shouldReturnActivePoolsList() {
            MatchmakingPool pool1 = MatchmakingPool.builder()
                    .id(UUID.randomUUID())
                    .creator(creatorUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .startCondition(StartCondition.FILL_BASED)
                    .skillLevel(SkillLevel.OPEN_FOR_ALL)
                    .status(PoolStatus.OPEN)
                    .participants(new ArrayList<>())
                    .createdAt(Instant.now().minusSeconds(60))
                    .build();
            pool1.addParticipant(PoolParticipant.builder()
                    .id(UUID.randomUUID())
                    .pool(pool1)
                    .user(creatorUser)
                    .role(PoolParticipantRole.HOST)
                    .joinedAt(Instant.now().minusSeconds(60))
                    .build());

            when(matchmakingPoolRepository.findByStatusOrderByCreatedAtDesc(eq(PoolStatus.OPEN)))
                    .thenReturn(List.of(pool1));

            List<PoolResponse> responses = poolService.getActivePools();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).id()).isEqualTo(pool1.getId());
            assertThat(responses.get(0).status()).isEqualTo(PoolStatus.OPEN);
            assertThat(responses.get(0).currentPlayers()).isEqualTo(1);
            assertThat(responses.get(0).requiredPlayers()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("joinPool Business Logic Specs (AC 2, AC 3, AC 4, AC 5, AC 6)")
    class JoinPoolSpecs {

        @Test
        @DisplayName("Should successfully add player to open 1v1 pool and transition status to FILLED")
        void shouldJoinPoolAndTransitionToFilled() {
            UUID poolId = UUID.randomUUID();
            MatchmakingPool pool = MatchmakingPool.builder()
                    .id(poolId)
                    .creator(creatorUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .startCondition(StartCondition.FILL_BASED)
                    .skillLevel(SkillLevel.OPEN_FOR_ALL)
                    .status(PoolStatus.OPEN)
                    .participants(new ArrayList<>())
                    .createdAt(Instant.now().minusSeconds(120))
                    .build();
            PoolParticipant host = PoolParticipant.builder()
                    .id(UUID.randomUUID())
                    .pool(pool)
                    .user(creatorUser)
                    .role(PoolParticipantRole.HOST)
                    .joinedAt(Instant.now().minusSeconds(120))
                    .build();
            pool.addParticipant(host);

            when(matchmakingPoolRepository.findById(eq(poolId))).thenReturn(Optional.of(pool));
            when(userRepository.findById(eq(joinerId))).thenReturn(Optional.of(joinerUser));
            when(matchmakingPoolRepository.save(any(MatchmakingPool.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PoolResponse response = poolService.joinPool(poolId, joinerId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(poolId);
            assertThat(response.status()).isEqualTo(PoolStatus.FILLED);
            assertThat(response.currentPlayers()).isEqualTo(2);
            assertThat(response.requiredPlayers()).isEqualTo(2);
            assertThat(response.participants()).hasSize(2);
            assertThat(response.participants())
                    .extracting(p -> p.userId())
                    .containsExactlyInAnyOrder(creatorId, joinerId);
            verify(matchmakingPoolRepository).save(pool);
        }

        @Test
        @DisplayName("Should successfully add player to open 2v2 pool without marking FILLED when slots remain")
        void shouldJoinPoolWithOpenSlotsRemaining() {
            UUID poolId = UUID.randomUUID();
            MatchmakingPool pool = MatchmakingPool.builder()
                    .id(poolId)
                    .creator(creatorUser)
                    .matchType(MatchType.TWO_VS_TWO)
                    .startCondition(StartCondition.FILL_BASED)
                    .skillLevel(SkillLevel.OPEN_FOR_ALL)
                    .status(PoolStatus.OPEN)
                    .participants(new ArrayList<>())
                    .createdAt(Instant.now().minusSeconds(120))
                    .build();
            PoolParticipant host = PoolParticipant.builder()
                    .id(UUID.randomUUID())
                    .pool(pool)
                    .user(creatorUser)
                    .role(PoolParticipantRole.HOST)
                    .joinedAt(Instant.now().minusSeconds(120))
                    .build();
            pool.addParticipant(host);

            when(matchmakingPoolRepository.findById(eq(poolId))).thenReturn(Optional.of(pool));
            when(userRepository.findById(eq(joinerId))).thenReturn(Optional.of(joinerUser));
            when(matchmakingPoolRepository.save(any(MatchmakingPool.class))).thenAnswer(invocation -> invocation.getArgument(0));

            PoolResponse response = poolService.joinPool(poolId, joinerId);

            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(PoolStatus.OPEN);
            assertThat(response.currentPlayers()).isEqualTo(2);
            assertThat(response.requiredPlayers()).isEqualTo(4);
        }

        @Test
        @DisplayName("Should throw IllegalStateException (409 Conflict) when user is already a participant")
        void shouldThrowConflictWhenUserAlreadyParticipant() {
            UUID poolId = UUID.randomUUID();
            MatchmakingPool pool = MatchmakingPool.builder()
                    .id(poolId)
                    .creator(creatorUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .startCondition(StartCondition.FILL_BASED)
                    .skillLevel(SkillLevel.OPEN_FOR_ALL)
                    .status(PoolStatus.OPEN)
                    .participants(new ArrayList<>())
                    .build();
            PoolParticipant host = PoolParticipant.builder()
                    .id(UUID.randomUUID())
                    .pool(pool)
                    .user(creatorUser)
                    .role(PoolParticipantRole.HOST)
                    .joinedAt(Instant.now())
                    .build();
            pool.addParticipant(host);

            when(matchmakingPoolRepository.findById(eq(poolId))).thenReturn(Optional.of(pool));

            assertThatThrownBy(() -> poolService.joinPool(poolId, creatorId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("User is already a participant in this pool");
        }

        @Test
        @DisplayName("Should throw IllegalStateException (409 Conflict) when pool is not in OPEN status")
        void shouldThrowConflictWhenPoolNotOpen() {
            UUID poolId = UUID.randomUUID();
            MatchmakingPool pool = MatchmakingPool.builder()
                    .id(poolId)
                    .creator(creatorUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .startCondition(StartCondition.FILL_BASED)
                    .skillLevel(SkillLevel.OPEN_FOR_ALL)
                    .status(PoolStatus.FILLED)
                    .participants(new ArrayList<>())
                    .build();

            when(matchmakingPoolRepository.findById(eq(poolId))).thenReturn(Optional.of(pool));

            assertThatThrownBy(() -> poolService.joinPool(poolId, joinerId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Pool is no longer open for joining");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when pool ID does not exist")
        void shouldThrowNotFoundWhenPoolDoesNotExist() {
            UUID nonExistentPoolId = UUID.randomUUID();
            when(matchmakingPoolRepository.findById(eq(nonExistentPoolId))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> poolService.joinPool(nonExistentPoolId, joinerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Pool not found");
        }
    }
}
