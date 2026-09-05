package com.tictactore.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.dto.RuleConfigurationRequest;
import com.tictactore.model.MatchFormat;
import com.tictactore.model.PointDistribution;
import com.tictactore.model.PositionSwapRule;
import com.tictactore.model.RestartRule;
import com.tictactore.model.SideSwapRule;
import com.tictactore.model.User;
import com.tictactore.model.WinByTwoRule;
import com.tictactore.repository.RuleConfigurationRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.RuleConfigurationOperation;
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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("RuleConfigurationApiIT Integration Tests")
public class RuleConfigurationApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RuleConfigurationRepository ruleConfigurationRepository;

    @Autowired
    private RuleConfigurationOperation ruleConfigurationOperation;

    @org.springframework.boot.test.mock.mockito.MockBean
    private org.redisson.api.RedissonClient redissonClient;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.tictactore.service.TokenRevocationService tokenRevocationService;

    private User testUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("ruleit_user1@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("ruleit_user1@example.com")
                        .nickname("RuleTester1")
                        .build()));

        otherUser = userRepository.findByEmail("ruleit_user2@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("ruleit_user2@example.com")
                        .nickname("RuleTester2")
                        .build()));
    }

    private UsernamePasswordAuthenticationToken auth(User user) {
        return new UsernamePasswordAuthenticationToken(user, null, List.of());
    }

    private RuleConfigurationRequest createSampleRequest(String name) {
        return RuleConfigurationRequest.builder()
                .name(name)
                .matchFormat(MatchFormat.BEST_OF_N)
                .goalLimit(5)
                .gameLimit(3)
                .gamesToWin(2)
                .winByTwoRule(WinByTwoRule.ALL_GAMES)
                .absoluteScoreCap(8)
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
    }

    @Test
    @DisplayName("GET /api/v1/rule-configurations - should return presets")
    void getRuleConfigurations_ReturnsPresets() throws Exception {
        mockMvc.perform(get("/api/v1/rule-configurations?type=PRESET")
                        .with(authentication(auth(testUser))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/rule-configurations - should create custom rule")
    void createRuleConfiguration_CreatesCustomRule() throws Exception {
        var request = createSampleRequest("Rule " + UUID.randomUUID().toString().substring(0, 8));

        mockMvc.perform(post("/api/v1/rule-configurations")
                        .with(authentication(auth(testUser)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.type").value("CUSTOM"))
                .andExpect(jsonPath("$.goalLimit").value(5))
                .andExpect(jsonPath("$.winByTwoRule").value("ALL_GAMES"))
                .andExpect(jsonPath("$.absoluteScoreCap").value(8))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("POST /api/v1/rule-configurations - should return 400 on duplicate name for same creator")
    void createRuleConfiguration_DuplicateName_ReturnsBadRequest() throws Exception {
        var name = "UniqueRule " + UUID.randomUUID().toString().substring(0, 8);
        var request = createSampleRequest(name);
        ruleConfigurationOperation.createCustomRule(request, testUser.getId());

        mockMvc.perform(post("/api/v1/rule-configurations")
                        .with(authentication(auth(testUser)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/rule-configurations - should return 400 when absoluteScoreCap <= goalLimit")
    void createRuleConfiguration_InvalidScoreCap_ReturnsBadRequest() throws Exception {
        var request = RuleConfigurationRequest.builder()
                .name("Invalid Cap " + UUID.randomUUID().toString().substring(0, 8))
                .matchFormat(MatchFormat.BEST_OF_N)
                .goalLimit(5)
                .gameLimit(3)
                .gamesToWin(2)
                .winByTwoRule(WinByTwoRule.ALL_GAMES)
                .absoluteScoreCap(5)
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
                        .with(authentication(auth(testUser)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/rule-configurations/{id} - returns 403 when rule belongs to another user")
    void getRuleById_ForeignCustomRule_ReturnsForbidden() throws Exception {
        var request = createSampleRequest("Other User Rule " + UUID.randomUUID().toString().substring(0, 8));
        var saved = ruleConfigurationOperation.createCustomRule(request, otherUser.getId());

        mockMvc.perform(get("/api/v1/rule-configurations/{id}", saved.getId())
                        .with(authentication(auth(testUser))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/rule-configurations/{id} - deletes custom rule owned by user")
    void deleteCustomRule_OwnedRule_ReturnsNoContent() throws Exception {
        var request = createSampleRequest("Delete Rule " + UUID.randomUUID().toString().substring(0, 8));
        var saved = ruleConfigurationOperation.createCustomRule(request, testUser.getId());

        mockMvc.perform(delete("/api/v1/rule-configurations/{id}", saved.getId())
                        .with(authentication(auth(testUser)))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/rule-configurations/{id} - returns 403 when deleting another user's rule")
    void deleteCustomRule_ForeignRule_ReturnsForbidden() throws Exception {
        var request = createSampleRequest("Foreign Delete Rule " + UUID.randomUUID().toString().substring(0, 8));
        var saved = ruleConfigurationOperation.createCustomRule(request, otherUser.getId());

        mockMvc.perform(delete("/api/v1/rule-configurations/{id}", saved.getId())
                        .with(authentication(auth(testUser)))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
