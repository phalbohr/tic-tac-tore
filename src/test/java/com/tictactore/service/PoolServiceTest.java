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
import java.time.temporal.ChronoUnit;
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
@DisplayName("PoolServiceImpl ATDD Unit Tests")
class PoolServiceTest {

    @Mock
    private MatchmakingPoolRepository matchmakingPoolRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PoolServiceImpl poolService;

    private UUID creatorId;
    private User creatorUser;

    @BeforeEach
    void setUp() {
        creatorId = UUID.randomUUID();
        creatorUser = User.builder()
                .id(creatorId)
                .email("host@example.com")
                .nickname("HostPlayer")
                .avatar("avatar-1")
                .build();
    }

    @Nested
    @DisplayName("createPool Business Logic Specs")
    class CreatePoolSpecs {

        @Test
        @DisplayName("Should create 1v1 fill-based pool and attach creator as HOST")
        void shouldCreateFillBasedPoolSuccessfully() {
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.ONE_VS_ONE,
                    StartCondition.FILL_BASED,
                    null,
                    SkillLevel.OPEN_FOR_ALL
            );
            when(matchmakingPoolRepository.countByCreatorIdAndStatus(eq(creatorId), eq(PoolStatus.OPEN))).thenReturn(0L);
            when(userRepository.findById(eq(creatorId))).thenReturn(Optional.of(creatorUser));

            MatchmakingPool savedPool = MatchmakingPool.builder()
                    .id(UUID.randomUUID())
                    .creator(creatorUser)
                    .matchType(MatchType.ONE_VS_ONE)
                    .startCondition(StartCondition.FILL_BASED)
                    .skillLevel(SkillLevel.OPEN_FOR_ALL)
                    .status(PoolStatus.OPEN)
                    .participants(new ArrayList<>())
                    .createdAt(Instant.now())
                    .build();
            PoolParticipant host = PoolParticipant.builder()
                    .id(UUID.randomUUID())
                    .pool(savedPool)
                    .user(creatorUser)
                    .role(PoolParticipantRole.HOST)
                    .joinedAt(Instant.now())
                    .build();
            savedPool.getParticipants().add(host);

            when(matchmakingPoolRepository.save(any(MatchmakingPool.class))).thenReturn(savedPool);

            PoolResponse response = poolService.createPool(creatorId, request);

            assertThat(response).isNotNull();
            assertThat(response.creatorId()).isEqualTo(creatorId);
            assertThat(response.matchType()).isEqualTo(MatchType.ONE_VS_ONE);
            assertThat(response.startCondition()).isEqualTo(StartCondition.FILL_BASED);
            assertThat(response.status()).isEqualTo(PoolStatus.OPEN);
            assertThat(response.requiredPlayers()).isEqualTo(2);
            assertThat(response.currentPlayers()).isEqualTo(1);
            assertThat(response.participants()).hasSize(1);
            assertThat(response.participants().get(0).role()).isEqualTo(PoolParticipantRole.HOST);
            verify(matchmakingPoolRepository).save(any(MatchmakingPool.class));
            verify(eventPublisher).publishEvent(any(com.tictactore.event.PoolCreatedEvent.class));
        }

        @Test
        @DisplayName("Should create 2v2 scheduled pool with future timestamp")
        void shouldCreateScheduledPoolSuccessfully() {
            Instant scheduledTime = Instant.now().plus(3, ChronoUnit.DAYS);
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.TWO_VS_TWO,
                    StartCondition.SCHEDULED_TIME,
                    scheduledTime,
                    SkillLevel.INTERMEDIATE
            );
            when(matchmakingPoolRepository.countByCreatorIdAndStatus(eq(creatorId), eq(PoolStatus.OPEN))).thenReturn(1L);
            when(userRepository.findById(eq(creatorId))).thenReturn(Optional.of(creatorUser));

            MatchmakingPool savedPool = MatchmakingPool.builder()
                    .id(UUID.randomUUID())
                    .creator(creatorUser)
                    .matchType(MatchType.TWO_VS_TWO)
                    .startCondition(StartCondition.SCHEDULED_TIME)
                    .scheduledTime(scheduledTime)
                    .skillLevel(SkillLevel.INTERMEDIATE)
                    .status(PoolStatus.OPEN)
                    .participants(new ArrayList<>())
                    .createdAt(Instant.now())
                    .build();
            PoolParticipant host = PoolParticipant.builder()
                    .id(UUID.randomUUID())
                    .pool(savedPool)
                    .user(creatorUser)
                    .role(PoolParticipantRole.HOST)
                    .joinedAt(Instant.now())
                    .build();
            savedPool.getParticipants().add(host);

            when(matchmakingPoolRepository.save(any(MatchmakingPool.class))).thenReturn(savedPool);

            PoolResponse response = poolService.createPool(creatorId, request);

