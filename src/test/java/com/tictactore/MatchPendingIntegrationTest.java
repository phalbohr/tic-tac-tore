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

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Match Pending API Integration Tests")
@Transactional
public class MatchPendingIntegrationTest {

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

    @BeforeEach
    void setUp() {
        matchRepository.deleteAll();
        userRepository.deleteAll();

        creator = createUser("creator@test.com", "Creator");
        teammate = createUser("teammate@test.com", "Teammate");
        opponent1 = createUser("opponent1@test.com", "Opponent 1");
        opponent2 = createUser("opponent2@test.com", "Opponent 2");

        // Match 1: Opponent 1 needs to approve
        createMatch(creator, teammate, opponent1, opponent2, MatchStatus.PENDING_APPROVAL);
        
        // Match 2: Opponent 1 already approved (Confirmed) - should NOT show up
        createMatch(creator, teammate, opponent1, opponent2, MatchStatus.CONFIRMED);

        // Match 3: Opponent 1 is creator - should NOT show up in pending approvals for them
        createMatch(opponent1, opponent2, creator, teammate, MatchStatus.PENDING_APPROVAL);
    }

    private User createUser(String email, String name) {
        var user = new User();
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }

    private void createMatch(User a1, User a2, User b1, User b2, MatchStatus status) {
        var match = new Match();
        match.setCreator(a1);
        match.setTeamAAttacker(a1);
        match.setTeamADefender(a2);
        match.setTeamBAttacker(b1);
        match.setTeamBDefender(b2);
        match.setStatus(status);

        var game = new Game();
        game.setTeamAScore(10);
        game.setTeamBScore(8);
        game.setGameNumber(1);
        game.setMatch(match);
        match.setGames(new ArrayList<>(List.of(game)));

        matchRepository.save(match);
    }

    @Test
    @DisplayName("Successfully fetch pending matches for opponent")
    @WithMockUser(username = "opponent1@test.com")
    void getPendingMatches_success() throws Exception {
        mockMvc.perform(get("/api/v1/matches/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].teamBAttackerName").value("Opponent 1"));
    }
}
