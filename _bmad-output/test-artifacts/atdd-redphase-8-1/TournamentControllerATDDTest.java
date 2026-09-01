package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tictactore.dto.CreateTournamentRequest;
import com.tictactore.dto.RuleConfigurationResponse;
import com.tictactore.dto.TournamentResponse;
import com.tictactore.exception.GlobalExceptionHandler;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import com.tictactore.service.TournamentService;
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
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.OffsetDateTime;
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
@DisplayName("TournamentController ATDD Specifications — Tournament Creation & Configuration (Story 8.1)")
class TournamentControllerATDDTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private TournamentService tournamentService;

    @InjectMocks
    private TournamentController tournamentController;

    private final UUID userId = UUID.fromString("a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d");
    private final UUID ruleConfigId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID tournamentId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tournamentController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User mockUser = User.builder()
                .id(userId)
                .email("organizer@example.com")
                .nickname("TournamentMaster")
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                mockUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private RuleConfigurationResponse createSampleRuleConfigResponse() {
        return RuleConfigurationResponse.builder()
                .id(ruleConfigId)
                .name("ITSF Standard Matchplay")
                .type(RuleConfigurationType.PRESET)
                .createdBy(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .goalLimit(5)
                .gameLimit(3)
                .winByTwo(true)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private TournamentResponse createSampleTournamentResponse(TournamentFormat format, TournamentMode mode) {
        return TournamentResponse.builder()
                .id(tournamentId)
                .name("Autumn Championship 2026")
                .format(format)
                .mode(mode)
                .ruleConfiguration(createSampleRuleConfigResponse())
                .minParticipants(4)
                .maxParticipants(16)
                .registrationDeadline(Instant.now().plus(7, ChronoUnit.DAYS))
                .roundCount(format == TournamentFormat.CHAMPIONSHIP ? 5 : null)
                .hasPlayoff(false)
                .status(TournamentStatus.REGISTRATION_OPEN)
                .creatorId(userId)
                .creatorNickname("TournamentMaster")
                .createdAt(OffsetDateTime.now())
                .build();
    }

    private CreateTournamentRequest createValidCupRequest() {
        return CreateTournamentRequest.builder()
                .name("Autumn Championship 2026")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .ruleConfigurationId(ruleConfigId)
                .minParticipants(4)
                .maxParticipants(16)
                .registrationDeadline(Instant.now().plus(7, ChronoUnit.DAYS))
                .hasPlayoff(false)
                .build();
    }

    @Nested
    @DisplayName("AC 2 & AC 3: Create Tournament (POST /api/v1/tournaments)")
    class CreateTournamentScenarios {

        @Test
        @DisplayName("POST /api/v1/tournaments with valid Single Elimination Cup payload should return 201 Created")
        void shouldCreateCupTournament_whenRequestValid() throws Exception {
            var request = createValidCupRequest();
            var response = createSampleTournamentResponse(TournamentFormat.CUP, TournamentMode.ONE_VS_ONE_PERSONAL);
            when(tournamentService.createTournament(eq(userId), any(CreateTournamentRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/tournaments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(tournamentId.toString()))
                    .andExpect(jsonPath("$.name").value("Autumn Championship 2026"))
                    .andExpect(jsonPath("$.format").value("CUP"))
                    .andExpect(jsonPath("$.mode").value("ONE_VS_ONE_PERSONAL"))
                    .andExpect(jsonPath("$.status").value("REGISTRATION_OPEN"))
                    .andExpect(jsonPath("$.creatorId").value(userId.toString()))
                    .andExpect(jsonPath("$.creatorNickname").value("TournamentMaster"))
                    .andExpect(jsonPath("$.ruleConfiguration.id").value(ruleConfigId.toString()));
        }

        @Test
        @DisplayName("POST /api/v1/tournaments with Round Robin Championship payload should return 201 Created")
        void shouldCreateChampionshipTournament_whenRequestValid() throws Exception {
            var request = CreateTournamentRequest.builder()
                    .name("Winter League 2026")
                    .format(TournamentFormat.CHAMPIONSHIP)
                    .mode(TournamentMode.TWO_VS_TWO_FIXED_TEAMS)
                    .ruleConfigurationId(ruleConfigId)
                    .minParticipants(4)
                    .maxParticipants(8)
                    .registrationDeadline(Instant.now().plus(14, ChronoUnit.DAYS))
                    .roundCount(7)
                    .hasPlayoff(true)
                    .build();
            var response = createSampleTournamentResponse(TournamentFormat.CHAMPIONSHIP, TournamentMode.TWO_VS_TWO_FIXED_TEAMS);
            when(tournamentService.createTournament(eq(userId), any(CreateTournamentRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/tournaments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.format").value("CHAMPIONSHIP"))
                    .andExpect(jsonPath("$.mode").value("TWO_VS_TWO_FIXED_TEAMS"));
        }
    }

    @Nested
    @DisplayName("AC 4: Validation Errors (POST /api/v1/tournaments)")
    class ValidationErrorScenarios {

        @Test
        @DisplayName("POST /api/v1/tournaments should return 400 when minParticipants exceeds maxParticipants")
        void shouldReturn400_whenMinParticipantsExceedsMax() throws Exception {
            var request = createValidCupRequest();
            when(tournamentService.createTournament(eq(userId), any(CreateTournamentRequest.class)))
                    .thenThrow(new IllegalArgumentException("minParticipants cannot be greater than maxParticipants"));

            mockMvc.perform(post("/api/v1/tournaments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/tournaments should return 400 when 2v2 mode has minParticipants less than 4")
        void shouldReturn400_whenTwoVsTwoHasLessThan4Participants() throws Exception {
            var request = createValidCupRequest();
            when(tournamentService.createTournament(eq(userId), any(CreateTournamentRequest.class)))
                    .thenThrow(new IllegalArgumentException("2v2 tournaments require a minimum of 4 participants"));

            mockMvc.perform(post("/api/v1/tournaments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/tournaments should return 404 when ruleConfigurationId does not exist")
        void shouldReturn404_whenRuleConfigurationNotFound() throws Exception {
            var request = createValidCupRequest();
            when(tournamentService.createTournament(eq(userId), any(CreateTournamentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("RuleConfiguration", ruleConfigId.toString()));

            mockMvc.perform(post("/api/v1/tournaments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("AC 5: Query Tournaments (GET /api/v1/tournaments)")
    class QueryTournamentsScenarios {

        @Test
        @DisplayName("GET /api/v1/tournaments should return list of all tournaments")
        void shouldReturnAllTournaments() throws Exception {
            var cup = createSampleTournamentResponse(TournamentFormat.CUP, TournamentMode.ONE_VS_ONE_PERSONAL);
            var champ = createSampleTournamentResponse(TournamentFormat.CHAMPIONSHIP, TournamentMode.TWO_VS_TWO_FIXED_TEAMS);
            when(tournamentService.listTournaments(eq(null)))
                    .thenReturn(List.of(cup, champ));

            mockMvc.perform(get("/api/v1/tournaments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Autumn Championship 2026"))
                    .andExpect(jsonPath("$[0].status").value("REGISTRATION_OPEN"));
        }

        @Test
        @DisplayName("GET /api/v1/tournaments?status=REGISTRATION_OPEN should filter tournaments by status")
        void shouldFilterTournamentsByStatus() throws Exception {
            var cup = createSampleTournamentResponse(TournamentFormat.CUP, TournamentMode.ONE_VS_ONE_PERSONAL);
            when(tournamentService.listTournaments(eq(TournamentStatus.REGISTRATION_OPEN)))
                    .thenReturn(List.of(cup));

            mockMvc.perform(get("/api/v1/tournaments").param("status", "REGISTRATION_OPEN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].status").value("REGISTRATION_OPEN"));
        }

        @Test
        @DisplayName("GET /api/v1/tournaments/{id} should return single tournament details")
        void shouldReturnTournamentById_whenExists() throws Exception {
            var cup = createSampleTournamentResponse(TournamentFormat.CUP, TournamentMode.ONE_VS_ONE_PERSONAL);
            when(tournamentService.getTournamentById(eq(tournamentId)))
                    .thenReturn(cup);

            mockMvc.perform(get("/api/v1/tournaments/{id}", tournamentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(tournamentId.toString()))
                    .andExpect(jsonPath("$.name").value("Autumn Championship 2026"))
                    .andExpect(jsonPath("$.ruleConfiguration.name").value("ITSF Standard Matchplay"));
        }

        @Test
        @DisplayName("GET /api/v1/tournaments/{id} should return 404 when tournament does not exist")
        void shouldReturn404_whenTournamentNotFound() throws Exception {
            var nonExistentId = UUID.randomUUID();
            when(tournamentService.getTournamentById(eq(nonExistentId)))
                    .thenThrow(new ResourceNotFoundException("Tournament", nonExistentId.toString()));

            mockMvc.perform(get("/api/v1/tournaments/{id}", nonExistentId))
                    .andExpect(status().isNotFound());
        }
    }
}
