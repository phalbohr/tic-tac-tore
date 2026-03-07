package com.tictactore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.dto.GameRequest;
import com.tictactore.dto.MatchRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Match API Integration Tests")
@Transactional // Ensures session stays open if we access lazy collections in assertions
public class MatchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }

    @Test
    @DisplayName("Successfully create a 2v2 match with 3 games")
    @WithMockUser(username = "creator@test.com")
    void createMatch_success() throws Exception {
        MatchRequest request = new MatchRequest(
                creator.getId(), teammate.getId(),
                opponent1.getId(), opponent2.getId(),
                List.of(
                        new GameRequest(10, 8),
                        new GameRequest(5, 10),
                        new GameRequest(10, 7)
                )
        );

        mockMvc.perform(post("/api/v1/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                .andExpect(jsonPath("$.creatorName").value("Creator"))
                .andExpect(jsonPath("$.games.length()").value(3))
                .andExpect(jsonPath("$.teamAAttackerName").value("Creator"))
                .andExpect(jsonPath("$.teamADefenderName").value("Teammate"))
                .andExpect(jsonPath("$.teamBAttackerName").value("Opponent 1"))
                .andExpect(jsonPath("$.teamBDefenderName").value("Opponent 2"));

        List<Match> matches = matchRepository.findAll();
        assertThat(matches).hasSize(1);
        Match match = matches.get(0);
        assertThat(match.getStatus()).isEqualTo(MatchStatus.PENDING_APPROVAL);
        assertThat(match.getCreator().getEmail()).isEqualTo("creator@test.com");
    }

    @Test
    @DisplayName("Should return 400 Bad Request when players are not unique")
    @WithMockUser(username = "creator@test.com")
    void createMatch_nonUniquePlayers() throws Exception {
        MatchRequest request = new MatchRequest(
                creator.getId(), creator.getId(), // Same player twice
                opponent1.getId(), opponent2.getId(),
                List.of(new GameRequest(10, 8))
        );

        mockMvc.perform(post("/api/v1/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.details.playersUnique").exists());
    }

    @Test
    @DisplayName("Should return 404 Not Found when a participant does not exist")
    @WithMockUser(username = "creator@test.com")
    void createMatch_userNotFound() throws Exception {
        MatchRequest request = new MatchRequest(
                creator.getId(), UUID.randomUUID(), // Unknown player
                opponent1.getId(), opponent2.getId(),
                List.of(new GameRequest(10, 8))
        );

        mockMvc.perform(post("/api/v1/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }
}
