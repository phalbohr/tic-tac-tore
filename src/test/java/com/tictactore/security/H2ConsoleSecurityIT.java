package com.tictactore.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("H2 Console Security Tests")
class H2ConsoleSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Unauthenticated request to H2 console - should be unauthorized in test profile")
    void unauthenticatedRequestToH2Console_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/h2-console"))
                .andExpect(status().isUnauthorized());
    }
}
