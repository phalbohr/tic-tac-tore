package com.tictactore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.dto.GameRequest;
import com.tictactore.dto.MatchRequest;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("End-to-End Match Lifecycle Integration Tests")
@Transactional
public class MatchLifecycleIntegrationTest {

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
        var user = new User();
        user.setEmail(email);
        user.setName(name);
        return userRepository.save(user);
    }

    @Test
    @DisplayName("Complete Happy Path: Create -> List Pending -> Approve -> Confirmed")
    @WithMockUser(username = "creator@test.com")
    void fullMatchLifecycle_success() throws Exception {
        // 1. Create Match (as Creator)
        MatchRequest request = new MatchRequest(
                creator.getId(), teammate.getId(),
                opponent1.getId(), opponent2.getId(),
                List.of(new GameRequest(10, 8))
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/matches")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Map<String, Object> createdMatch = objectMapper.readValue(responseBody, Map.class);
        String matchId = (String) createdMatch.get("id");

        // 2. List Pending Matches (as Opponent 1)
        mockMvc.perform(get("/api/v1/matches/pending")
                        .with(user("opponent1@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(matchId));

        // 3. Approve Match (as Opponent 1)
        mockMvc.perform(put("/api/v1/matches/{id}/approve", matchId)
                        .with(csrf())
                        .with(user("opponent1@test.com")))
                .andExpect(status().isNoContent());

        // 4. Verify Final Status (Confirmed)
        var finalMatch = matchRepository.findById(java.util.UUID.fromString(matchId)).orElseThrow();
        assertThat(finalMatch.getStatus()).isEqualTo(MatchStatus.CONFIRMED);
        
        // 5. Verify it's no longer in pending list for opponent
        mockMvc.perform(get("/api/v1/matches/pending")
                        .with(user("opponent1@test.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // Helper for switching users in the same test
    private static org.springframework.test.web.servlet.request.RequestPostProcessor user(String username) {
        return org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user(username);
    }
}
