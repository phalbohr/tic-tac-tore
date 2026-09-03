package com.tictactore.controller;

import com.tictactore.dto.tournament.TournamentStandingResponse;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentStatus;
import com.tictactore.service.tournament.TournamentService;
import com.tictactore.service.tournament.TournamentStandingsService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled("ATDD red phase: Story 8.7 - Tournament standings and archive endpoints")
@WebMvcTest(TournamentController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TournamentController Standings & Archive Endpoints")
class TournamentStandingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TournamentStandingsService tournamentStandingsService;

    @MockBean
    private TournamentService tournamentService;

    @Test
    @WithMockUser
    @DisplayName("Should return 200 OK with standings list when querying tournament standings")
    void shouldReturnTournamentStandings() throws Exception {
        var tournamentId = UUID.randomUUID();
        var regId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        var standing = new TournamentStandingResponse(
                regId,
                userId,
                "Alice",
                "https://example.com/alice.png",
                null,
                null,
                null,
                3,
                3,
                0,
                6,
                1,
                5,
                9,
                false,
                1
        );

        when(tournamentStandingsService.calculateStandings(tournamentId)).thenReturn(List.of(standing));

        mockMvc.perform(get("/api/v1/tournaments/{id}/standings", tournamentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationId").value(regId.toString()))
                .andExpect(jsonPath("$[0].nickname").value("Alice"))
                .andExpect(jsonPath("$[0].points").value(9))
                .andExpect(jsonPath("$[0].rank").value(1))
                .andExpect(jsonPath("$[0].gameDifference").value(5));
    }
}
