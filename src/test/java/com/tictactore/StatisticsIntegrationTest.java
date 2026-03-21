package com.tictactore;

import com.tictactore.model.*;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("[StatisticsIntegration] Tests")
class StatisticsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    private User player1;
    private User player2;
    private User player3;
    private User player4;

    @BeforeEach
    void setUp() {
        player1 = createUser("player1@example.com", "Player One");
        player2 = createUser("player2@example.com", "Player Two");
        player3 = createUser("player3@example.com", "Player Three");
        player4 = createUser("player4@example.com", "Player Four");

        // Create a confirmed match: Team A (p1, p2) wins against Team B (p3, p4)
        createConfirmedMatch(player1, player2, player3, player4, WinnerTeam.TEAM_A);
    }

    @Test
    @WithMockUser(username = "player1@example.com")
    @DisplayName("GET /api/v1/statistics/leaderboard - should filter by role")
    void getLeaderboard_shouldFilterByRole() throws Exception {
        // player1 is Attacker in Match 1 (win)
        // player2 is Defender in Match 1 (win)
        
        // Attacker Leaderboard
        mockMvc.perform(get("/api/v1/statistics/leaderboard")
                .param("type", "ATTACKER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].playerName").value("Player One"))
                .andExpect(jsonPath("$.totalElements").value(2)); // p1 (win), p3 (loss)
        
        // Defender Leaderboard
        mockMvc.perform(get("/api/v1/statistics/leaderboard")
                .param("type", "DEFENDER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].playerName").value("Player Two"))
                .andExpect(jsonPath("$.totalElements").value(2)); // p2 (win), p4 (loss)
    }

    @Test
    @WithMockUser(username = "player1@example.com")
    @DisplayName("GET /api/v1/statistics/me - should return personal statistics")
    void getPersonalStats_shouldReturnResults() throws Exception {
        mockMvc.perform(get("/api/v1/statistics/me")
                .param("period", "ALL_TIME")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerName").value("Player One"))
                .andExpect(jsonPath("$.overall.wins").value(1))
                .andExpect(jsonPath("$.attacker.wins").value(1))
                .andExpect(jsonPath("$.defender.wins").value(0));
    }

    @Test
    @WithMockUser(username = "player1@example.com")
    @DisplayName("GET /api/v1/statistics/h2h - should return paginated head-to-head statistics")
    void getH2HStats_shouldReturnResults() throws Exception {
        mockMvc.perform(get("/api/v1/statistics/h2h")
                .param("period", "ALL_TIME")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].opponentName").value("Player Three"))
                .andExpect(jsonPath("$.content[0].wins").value(1))
                .andExpect(jsonPath("$.content[1].opponentName").value("Player Four"))
                .andExpect(jsonPath("$.content[1].wins").value(1));
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }

    private void createConfirmedMatch(User aAtt, User aDef, User bAtt, User bDef, WinnerTeam winner) {
        Match match = new Match();
        match.setCreator(aAtt);
        match.setTeamAAttacker(aAtt);
        match.setTeamADefender(aDef);
        match.setTeamBAttacker(bAtt);
        match.setTeamBDefender(bDef);
        match.setStatus(MatchStatus.CONFIRMED);
        match.setWinner(winner);
        
        Game game = new Game();
        game.setGameNumber(1);
        if (winner == WinnerTeam.TEAM_A) {
            game.setTeamAScore(10);
            game.setTeamBScore(0);
        } else {
            game.setTeamAScore(0);
            game.setTeamBScore(10);
        }
        match.addGame(game);
        
        matchRepository.save(match);
    }
}
