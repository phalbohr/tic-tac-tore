package com.tictactore.controller;

import com.tictactore.controller.UserMatchController.PlayerDto;
import com.tictactore.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ATDD Red-Phase Scaffolds for UserMatchController GET /players/search.
 * Story 2.7: Global Player Search & Selection
 *
 * Provider endpoint: TODO — new endpoint, not yet implemented
 *
 * Provider Scrutiny Evidence:
 * - Handler: NEW — not yet implemented (TDD red phase)
 * - Expected from acceptance criteria:
 *   - Endpoint: GET /api/users/me/players/search?q=
 *   - Status: 200 for success, 200 with empty list for blank query
 *   - Response: List<PlayerDto> where PlayerDto = { id: String, nickname: String, avatar: String }
 *   - Auth: public (no authentication required)
 *   - Filtering: excludes soft-deleted accounts (email NOT LIKE 'deleted-%', nickname NOT LIKE 'ex-player-%')
 *   - Matching: case-insensitive nickname LIKE
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserMatchController ATDD Specifications — Player Search")
class UserMatchControllerATDDTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserMatchController userMatchController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userMatchController).build();
    }

    @Nested
    @DisplayName("GET /api/users/me/players/search Endpoint Specs")
    class SearchEndpointSpecs {

        @Test
        @DisplayName("[P0] Should return 200 with matching active users when query matches nicknames")
        @Disabled("RED PHASE — endpoint not yet implemented")
        void shouldReturn200WithMatchingActiveUsers() throws Exception {
            var user1 = new com.tictactore.model.User();
            user1.setId(UUID.randomUUID());
            user1.setNickname("Alice");
            user1.setAvatar("avatar-1");

            var user2 = new com.tictactore.model.User();
            user2.setId(UUID.randomUUID());
            user2.setNickname("alicia");
            user2.setAvatar("avatar-2");

            when(userService.searchActiveUsers("ali")).thenReturn(
                    List.of(
                            new PlayerDto(user1.getId().toString(), "Alice", "avatar-1"),
                            new PlayerDto(user2.getId().toString(), "alicia", "avatar-2")
                    )
            );

            mockMvc.perform(get("/api/users/me/players/search")
                            .param("q", "ali")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nickname").value("Alice"))
                    .andExpect(jsonPath("$[1].nickname").value("alicia"));
        }

        @Test
        @DisplayName("[P0] Should return 200 with empty list when query is blank")
        @Disabled("RED PHASE — endpoint not yet implemented")
        void shouldReturnEmptyListForBlankQuery() throws Exception {
            mockMvc.perform(get("/api/users/me/players/search")
                            .param("q", "   ")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("[P0] Should return 200 with empty list when query parameter is missing")
        @Disabled("RED PHASE — endpoint not yet implemented")
        void shouldReturnEmptyListWhenQueryMissing() throws Exception {
            mockMvc.perform(get("/api/users/me/players/search")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("[P1] Should perform case-insensitive nickname matching")
        @Disabled("RED PHASE — endpoint not yet implemented")
        void shouldMatchNicknameCaseInsensitively() throws Exception {
            var user = new com.tictactore.model.User();
            user.setId(UUID.randomUUID());
            user.setNickname("Charlie");
            user.setAvatar("avatar-c");

            when(userService.searchActiveUsers("CHARLIE")).thenReturn(
                    List.of(new PlayerDto(user.getId().toString(), "Charlie", "avatar-c"))
            );

            mockMvc.perform(get("/api/users/me/players/search")
                            .param("q", "CHARLIE")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].nickname").value("Charlie"));
        }

        @Test
        @DisplayName("[P1] Should not expose email addresses in search results")
        @Disabled("RED PHASE — endpoint not yet implemented")
        void shouldNotExposeEmailAddresses() throws Exception {
            var user = new com.tictactore.model.User();
            user.setId(UUID.randomUUID());
            user.setNickname("Alice");
            user.setAvatar("avatar-1");

            when(userService.searchActiveUsers("ali")).thenReturn(
                    List.of(new PlayerDto(user.getId().toString(), "Alice", "avatar-1"))
            );

            mockMvc.perform(get("/api/users/me/players/search")
                            .param("q", "ali")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].email").doesNotExist());
        }
    }
}
