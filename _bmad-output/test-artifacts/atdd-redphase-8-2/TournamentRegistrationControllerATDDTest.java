package com.tictactore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactore.config.ApplicationProperties;
import com.tictactore.config.SecurityConfig;
import com.tictactore.dto.MyRegistrationStatusResponse;
import com.tictactore.dto.RegisterTournamentRequest;
import com.tictactore.dto.TournamentRegistrationResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.RegistrationStatus;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.security.CsrfCookieFilter;
import com.tictactore.security.CustomOAuth2SuccessHandler;
import com.tictactore.security.JwtAuthenticationFilter;
import com.tictactore.service.JwtService;
import com.tictactore.service.TokenRevocationService;
import com.tictactore.service.TournamentRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TournamentRegistrationController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CsrfCookieFilter.class})
@DisplayName("TournamentRegistrationController ATDD Specifications — Team Registration & Confirmation (Story 8.2)")
class TournamentRegistrationControllerATDDTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TournamentRegistrationService registrationService;

    @MockBean
    private TokenRevocationService tokenRevocationService;

    @MockBean
    private CustomOAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ApplicationProperties properties;

    private final UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID partnerId = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private final UUID tournamentId = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID registrationId = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private User principalUser;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        principalUser = User.builder()
                .id(userId)
                .email("player@example.com")
                .nickname("Striker")
                .build();

        auth = new UsernamePasswordAuthenticationToken(
                principalUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    private TournamentRegistrationResponse createRegistrationResponse(RegistrationStatus status, UUID partner) {
        return TournamentRegistrationResponse.builder()
                .id(registrationId)
                .tournamentId(tournamentId)
                .tournamentName("Autumn Cup 2026")
                .playerId(userId)
                .playerNickname("Striker")
                .playerAvatarUrl("https://example.com/avatars/striker.png")
                .partnerId(partner)
                .partnerNickname(partner != null ? "Defender" : null)
                .partnerAvatarUrl(partner != null ? "https://example.com/avatars/defender.png" : null)
                .status(status)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("AC 1: Solo Registration (1v1 & 2v2 Random)")
    class SoloRegistrationScenarios {

        @Test
        @DisplayName("POST /api/v1/tournaments/{id}/registrations without partner should return 201 Created with CONFIRMED status")
        void shouldRegisterSoloSuccessfully() throws Exception {
            var request = new RegisterTournamentRequest(null);
            var response = createRegistrationResponse(RegistrationStatus.CONFIRMED, null);
            when(registrationService.register(eq(tournamentId), eq(userId), any(RegisterTournamentRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/tournaments/{tournamentId}/registrations", tournamentId)
                            .with(authentication(auth))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(registrationId.toString()))
                    .andExpect(jsonPath("$.tournamentId").value(tournamentId.toString()))
                    .andExpect(jsonPath("$.playerId").value(userId.toString()))
                    .andExpect(jsonPath("$.status").value("CONFIRMED"))
                    .andExpect(jsonPath("$.partnerId").isEmpty());
        }
    }

    @Nested
    @DisplayName("AC 2: 2v2 Fixed Team Registration with Partner")
    class PartnerRegistrationScenarios {

        @Test
        @DisplayName("POST /api/v1/tournaments/{id}/registrations with valid partner should return 201 Created with PENDING_CONFIRMATION")
        void shouldRegisterWithPartnerSuccessfully() throws Exception {
            var request = new RegisterTournamentRequest(partnerId);
            var response = createRegistrationResponse(RegistrationStatus.PENDING_CONFIRMATION, partnerId);
            when(registrationService.register(eq(tournamentId), eq(userId), any(RegisterTournamentRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/tournaments/{tournamentId}/registrations", tournamentId)
                            .with(authentication(auth))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(registrationId.toString()))
                    .andExpect(jsonPath("$.status").value("PENDING_CONFIRMATION"))
                    .andExpect(jsonPath("$.partnerId").value(partnerId.toString()))
                    .andExpect(jsonPath("$.partnerNickname").value("Defender"));
        }
    }

    @Nested
    @DisplayName("AC 3 & AC 4: Invitation Accept & Decline")
    class InvitationResponseScenarios {

        @Test
        @DisplayName("POST /api/v1/tournaments/{id}/registrations/{regId}/accept should return 200 OK with CONFIRMED status")
        void shouldAcceptInvitationSuccessfully() throws Exception {
            var response = createRegistrationResponse(RegistrationStatus.CONFIRMED, partnerId);
            when(registrationService.acceptInvitation(eq(tournamentId), eq(registrationId), eq(userId)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/tournaments/{tournamentId}/registrations/{registrationId}/accept",
                            tournamentId, registrationId)
                            .with(authentication(auth))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(registrationId.toString()))
                    .andExpect(jsonPath("$.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("POST /api/v1/tournaments/{id}/registrations/{regId}/decline should return 200 OK with DECLINED status")
        void shouldDeclineInvitationSuccessfully() throws Exception {
            var response = createRegistrationResponse(RegistrationStatus.DECLINED, partnerId);
            when(registrationService.declineInvitation(eq(tournamentId), eq(registrationId), eq(userId)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/tournaments/{tournamentId}/registrations/{registrationId}/decline",
                            tournamentId, registrationId)
                            .with(authentication(auth))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(registrationId.toString()))
                    .andExpect(jsonPath("$.status").value("DECLINED"));
        }
    }

    @Nested
    @DisplayName("AC 5: Withdraw & Cancel Registration")
    class CancelRegistrationScenarios {

        @Test
        @DisplayName("DELETE /api/v1/tournaments/{id}/registrations/{regId} should return 204 No Content")
        void shouldCancelRegistrationSuccessfully() throws Exception {
            doNothing().when(registrationService).cancelRegistration(eq(tournamentId), eq(registrationId), eq(userId));

            mockMvc.perform(delete("/api/v1/tournaments/{tournamentId}/registrations/{registrationId}",
                            tournamentId, registrationId)
                            .with(authentication(auth))
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("AC 6: Validation Errors & Access Control")
    class ValidationAndErrorScenarios {

        @Test
        @DisplayName("POST registrations should return 404 when tournament not found")
        void shouldReturn404_whenTournamentNotFound() throws Exception {
            var request = new RegisterTournamentRequest(null);
            when(registrationService.register(eq(tournamentId), eq(userId), any(RegisterTournamentRequest.class)))
                    .thenThrow(new ResourceNotFoundException("Tournament", tournamentId.toString()));

            mockMvc.perform(post("/api/v1/tournaments/{tournamentId}/registrations", tournamentId)
                            .with(authentication(auth))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST registrations should return 400 when registration deadline passed")
        void shouldReturn400_whenDeadlinePassed() throws Exception {
            var request = new RegisterTournamentRequest(null);
            when(registrationService.register(eq(tournamentId), eq(userId), any(RegisterTournamentRequest.class)))
                    .thenThrow(new IllegalArgumentException("Tournament registration deadline has passed"));

            mockMvc.perform(post("/api/v1/tournaments/{tournamentId}/registrations", tournamentId)
                            .with(authentication(auth))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST registrations should return 400 when partnerId equals playerId in 2v2 fixed")
        void shouldReturn400_whenPartnerIsSelf() throws Exception {
            var request = new RegisterTournamentRequest(userId);
            when(registrationService.register(eq(tournamentId), eq(userId), any(RegisterTournamentRequest.class)))
                    .thenThrow(new IllegalArgumentException("Partner cannot be the same as the initiating player"));

            mockMvc.perform(post("/api/v1/tournaments/{tournamentId}/registrations", tournamentId)
                            .with(authentication(auth))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST registrations should return 409 when user already has active registration")
        void shouldReturn409_whenDuplicateRegistration() throws Exception {
            var request = new RegisterTournamentRequest(null);
            when(registrationService.register(eq(tournamentId), eq(userId), any(RegisterTournamentRequest.class)))
                    .thenThrow(new IllegalStateException("User already has an active registration for this tournament"));

            mockMvc.perform(post("/api/v1/tournaments/{tournamentId}/registrations", tournamentId)
                            .with(authentication(auth))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("POST accept invitation should return 403 Forbidden when caller is not the invited partner")
        void shouldReturn403_whenNonPartnerAccepts() throws Exception {
            when(registrationService.acceptInvitation(eq(tournamentId), eq(registrationId), eq(userId)))
                    .thenThrow(new AccessDeniedException("Only the invited partner can accept this invitation"));

            mockMvc.perform(post("/api/v1/tournaments/{tournamentId}/registrations/{registrationId}/accept",
                            tournamentId, registrationId)
                            .with(authentication(auth))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("DELETE registration should return 403 Forbidden when caller is neither player nor partner")
        void shouldReturn403_whenUnauthorizedUserCancels() throws Exception {
            doThrow(new AccessDeniedException("Only registered participants can cancel registration"))
                    .when(registrationService).cancelRegistration(eq(tournamentId), eq(registrationId), eq(userId));

            mockMvc.perform(delete("/api/v1/tournaments/{tournamentId}/registrations/{registrationId}",
                            tournamentId, registrationId)
                            .with(authentication(auth))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("AC 7: Query Registrations & Status")
    class QueryRegistrationScenarios {

        @Test
        @DisplayName("GET registrations should return list of confirmed and pending registrations")
        void shouldReturnRegistrationsList() throws Exception {
            var r1 = createRegistrationResponse(RegistrationStatus.CONFIRMED, null);
            var r2 = createRegistrationResponse(RegistrationStatus.PENDING_CONFIRMATION, partnerId);
            when(registrationService.listRegistrations(eq(tournamentId), eq(null)))
                    .thenReturn(List.of(r1, r2));

            mockMvc.perform(get("/api/v1/tournaments/{tournamentId}/registrations", tournamentId)
                            .with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                    .andExpect(jsonPath("$[1].status").value("PENDING_CONFIRMATION"));
        }

        @Test
        @DisplayName("GET registrations/my should return current user registration status")
        void shouldReturnMyRegistrationStatus() throws Exception {
            var reg = createRegistrationResponse(RegistrationStatus.CONFIRMED, null);
            var myStatus = new MyRegistrationStatusResponse(true, reg, false);
            when(registrationService.getMyRegistrationStatus(eq(tournamentId), eq(userId)))
                    .thenReturn(myStatus);

            mockMvc.perform(get("/api/v1/tournaments/{tournamentId}/registrations/my", tournamentId)
                            .with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(true))
                    .andExpect(jsonPath("$.registration.id").value(registrationId.toString()));
        }

        @Test
        @DisplayName("GET /api/v1/tournaments/invitations/pending should return pending invitations for user")
        void shouldReturnPendingInvitations() throws Exception {
            var invite = createRegistrationResponse(RegistrationStatus.PENDING_CONFIRMATION, userId);
            when(registrationService.getPendingInvitations(eq(userId)))
                    .thenReturn(List.of(invite));

            mockMvc.perform(get("/api/v1/tournaments/invitations/pending")
                            .with(authentication(auth)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].status").value("PENDING_CONFIRMATION"));
        }
    }

    @Nested
    @DisplayName("Security & Authentication Specs")
    class SecuritySpecs {

        @Test
        @WithAnonymousUser
        @DisplayName("POST registrations should return 401 Unauthorized when unauthenticated")
        void shouldReturn401_whenUnauthenticated() throws Exception {
            var request = new RegisterTournamentRequest(null);

            mockMvc.perform(post("/api/v1/tournaments/{tournamentId}/registrations", tournamentId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
