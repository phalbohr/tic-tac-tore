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

        // Match 1: Team A (p1:Att, p2:Def) wins against Team B (p3:Att, p4:Def)
        createConfirmedMatch(player1, player2, player3, player4, WinnerTeam.TEAM_A);
        
        // Match 2: Team A (p2:Att, p1:Def) wins against Team B (p4:Att, p3:Def)
        // p1 is now Defender
        createConfirmedMatch(player2, player1, player4, player3, WinnerTeam.TEAM_A);
    }

    @Test
    @WithMockUser(username = "player1@example.com")
    @DisplayName("GET /api/v1/statistics/h2h - should filter by positional combinations")
    void getH2HStats_shouldFilterByPositions() throws Exception {
        // As Attacker, p1 played Match 1 vs p3 (Att) and p4 (Def)
        mockMvc.perform(get("/api/v1/statistics/h2h")
                .param("myPosition", "ATTACKER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].wins").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));

        // As Defender, p1 played Match 2 vs p3 (Def) and p4 (Att)
        // If we filter p1 as Defender and opponent as Attacker, we should only see p4
        mockMvc.perform(get("/api/v1/statistics/h2h")
                .param("myPosition", "DEFENDER")
                .param("opponentPosition", "ATTACKER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].opponentName").value("Player Four"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "player1@example.com")
    @DisplayName("GET /api/v1/statistics/leaderboard - should filter by role")
    void getLeaderboard_shouldFilterByRole() throws Exception {
        // player1 is Attacker in Match 1 (win)
        // player2 is Attacker in Match 2 (win)
        
        // Attacker Leaderboard
        mockMvc.perform(get("/api/v1/statistics/leaderboard")
                .param("type", "ATTACKER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4)); // p1, p2, p3, p4 all played as Attacker
        
        // Defender Leaderboard
        mockMvc.perform(get("/api/v1/statistics/leaderboard")
                .param("type", "DEFENDER")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(4));
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
                .andExpect(jsonPath("$.overall.wins").value(2))
                .andExpect(jsonPath("$.attacker.wins").value(1))
                .andExpect(jsonPath("$.defender.wins").value(1));
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
                .andExpect(jsonPath("$.content[0].opponentName").value("Player Four"))
                .andExpect(jsonPath("$.content[0].wins").value(2))
                .andExpect(jsonPath("$.content[1].opponentName").value("Player Three"))
                .andExpect(jsonPath("$.content[1].wins").value(2));
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
