package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tictactore.dto.CreatePoolRequest;
import com.tictactore.dto.PoolParticipantDto;
import com.tictactore.dto.PoolResponse;
import com.tictactore.exception.GlobalExceptionHandler;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("PoolController ATDD Specifications — Want to Play Matchmaking Pools")
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
        currentUser = User.builder().id(currentUserId).email("creator@example.com").nickname("HostUser").build();
        auth = new UsernamePasswordAuthenticationToken(currentUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("POST /api/v1/pools Endpoint Specs")
    class CreatePoolSpecs {

        @Test
        @DisplayName("[P0] Should return 201 Created when creating fill-based 1v1 pool (AC 1, AC 2)")
        void shouldReturn201OnValidFillBasedPool() throws Exception {
            UUID poolId = UUID.randomUUID();
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.ONE_VS_ONE,
                    StartCondition.FILL_BASED,
                    null,
                    SkillLevel.OPEN_FOR_ALL
            );
            PoolParticipantDto hostParticipant = new PoolParticipantDto(
                    currentUserId,
                    "HostUser",
                    "avatar-1",
                    PoolParticipantRole.HOST,
                    Instant.now()
            );
            PoolResponse response = new PoolResponse(
                    poolId,
                    currentUserId,
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
            when(poolService.createPool(eq(currentUserId), any(CreatePoolRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/pools")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(poolId.toString()))
                    .andExpect(jsonPath("$.creatorId").value(currentUserId.toString()))
                    .andExpect(jsonPath("$.matchType").value("ONE_VS_ONE"))
                    .andExpect(jsonPath("$.startCondition").value("FILL_BASED"))
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.requiredPlayers").value(2))
                    .andExpect(jsonPath("$.currentPlayers").value(1))
                    .andExpect(jsonPath("$.participants[0].userId").value(currentUserId.toString()))
                    .andExpect(jsonPath("$.participants[0].role").value("HOST"));
        }

        @Test
        @DisplayName("[P0] Should return 201 Created when creating scheduled 2v2 pool with valid future time (AC 3)")
        void shouldReturn201OnValidScheduledPool() throws Exception {
            UUID poolId = UUID.randomUUID();
            Instant scheduledTime = Instant.now().plus(2, ChronoUnit.DAYS);
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.TWO_VS_TWO,
                    StartCondition.SCHEDULED_TIME,
                    scheduledTime,
                    SkillLevel.ADVANCED
            );
            PoolParticipantDto hostParticipant = new PoolParticipantDto(
                    currentUserId,
                    "HostUser",
                    "avatar-1",
                    PoolParticipantRole.HOST,
                    Instant.now()
            );
            PoolResponse response = new PoolResponse(
                    poolId,
                    currentUserId,
                    "HostUser",
                    MatchType.TWO_VS_TWO,
                    StartCondition.SCHEDULED_TIME,
                    scheduledTime,
                    SkillLevel.ADVANCED,
                    PoolStatus.OPEN,
                    4,
                    1,
                    List.of(hostParticipant),
                    Instant.now()
            );
            when(poolService.createPool(eq(currentUserId), any(CreatePoolRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/pools")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(poolId.toString()))
                    .andExpect(jsonPath("$.matchType").value("TWO_VS_TWO"))
                    .andExpect(jsonPath("$.startCondition").value("SCHEDULED_TIME"))
                    .andExpect(jsonPath("$.skillLevel").value("ADVANCED"))
                    .andExpect(jsonPath("$.requiredPlayers").value(4))
                    .andExpect(jsonPath("$.currentPlayers").value(1));
        }

        @Test
        @DisplayName("[P0] Should return 400 Bad Request when creator quota of 3 active pools is exceeded (AC 5)")
        void shouldReturn400WhenMaxActivePoolsExceeded() throws Exception {
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.ONE_VS_ONE,
                    StartCondition.FILL_BASED,
                    null,
                    SkillLevel.OPEN_FOR_ALL
            );
            when(poolService.createPool(eq(currentUserId), any(CreatePoolRequest.class)))
                    .thenThrow(new IllegalArgumentException("Maximum active pools limit reached (3)"));

            mockMvc.perform(post("/api/v1/pools")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Maximum active pools limit reached (3)"));
        }

        @Test
        @DisplayName("[P1] Should return 400 Bad Request when scheduled time is in the past (AC 4)")
        void shouldReturn400WhenScheduledTimeInPast() throws Exception {
            Instant pastTime = Instant.now().minus(1, ChronoUnit.HOURS);
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.ONE_VS_ONE,
                    StartCondition.SCHEDULED_TIME,
                    pastTime,
                    SkillLevel.OPEN_FOR_ALL
            );
            when(poolService.createPool(eq(currentUserId), any(CreatePoolRequest.class)))
                    .thenThrow(new IllegalArgumentException("Scheduled time must be in the future (within 7 days)"));

            mockMvc.perform(post("/api/v1/pools")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[P1] Should return 401 Unauthorized when unauthenticated (AC 4)")
        @WithAnonymousUser
        void shouldReturn401WhenUnauthenticated() throws Exception {
            SecurityContextHolder.clearContext();
            CreatePoolRequest request = new CreatePoolRequest(
                    MatchType.ONE_VS_ONE,
                    StartCondition.FILL_BASED,
                    null,
                    SkillLevel.OPEN_FOR_ALL
            );

            mockMvc.perform(post("/api/v1/pools")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/pools/{id} Endpoint Specs")
    class GetPoolByIdSpecs {

        @Test
        @DisplayName("[P0] Should return 200 OK with pool details when pool exists")
        void shouldReturn200WithPoolDetails() throws Exception {
            UUID poolId = UUID.randomUUID();
            PoolParticipantDto hostParticipant = new PoolParticipantDto(
                    currentUserId,
                    "HostUser",
                    "avatar-1",
                    PoolParticipantRole.HOST,
                    Instant.now()
            );
            PoolResponse response = new PoolResponse(
                    poolId,
                    currentUserId,
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
            when(poolService.getPoolById(eq(poolId))).thenReturn(response);

            mockMvc.perform(get("/api/v1/pools/{id}", poolId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(poolId.toString()))
                    .andExpect(jsonPath("$.matchType").value("ONE_VS_ONE"))
                    .andExpect(jsonPath("$.status").value("OPEN"))
                    .andExpect(jsonPath("$.requiredPlayers").value(2));
        }

        @Test
        @DisplayName("[P1] Should return 401 Unauthorized when unauthenticated")
        @WithAnonymousUser
        void shouldReturn401WhenUnauthenticatedOnGet() throws Exception {
            SecurityContextHolder.clearContext();
            UUID poolId = UUID.randomUUID();

            mockMvc.perform(get("/api/v1/pools/{id}", poolId)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
