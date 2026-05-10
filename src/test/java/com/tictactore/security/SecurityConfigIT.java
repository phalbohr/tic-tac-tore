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
@DisplayName("SecurityConfig Tests")
class SecurityConfigIT {

    private static final String ENDPOINT_PROTECTED = "/api/matches";
    private static final String ENDPOINT_PUBLIC = "/actuator/health";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Unauthenticated Request - should return 401 when accessing protected endpoint")
    void unauthenticatedRequest_toProtectedEndpoint_returns401() throws Exception {
        mockMvc.perform(get(ENDPOINT_PROTECTED))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Actuator Health - should be publicly accessible")
    void actuatorHealth_isPubliclyAccessible() throws Exception {
        mockMvc.perform(get(ENDPOINT_PUBLIC))
                .andExpect(status().isOk());
    }
}
