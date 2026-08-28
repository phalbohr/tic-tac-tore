package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tictactore.dto.PoolParticipantDto;
import com.tictactore.dto.PoolResponse;
import com.tictactore.exception.GlobalExceptionHandler;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.MatchType;
import com.tictactore.model.PoolParticipantRole;
import com.tictactore.model.PoolStatus;
import com.tictactore.model.SkillLevel;
import com.tictactore.model.StartCondition;
import com.tictactore.model.User;
import com.tictactore.service.PoolService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("PoolController ATDD Specifications — Story 6.4: Active Pools & Join API")
class PoolControllerATDDTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PoolService poolService;

    @InjectMocks
    private PoolController poolController;

    private UUID currentUserId;
    private User currentUser;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(poolController)
                .setCustomArgumentResolvers(new org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        currentUserId = UUID.randomUUID();
        currentUser = User.builder().id(currentUserId).email("player@example.com").nickname("PlayerOne").build();
        auth = new UsernamePasswordAuthenticationToken(currentUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /api/v1/pools Endpoint Specs (AC 1)")
    class GetActivePoolsSpecs {

        @Test
        @DisplayName("[P0] Should return 200 OK with list of active pools for authenticated user")
        void shouldReturnActivePoolsList() throws Exception {
            UUID poolId = UUID.randomUUID();
            PoolParticipantDto hostParticipant = new PoolParticipantDto(
                    UUID.randomUUID(),
                    "HostUser",
                    "avatar-1",
                    PoolParticipantRole.HOST,
                    Instant.now()
            );
            PoolResponse poolResponse = new PoolResponse(
                    poolId,
                    hostParticipant.userId(),
                    "HostUser",
                    MatchType.ONE_VS_ONE,
                    StartCondition.FILL_BASED,
                    null,
                    SkillLevel.OPEN_FOR_ALL,
                    PoolStatus.OPEN,
                    2,
                    1,
                    List.of(hostParticipant),
                    Instant.now()
            );
            when(poolService.getActivePools()).thenReturn(List.of(poolResponse));

            mockMvc.perform(get("/api/v1/pools")
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(poolId.toString()))
                    .andExpect(jsonPath("$[0].status").value("OPEN"))
                    .andExpect(jsonPath("$[0].matchType").value("ONE_VS_ONE"))
                    .andExpect(jsonPath("$[0].currentPlayers").value(1))
                    .andExpect(jsonPath("$[0].requiredPlayers").value(2));
        }

        @Test
        @DisplayName("[P1] Should return 401 Unauthorized when unauthenticated on GET /api/v1/pools")
        @WithAnonymousUser
        void shouldReturn401WhenUnauthenticated() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(get("/api/v1/pools")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/pools/{id}/join Endpoint Specs (AC 2, AC 3, AC 4, AC 5)")
    class JoinPoolSpecs {

        @Test
        @DisplayName("[P0] Should return 200 OK with updated PoolResponse upon successful join (AC 2, AC 3)")
        void shouldReturn200OnSuccessfulJoin() throws Exception {
            UUID poolId = UUID.randomUUID();
            UUID hostId = UUID.randomUUID();
            PoolParticipantDto hostParticipant = new PoolParticipantDto(
                    hostId,
                    "HostUser",
                    "avatar-1",
                    PoolParticipantRole.HOST,
                    Instant.now().minusSeconds(60)
            );
            PoolParticipantDto joinedParticipant = new PoolParticipantDto(
                    currentUserId,
                    "PlayerOne",
                    "avatar-2",
                    PoolParticipantRole.PLAYER,
                    Instant.now()
            );
            PoolResponse updatedResponse = new PoolResponse(
                    poolId,
                    hostId,
                    "HostUser",
                    MatchType.ONE_VS_ONE,
                    StartCondition.FILL_BASED,
                    null,
                    SkillLevel.OPEN_FOR_ALL,
                    PoolStatus.FILLED,
                    2,
                    2,
                    List.of(hostParticipant, joinedParticipant),
                    Instant.now().minusSeconds(60)
            );
            when(poolService.joinPool(eq(poolId), eq(currentUserId))).thenReturn(updatedResponse);

            mockMvc.perform(post("/api/v1/pools/{id}/join", poolId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(poolId.toString()))
                    .andExpect(jsonPath("$.status").value("FILLED"))
                    .andExpect(jsonPath("$.currentPlayers").value(2))
                    .andExpect(jsonPath("$.participants.length()").value(2))
                    .andExpect(jsonPath("$.participants[1].userId").value(currentUserId.toString()))
                    .andExpect(jsonPath("$.participants[1].role").value("PLAYER"));
        }

        @Test
        @DisplayName("[P0] Should return 409 Conflict when user already participates in pool (AC 4)")
        void shouldReturn409WhenUserAlreadyParticipant() throws Exception {
            UUID poolId = UUID.randomUUID();
            when(poolService.joinPool(eq(poolId), eq(currentUserId)))
                    .thenThrow(new IllegalStateException("User is already a participant in this pool"));

            mockMvc.perform(post("/api/v1/pools/{id}/join", poolId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("User is already a participant in this pool"));
        }

        @Test
        @DisplayName("[P0] Should return 409 Conflict when pool is no longer open for joining (AC 5)")
        void shouldReturn409WhenPoolNotOpen() throws Exception {
            UUID poolId = UUID.randomUUID();
            when(poolService.joinPool(eq(poolId), eq(currentUserId)))
                    .thenThrow(new IllegalStateException("Pool is no longer open for joining"));

            mockMvc.perform(post("/api/v1/pools/{id}/join", poolId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Pool is no longer open for joining"));
        }

        @Test
        @DisplayName("[P1] Should return 404 Not Found when pool ID does not exist")
        void shouldReturn404WhenPoolNotFound() throws Exception {
            UUID nonExistentPoolId = UUID.randomUUID();
            when(poolService.joinPool(eq(nonExistentPoolId), eq(currentUserId)))
                    .thenThrow(new ResourceNotFoundException("Pool not found: " + nonExistentPoolId));

            mockMvc.perform(post("/api/v1/pools/{id}/join", nonExistentPoolId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("[P1] Should return 401 Unauthorized when unauthenticated")
        @WithAnonymousUser
        void shouldReturn401WhenUnauthenticated() throws Exception {
            SecurityContextHolder.clearContext();
            UUID poolId = UUID.randomUUID();

            mockMvc.perform(post("/api/v1/pools/{id}/join", poolId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
