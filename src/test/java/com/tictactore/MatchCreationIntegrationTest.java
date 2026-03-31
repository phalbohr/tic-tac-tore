package com.tictactore;

import com.tictactore.dto.GameRequest;
import com.tictactore.dto.MatchRequest;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Match Creation API Integration Tests")
@Transactional
public class MatchCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("Successfully create match when all data is valid")
    @WithMockUser(username = "creator@test.com")
    void createMatch_success() throws Exception {
        MatchRequest request = new MatchRequest(
                creator.getId(), teammate.getId(),
                opponent1.getId(), opponent2.getId(),
                List.of(new GameRequest(10, 8))
        );

        mockMvc.perform(post("/api/v1/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Fail to create match when players are not unique")
    @WithMockUser(username = "creator@test.com")
    void createMatch_failDuplicatePlayers() throws Exception {
        MatchRequest request = new MatchRequest(
                creator.getId(), creator.getId(), // Duplicate
                opponent1.getId(), opponent2.getId(),
                List.of(new GameRequest(10, 8))
        );

        mockMvc.perform(post("/api/v1/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