            assertThat(response).isNotNull();
            assertThat(response.matchType()).isEqualTo(MatchType.TWO_VS_TWO);
            assertThat(response.startCondition()).isEqualTo(StartCondition.SCHEDULED_TIME);
            assertThat(response.scheduledTime()).isEqualTo(scheduledTime);
            assertThat(response.requiredPlayers()).isEqualTo(4);
            assertThat(response.currentPlayers()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when creator already has 3 active open pools")
        void shouldThrowExceptionWhenActivePoolQuotaExceeded() {
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.ONE_VS_ONE,
                    StartCondition.FILL_BASED,
                    null,
                    SkillLevel.OPEN_FOR_ALL
            );
            when(matchmakingPoolRepository.countByCreatorIdAndStatus(eq(creatorId), eq(PoolStatus.OPEN))).thenReturn(3L);

            assertThatThrownBy(() -> poolService.createPool(creatorId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Maximum active pools limit reached (3)");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when scheduledTime is missing for SCHEDULED_TIME")
        void shouldThrowExceptionWhenScheduledTimeMissing() {
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.ONE_VS_ONE,
                    StartCondition.SCHEDULED_TIME,
                    null,
                    SkillLevel.OPEN_FOR_ALL
            );

            assertThatThrownBy(() -> poolService.createPool(creatorId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Scheduled time is required for scheduled pools");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when scheduledTime is in past")
        void shouldThrowExceptionWhenScheduledTimeInPast() {
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.ONE_VS_ONE,
                    StartCondition.SCHEDULED_TIME,
                    Instant.now().minus(10, ChronoUnit.MINUTES),
                    SkillLevel.OPEN_FOR_ALL
            );

            assertThatThrownBy(() -> poolService.createPool(creatorId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Scheduled time must be in the future");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when scheduledTime is beyond 7 days")
        void shouldThrowExceptionWhenScheduledTimeTooFarInFuture() {
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.ONE_VS_ONE,
                    StartCondition.SCHEDULED_TIME,
                    Instant.now().plus(8, ChronoUnit.DAYS),
                    SkillLevel.OPEN_FOR_ALL
            );

            assertThatThrownBy(() -> poolService.createPool(creatorId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Scheduled time cannot exceed 7 days");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when fill-based pool specifies scheduledTime")
        void shouldThrowExceptionWhenFillBasedPoolHasScheduledTime() {
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.ONE_VS_ONE,
                    StartCondition.FILL_BASED,
                    Instant.now().plus(1, ChronoUnit.DAYS),
                    SkillLevel.OPEN_FOR_ALL
            );

            assertThatThrownBy(() -> poolService.createPool(creatorId, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Scheduled time must not be set for fill-based pools");
        }
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
            UUID joinerId = UUID.randomUUID();
            User joinerUser = User.builder()
                    .id(joinerId)
                    .email("joiner@example.com")
                    .nickname("JoinerPlayer")
                    .avatar("avatar-2")
                    .build();

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
            verify(eventPublisher).publishEvent(any(com.tictactore.event.PoolFilledEvent.class));
        }

        @Test
        @DisplayName("Should successfully add player to open 2v2 pool without marking FILLED when slots remain")
        void shouldJoinPoolWithOpenSlotsRemaining() {
            UUID joinerId = UUID.randomUUID();
            User joinerUser = User.builder()
                    .id(joinerId)
                    .email("joiner@example.com")
                    .nickname("JoinerPlayer")
                    .avatar("avatar-2")
                    .build();

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
            org.mockito.Mockito.verify(eventPublisher, org.mockito.Mockito.never()).publishEvent(any(com.tictactore.event.PoolFilledEvent.class));
        }

        @Test
        @DisplayName("Should throw PoolConflictException (409 Conflict) when user is already a participant")
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
            User joinerUser = User.builder().id(creatorId).build();

            when(matchmakingPoolRepository.findById(eq(poolId))).thenReturn(Optional.of(pool));
            when(userRepository.findById(eq(creatorId))).thenReturn(Optional.of(joinerUser));

            assertThatThrownBy(() -> poolService.joinPool(poolId, creatorId))
                    .isInstanceOf(com.tictactore.exception.PoolConflictException.class)
                    .hasMessageContaining("User is already a participant in this pool");
        }

        @Test
        @DisplayName("Should throw PoolConflictException (409 Conflict) when pool is not in OPEN status")
        void shouldThrowConflictWhenPoolNotOpen() {
            UUID joinerId = UUID.randomUUID();
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
            User joinerUser = User.builder().id(joinerId).build();

            when(matchmakingPoolRepository.findById(eq(poolId))).thenReturn(Optional.of(pool));
            when(userRepository.findById(eq(joinerId))).thenReturn(Optional.of(joinerUser));

            assertThatThrownBy(() -> poolService.joinPool(poolId, joinerId))
                    .isInstanceOf(com.tictactore.exception.PoolConflictException.class)
                    .hasMessageContaining("Pool is no longer open for joining");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when pool ID does not exist")
        void shouldThrowNotFoundWhenPoolDoesNotExist() {
            UUID joinerId = UUID.randomUUID();
            UUID nonExistentPoolId = UUID.randomUUID();
            when(matchmakingPoolRepository.findById(eq(nonExistentPoolId))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> poolService.joinPool(nonExistentPoolId, joinerId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Pool not found");
        }
    }
}
