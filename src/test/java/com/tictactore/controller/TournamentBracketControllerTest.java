package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.ApplicationProperties;
import com.tictactore.config.SecurityConfig;
import com.tictactore.dto.RoundMatchesResponse;
import com.tictactore.dto.TournamentBracketResponse;
import com.tictactore.dto.TournamentMatchResponse;
import com.tictactore.dto.TournamentRegistrationResponse;
import com.tictactore.dto.TournamentResponse;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentStatus;
import com.tictactore.repository.UserRepository;
import com.tictactore.security.CsrfCookieFilter;
import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.security.JwtAuthenticationFilter;
import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.tournament.TournamentLifecycleService;
import com.tictactore.service.tournament.TournamentMatchQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TournamentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CsrfCookieFilter.class})
@DisplayName("TournamentController - Bracket & Seeding Tests")
class TournamentBracketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TournamentLifecycleService tournamentLifecycleService;

    @MockBean
    private TournamentMatchQueryService tournamentMatchQueryService;

    @MockBean
    private TokenRevocationService tokenRevocationService;

    @MockBean
    private CustomOAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ApplicationProperties properties;

    @Nested
    @DisplayName("POST /api/v1/tournaments/{id}/start")
    class StartTournamentTests {

        @Test
        @WithMockUser(username = "organizer-uuid")
        @DisplayName("Should successfully start tournament and return IN_PROGRESS status")
        void shouldStartTournamentSuccessfully() throws Exception {
            UUID tournamentId = UUID.randomUUID();
            TournamentResponse response = TournamentResponse.builder()
                    .id(tournamentId)
                    .name("Autumn Cup 2026")
                    .format(TournamentFormat.CUP)
                    .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                    .status(TournamentStatus.IN_PROGRESS)
                    .minParticipants(4)
                    .maxParticipants(16)
                    .registrationDeadline(Instant.now().minusSeconds(60))
                    .createdAt(Instant.now().minusSeconds(3600))
                    .build();

            given(tournamentLifecycleService.startTournament(eq(tournamentId))).willReturn(response);

            mockMvc.perform(post("/api/v1/tournaments/{id}/start", tournamentId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(tournamentId.toString()))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("Should return 401 Unauthorized when starting tournament unauthenticated")
        void shouldReturnUnauthorizedWhenStartingUnauthenticated() throws Exception {
            UUID tournamentId = UUID.randomUUID();

            mockMvc.perform(post("/api/v1/tournaments/{id}/start", tournamentId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tournaments/{id}/bracket")
    class GetBracketTests {

        @Test
        @WithMockUser
        @DisplayName("Should return complete Cup tournament bracket structure")
        void shouldReturnCupBracketSuccessfully() throws Exception {
            UUID tournamentId = UUID.randomUUID();
            UUID matchId = UUID.randomUUID();
            UUID regId1 = UUID.randomUUID();
            UUID regId2 = UUID.randomUUID();

            TournamentRegistrationResponse part1 = TournamentRegistrationResponse.builder()
                    .id(regId1)
                    .tournamentId(tournamentId)
                    .playerId(UUID.randomUUID())
                    .playerNickname("Player 1")
                    .seed(1)
                    .build();

            TournamentRegistrationResponse part2 = TournamentRegistrationResponse.builder()
                    .id(regId2)
                    .tournamentId(tournamentId)
                    .playerId(UUID.randomUUID())
                    .playerNickname("Player 8")
                    .seed(8)
                    .build();

            TournamentMatchResponse matchResponse = TournamentMatchResponse.builder()
                    .id(matchId)
                    .tournamentId(tournamentId)
                    .round(1)
                    .matchOrder(1)
                    .participant1(part1)
                    .participant2(part2)
                    .seed1(1)
                    .seed2(8)
                    .status(TournamentMatchStatus.READY)
                    .build();

            RoundMatchesResponse round1 = RoundMatchesResponse.builder()
                    .round(1)
                    .roundName("Quarterfinals")
                    .matches(List.of(matchResponse))
                    .build();

            TournamentBracketResponse bracketResponse = TournamentBracketResponse.builder()
                    .tournamentId(tournamentId)
                    .tournamentName("Autumn Cup 2026")
                    .format(TournamentFormat.CUP)
                    .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                    .status(TournamentStatus.IN_PROGRESS)
                    .totalRounds(3)
                    .rounds(List.of(round1))
                    .seededParticipants(List.of(part1, part2))
                    .build();

            given(tournamentMatchQueryService.getTournamentBracket(eq(tournamentId))).willReturn(bracketResponse);

            mockMvc.perform(get("/api/v1/tournaments/{id}/bracket", tournamentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tournamentId").value(tournamentId.toString()))
                    .andExpect(jsonPath("$.format").value("CUP"))
                    .andExpect(jsonPath("$.totalRounds").value(3))
                    .andExpect(jsonPath("$.rounds[0].round").value(1))
                    .andExpect(jsonPath("$.rounds[0].matches[0].seed1").value(1))
                    .andExpect(jsonPath("$.rounds[0].matches[0].seed2").value(8));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/tournaments/{id}/matches")
    class GetMatchesTests {

        @Test
        @WithMockUser
        @DisplayName("Should return tournament matches filtered by round")
        void shouldReturnMatchesByRound() throws Exception {
            UUID tournamentId = UUID.randomUUID();
            UUID matchId = UUID.randomUUID();

            TournamentMatchResponse matchResponse = TournamentMatchResponse.builder()
                    .id(matchId)
                    .tournamentId(tournamentId)
                    .round(1)
                    .matchOrder(1)
                    .status(TournamentMatchStatus.READY)
                    .build();

            given(tournamentMatchQueryService.getTournamentMatches(eq(tournamentId), eq(1)))
                    .willReturn(List.of(matchResponse));

            mockMvc.perform(get("/api/v1/tournaments/{id}/matches", tournamentId)
                            .param("round", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(matchId.toString()))
                    .andExpect(jsonPath("$[0].round").value(1))
                    .andExpect(jsonPath("$[0].status").value("READY"));
        }

        @Test
        @WithMockUser
        @DisplayName("Should return tournament matches with partners and stub flags for 2v2 random pairing")
        void shouldReturnMatchesWithPartnersAndStubsFor2v2RandomPairing() throws Exception {
            UUID tournamentId = UUID.randomUUID();
            UUID matchId = UUID.randomUUID();

            TournamentRegistrationResponse part1 = TournamentRegistrationResponse.builder()
                    .id(UUID.randomUUID())
                    .playerNickname("P1")
                    .build();
            TournamentRegistrationResponse part1Partner = TournamentRegistrationResponse.builder()
                    .id(UUID.randomUUID())
                    .playerNickname("P1Partner")
                    .build();
            TournamentRegistrationResponse part2 = TournamentRegistrationResponse.builder()
                    .id(UUID.randomUUID())
                    .playerNickname("P2")
                    .build();
            TournamentRegistrationResponse part2Partner = TournamentRegistrationResponse.builder()
                    .id(UUID.randomUUID())
                    .playerNickname("P2Partner")
                    .build();

            TournamentMatchResponse matchResponse = TournamentMatchResponse.builder()
                    .id(matchId)
                    .tournamentId(tournamentId)
                    .round(1)
                    .matchOrder(1)
                    .participant1(part1)
                    .participant1Partner(part1Partner)
                    .participant2(part2)
                    .participant2Partner(part2Partner)
                    .isParticipant1Stub(false)
                    .isParticipant2Stub(true)
                    .status(TournamentMatchStatus.READY)
                    .build();

            given(tournamentMatchQueryService.getTournamentMatches(eq(tournamentId), eq(1)))
                    .willReturn(List.of(matchResponse));

            mockMvc.perform(get("/api/v1/tournaments/{id}/matches", tournamentId)
                            .param("round", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(matchId.toString()))
                    .andExpect(jsonPath("$[0].participant1.playerNickname").value("P1"))
                    .andExpect(jsonPath("$[0].participant1Partner.playerNickname").value("P1Partner"))
                    .andExpect(jsonPath("$[0].participant2.playerNickname").value("P2"))
                    .andExpect(jsonPath("$[0].participant2Partner.playerNickname").value("P2Partner"))
                    .andExpect(jsonPath("$[0].isParticipant1Stub").value(false))
                    .andExpect(jsonPath("$[0].isParticipant2Stub").value(true));
        }
    }
}
