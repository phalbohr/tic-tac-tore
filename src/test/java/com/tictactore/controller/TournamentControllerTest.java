package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.ApplicationProperties;
import com.tictactore.config.SecurityConfig;
import com.tictactore.dto.TournamentMatchResponse;
import com.tictactore.exception.ParticipantBusyException;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.repository.UserRepository;
import com.tictactore.security.CsrfCookieFilter;
import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.security.JwtAuthenticationFilter;
import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.tournament.TournamentLifecycleService;
import com.tictactore.service.tournament.TournamentMatchQueryService;
import com.tictactore.service.tournament.TournamentMatchService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TournamentController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CsrfCookieFilter.class})
@DisplayName("TournamentController Match Start & Cancel WebMvc Tests")
class TournamentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TournamentLifecycleService tournamentLifecycleService;

    @MockBean
    private TournamentMatchQueryService tournamentMatchQueryService;

    @MockBean
    private TournamentMatchService tournamentMatchService;

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
    @DisplayName("POST /api/v1/tournaments/{id}/matches/{matchId}/start (AC3)")
    class StartMatchEndpointSpecs {

        @Test
        @WithMockUser(username = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        void shouldStartMatch_andReturnOk() throws Exception {
            var tournamentId = UUID.randomUUID();
            var matchId = UUID.randomUUID();
            var userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

            var response = TournamentMatchResponse.builder()
                    .id(matchId)
                    .tournamentId(tournamentId)
                    .round(1)
                    .matchOrder(1)
                    .status(TournamentMatchStatus.IN_PROGRESS)
                    .isAvailable(false)
                    .isOpponentBusy(false)
                    .busyParticipantNicknames(List.of())
                    .createdAt(Instant.now())
                    .build();

            given(tournamentMatchService.startMatch(eq(tournamentId), eq(matchId), eq(userId))).willReturn(response);

            mockMvc.perform(post("/api/v1/tournaments/{id}/matches/{matchId}/start", tournamentId, matchId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(matchId.toString()))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @WithMockUser(username = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        void shouldReturn409Conflict_whenParticipantBusy() throws Exception {
            var tournamentId = UUID.randomUUID();
            var matchId = UUID.randomUUID();
            var userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

            given(tournamentMatchService.startMatch(eq(tournamentId), eq(matchId), eq(userId)))
                    .willThrow(new ParticipantBusyException("Participant Bob is currently playing another match"));

            mockMvc.perform(post("/api/v1/tournaments/{id}/matches/{matchId}/start", tournamentId, matchId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Participant Bob is currently playing another match"));
        }

        @Test
        @WithAnonymousUser
        void shouldReturn401_whenUnauthenticated() throws Exception {
            var tournamentId = UUID.randomUUID();
            var matchId = UUID.randomUUID();

            mockMvc.perform(post("/api/v1/tournaments/{id}/matches/{matchId}/start", tournamentId, matchId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "invalid-non-uuid-user")
        void shouldReturn401_whenPrincipalNameNotUuid() throws Exception {
            var tournamentId = UUID.randomUUID();
            var matchId = UUID.randomUUID();

            mockMvc.perform(post("/api/v1/tournaments/{id}/matches/{matchId}/start", tournamentId, matchId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/tournaments/{id}/matches/{matchId}/cancel (AC5)")
    class CancelMatchEndpointSpecs {

        @Test
        @WithMockUser(username = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        void shouldCancelMatch_andReturnOk() throws Exception {
            var tournamentId = UUID.randomUUID();
            var matchId = UUID.randomUUID();
            var userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

            var response = TournamentMatchResponse.builder()
                    .id(matchId)
                    .tournamentId(tournamentId)
                    .round(1)
                    .matchOrder(1)
                    .status(TournamentMatchStatus.READY)
                    .isAvailable(true)
                    .isOpponentBusy(false)
                    .busyParticipantNicknames(List.of())
                    .createdAt(Instant.now())
                    .build();

            given(tournamentMatchService.cancelMatch(eq(tournamentId), eq(matchId), eq(userId))).willReturn(response);

            mockMvc.perform(post("/api/v1/tournaments/{id}/matches/{matchId}/cancel", tournamentId, matchId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(matchId.toString()))
                    .andExpect(jsonPath("$.status").value("READY"));
        }
    }
}
