package com.tictactore.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.redisson.api.RedissonClient;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("NotificationController ATDD Red Phase Scaffolds")
class NotificationControllerATDDTest {

    @MockBean
    private RedissonClient redissonClient;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Subscription Endpoints (AC 5)")
    class SubscriptionEndpoints {

        @Test
        @WithMockUser
        @DisplayName("[P0] POST /api/v1/notifications/subscribe should return 201 Created or 200 OK")
        void shouldSubscribeToPushNotifications() throws Exception {
            String jsonPayload = """
                {
                    "endpoint": "https://push.services.mozilla.com/push/v1/gAAAAA...",
                    "p256dh": "BNcR...",
                    "auth": "tBc..."
                }
                """;

            mockMvc.perform(post("/api/v1/notifications/subscribe")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonPayload))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] DELETE /api/v1/notifications/unsubscribe should return 204 No Content")
        void shouldUnsubscribeFromPushNotifications() throws Exception {
            mockMvc.perform(delete("/api/v1/notifications/unsubscribe")
                    .with(csrf())
                    .param("endpoint", "https://push.services.mozilla.com/push/v1/gAAAAA..."))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] POST /subscribe should return 400 when @Valid payload is missing endpoint")
        void shouldReturnBadRequestOnMissingEndpoint() throws Exception {
            String jsonPayload = """
                {
                    "p256dh": "BNcR...",
                    "auth": "tBc..."
                }
                """;

            mockMvc.perform(post("/api/v1/notifications/subscribe")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonPayload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("[P1] POST /subscribe should return 400 when @Valid payload has blank p256dh")
        void shouldReturnBadRequestOnBlankP256dh() throws Exception {
            String jsonPayload = """
                {
                    "endpoint": "https://push.services.mozilla.com/push/v1/gAAAAA...",
                    "p256dh": "   ",
                    "auth": "tBc..."
                }
                """;

            mockMvc.perform(post("/api/v1/notifications/subscribe")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonPayload))
                    .andExpect(status().isBadRequest());
        }
    }
}
