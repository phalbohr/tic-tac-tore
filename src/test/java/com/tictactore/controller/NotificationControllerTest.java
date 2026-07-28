package com.tictactore.controller;

import com.tictactore.dto.PushSubscriptionRequest;
import com.tictactore.model.User;
import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.security.JwtAuthenticationFilter;
import com.tictactore.service.JwtService;
import com.tictactore.service.PushNotificationService;
import com.tictactore.service.TokenRevocationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({com.tictactore.config.SecurityConfig.class, JwtAuthenticationFilter.class})
@DisplayName("NotificationController Unit Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PushNotificationService pushNotificationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenRevocationService tokenRevocationService;

    @MockBean
    private CustomOAuth2SuccessHandler customOAuth2SuccessHandler;

    @Nested
    @DisplayName("Subscription Endpoints")
    class SubscriptionEndpoints {

        @Test
        @WithMockUser
        @DisplayName("[P0] POST /subscribe should return 201 Created for valid payload")
        void shouldReturnCreatedOnValidSubscription() throws Exception {
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
        @DisplayName("[P0] POST /subscribe should return 400 Bad Request for @Valid violations")
        void shouldReturnBadRequestOnInvalidSubscription() throws Exception {
            String jsonPayload = """
                    {
                        "endpoint": "",
                        "p256dh": "",
                        "auth": ""
                    }
                    """;

            mockMvc.perform(post("/api/v1/notifications/subscribe")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonPayload))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("[P0] POST /subscribe should return 401 Unauthorized without authentication")
        void shouldReturnUnauthorizedWhenNotAuthenticated() throws Exception {
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
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser
        @DisplayName("[P0] DELETE /unsubscribe should return 204 No Content")
        void shouldReturnNoContentOnUnsubscribe() throws Exception {
            mockMvc.perform(delete("/api/v1/notifications/unsubscribe")
                    .with(csrf())
                    .param("endpoint", "https://push.services.mozilla.com/push/v1/gAAAAA..."))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("[P0] DELETE /unsubscribe should return 401 Unauthorized without authentication")
        void shouldReturnUnauthorizedOnUnsubscribeWithoutAuth() throws Exception {
            mockMvc.perform(delete("/api/v1/notifications/unsubscribe")
                    .with(csrf())
                    .param("endpoint", "https://push.services.mozilla.com/push/v1/gAAAAA..."))
                    .andExpect(status().isUnauthorized());
        }
    }
}
