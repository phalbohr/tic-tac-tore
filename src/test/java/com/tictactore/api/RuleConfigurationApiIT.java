package com.tictactore.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.model.RuleConfigurationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("RuleConfigurationApiIT Tests")
public class RuleConfigurationApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/v1/rule-configurations - should return presets")
    @WithMockUser
    void getRuleConfigurations_ReturnsPresets() throws Exception {
        mockMvc.perform(get("/api/v1/rule-configurations?type=PRESET"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/rule-configurations - should create custom rule")
    @WithMockUser(username = "50f4a8e2-888e-4f10-9173-67c8cbcf8f3a")
    void createRuleConfiguration_CreatesCustomRule() throws Exception {
        RuleConfigurationRequest request = new RuleConfigurationRequest(
                "My Custom Rule",
                RuleConfigurationType.CUSTOM,
                10,
                5,
                true
        );

        mockMvc.perform(post("/api/v1/rule-configurations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("My Custom Rule"))
                .andExpect(jsonPath("$.type").value("CUSTOM"))
                .andExpect(jsonPath("$.goalLimit").value(10))
                .andExpect(jsonPath("$.gameLimit").value(5))
                .andExpect(jsonPath("$.winByTwo").value(true))
                .andExpect(jsonPath("$.id").exists());
    }
}
