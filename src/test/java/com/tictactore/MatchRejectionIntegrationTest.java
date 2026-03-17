package com.tictactore;

import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.MatchStatus;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Match Rejection API Integration Tests")
@Transactional
public class MatchRejectionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    private User creator;
    private User teammate;
    private User opponent1;
    private User opponent2;
    private Match pendingMatch;

    @BeforeEach
    void setUp() {
        matchRepository.deleteAll();
        userRepository.deleteAll();

        creator = createUser("creator@test.com", "Creator");
        teammate = createUser("teammate@test.com", "Teammate");
        opponent1 = createUser("opponent1@test.com", "Opponent 1");
        opponent2 = createUser("opponent2@test.com", "Opponent 2");

        pendingMatch = new Match();
        pendingMatch.setCreator(creator);
        pendingMatch.setTeamAAttacker(creator);
        pendingMatch.setTeamADefender(teammate);
        pendingMatch.setTeamBAttacker(opponent1);
        pendingMatch.setTeamBDefender(opponent2);
        pendingMatch.setStatus(MatchStatus.PENDING_APPROVAL);

        Game game = new Game();
        game.setTeamAScore(10);
        game.setTeamBScore(8);
        game.setGameNumber(1);
        game.setMatch(pendingMatch);
        pendingMatch.setGames(new ArrayList<>(List.of(game)));

        pendingMatch = matchRepository.save(pendingMatch);
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }

    @Test
    @DisplayName("Successfully reject match when logged in as opponent")
    @WithMockUser(username = "opponent1@test.com")
    void rejectMatch_success() throws Exception {
        mockMvc.perform(put("/api/v1/matches/{id}/reject", pendingMatch.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        Match updatedMatch = matchRepository.findById(pendingMatch.getId()).orElseThrow();
        assertThat(updatedMatch.getStatus()).isEqualTo(MatchStatus.DRAFT);
    }

    @Test
    @DisplayName("Fail to reject match when logged in as creator")
    @WithMockUser(username = "creator@test.com")
    void rejectMatch_failAsCreator() throws Exception {
        mockMvc.perform(put("/api/v1/matches/{id}/reject", pendingMatch.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        Match updatedMatch = matchRepository.findById(pendingMatch.getId()).orElseThrow();
        assertThat(updatedMatch.getStatus()).isEqualTo(MatchStatus.PENDING_APPROVAL);
    }

    @Test
    @DisplayName("Fail to reject match when logged in as teammate")
    @WithMockUser(username = "teammate@test.com")
    void rejectMatch_failAsTeammate() throws Exception {
        mockMvc.perform(put("/api/v1/matches/{id}/reject", pendingMatch.getId())
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        Match updatedMatch = matchRepository.findById(pendingMatch.getId()).orElseThrow();
        assertThat(updatedMatch.getStatus()).isEqualTo(MatchStatus.PENDING_APPROVAL);
    }

    @Test
    @DisplayName("Fail to reject match when match ID not found")
    @WithMockUser(username = "opponent1@test.com")
    void rejectMatch_notFound() throws Exception {
        mockMvc.perform(put("/api/v1/matches/{id}/reject", UUID.randomUUID())
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
