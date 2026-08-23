package com.tictactore.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.model.PointDistribution;
import com.tictactore.model.PositionSwapRule;
import com.tictactore.model.RestartRule;
import com.tictactore.model.SideSwapRule;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
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

    @Autowired
    private UserRepository userRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.redisson.api.RedissonClient redissonClient;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.tictactore.service.TokenRevocationService tokenRevocationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("ruleit@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("ruleit@example.com")
                        .nickname("RuleTester")
                        .build()));
    }

    private UsernamePasswordAuthenticationToken auth() {
        return new UsernamePasswordAuthenticationToken(testUser, null, List.of());
    }

    @Test
    @DisplayName("GET /api/v1/rule-configurations - should return presets")
    void getRuleConfigurations_ReturnsPresets() throws Exception {
        mockMvc.perform(get("/api/v1/rule-configurations?type=PRESET")
                        .with(authentication(auth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/rule-configurations - should create custom rule")
    void createRuleConfiguration_CreatesCustomRule() throws Exception {
        RuleConfigurationRequest request = RuleConfigurationRequest.builder()
                .name("Rule " + UUID.randomUUID().toString().substring(0, 8))
                .goalLimit(10)
                .gameLimit(5)
                .winByTwo(true)
                .absoluteScoreCap(12)
                .timeoutsPerGame(2)
                .timeoutDurationSeconds(30)
                .possessionLimit5BarSeconds(10)
                .possessionLimitOtherSeconds(15)
                .sideSwapRule(SideSwapRule.BETWEEN_GAMES)
                .restartRule(RestartRule.CONCEDING_TEAM)
                .spinningAllowed(false)
                .aerialsAllowed(false)
                .positionSwapRule(PositionSwapRule.BETWEEN_GAMES)
                .pointDistribution(PointDistribution.WIN_LOSS_3_0)
                .build();

        mockMvc.perform(post("/api/v1/rule-configurations")
                        .with(authentication(auth()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.type").value("CUSTOM"))
                .andExpect(jsonPath("$.goalLimit").value(10))
                .andExpect(jsonPath("$.gameLimit").value(5))
                .andExpect(jsonPath("$.winByTwo").value(true))
                .andExpect(jsonPath("$.id").exists());
    }
}
