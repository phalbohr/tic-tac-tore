package com.tictactore.controller;

import com.tictactore.dto.CreatePlayerGroupRequest;
import com.tictactore.dto.PlayerGroupResponse;
import com.tictactore.dto.PlayerSummaryDto;
import com.tictactore.dto.UpdatePlayerGroupRequest;
import com.tictactore.exception.GlobalExceptionHandler;
import com.tictactore.model.User;
import com.tictactore.service.PlayerGroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ATDD Red-Phase Scaffolds for PlayerGroupController (/api/v1/player-groups).
 * Story 6.1: Named Player Groups ("Teams")
 *
 * AC 1: Create/Update player group (unique name 1-50 chars, member IDs, favorite flag)
 * AC 2: Query user's player groups via GET /api/v1/player-groups with safe summaries (no PII, AD-04)
 * AC 5: Strict ownership isolation (PUT/DELETE return 403 Forbidden for non-creator)
 * Security: Authenticated principal via @AuthenticationPrincipal (AD-05)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerGroupController ATDD Specifications — Named Player Groups")
class PlayerGroupControllerATDDTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PlayerGroupService playerGroupService;

    @InjectMocks
    private PlayerGroupController playerGroupController;

    private UUID currentUserId;
    private User currentUser;
    private UsernamePasswordAuthenticationToken auth;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(playerGroupController)
                .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(org.springframework.security.core.annotation.AuthenticationPrincipal.class);
                    }

                    @Override
                    public Object resolveArgument(org.springframework.core.MethodParameter parameter,
                                                  org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                                  org.springframework.web.context.request.NativeWebRequest webRequest,
                                                  org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                        java.security.Principal principal = webRequest.getUserPrincipal();
                        if (principal instanceof org.springframework.security.core.Authentication authentication) {
                            Object p = authentication.getPrincipal();
                            if (p instanceof User user) {
                                return user;
                            }
                        }
                        return null;
                    }
                })
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        currentUserId = UUID.randomUUID();
        currentUser = User.builder().id(currentUserId).email("player@example.com").build();
        auth = new UsernamePasswordAuthenticationToken(currentUser, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("GET /api/v1/player-groups Endpoint Specs")
    class GetPlayerGroupsSpecs {

        @Test
        @DisplayName("[P0] Should return 200 OK with list of groups for authenticated user")
        void shouldReturn200WithUserGroups() throws Exception {
            UUID groupId = UUID.randomUUID();
            PlayerSummaryDto member = new PlayerSummaryDto(UUID.randomUUID(), "Alice", "avatar-1");
            PlayerGroupResponse group = new PlayerGroupResponse(
                    groupId, "Regulars", false, currentUserId, List.of(member),
                    OffsetDateTime.now(), OffsetDateTime.now()
            );

            when(playerGroupService.getGroups(eq(currentUserId))).thenReturn(List.of(group));

            mockMvc.perform(get("/api/v1/player-groups")
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(groupId.toString()))
                    .andExpect(jsonPath("$[0].name").value("Regulars"))
                    .andExpect(jsonPath("$[0].isFavorite").value(false))
                    .andExpect(jsonPath("$[0].members[0].nickname").value("Alice"))
                    .andExpect(jsonPath("$[0].members[0].email").doesNotExist());
        }

        @Test
        @DisplayName("[P1] Should return 401 Unauthorized when unauthenticated")
        @WithAnonymousUser
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/player-groups")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/player-groups/{id} Endpoint Specs")
    class GetPlayerGroupByIdSpecs {

        @Test
        @DisplayName("[P0] Should return 200 OK with group details for creator")
        void shouldReturn200WithGroupDetails() throws Exception {
            UUID groupId = UUID.randomUUID();
            PlayerGroupResponse group = new PlayerGroupResponse(
                    groupId, "Favorites", true, currentUserId, List.of(),
                    OffsetDateTime.now(), OffsetDateTime.now()
            );

            when(playerGroupService.getGroupById(eq(currentUserId), eq(groupId))).thenReturn(group);

            mockMvc.perform(get("/api/v1/player-groups/{id}", groupId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(groupId.toString()))
                    .andExpect(jsonPath("$.name").value("Favorites"))
                    .andExpect(jsonPath("$.isFavorite").value(true));
        }

        @Test
        @DisplayName("[P0] Should return 403 Forbidden when requesting group owned by another user")
        void shouldReturn403WhenNotCreator() throws Exception {
            UUID foreignGroupId = UUID.randomUUID();
            when(playerGroupService.getGroupById(eq(currentUserId), eq(foreignGroupId)))
                    .thenThrow(new AccessDeniedException("Access denied to player group"));

            mockMvc.perform(get("/api/v1/player-groups/{id}", foreignGroupId)
                            .principal(auth)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/player-groups Endpoint Specs")
    class CreatePlayerGroupSpecs {

        @Test
        @DisplayName("[P0] Should return 201 Created when creating a valid player group")
        void shouldReturn201OnCreate() throws Exception {
            UUID groupId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            CreatePlayerGroupRequest request = new CreatePlayerGroupRequest("Tuesday Squad", List.of(memberId), false);
            PlayerSummaryDto member = new PlayerSummaryDto(memberId, "Bob", "avatar-bob");
            PlayerGroupResponse response = new PlayerGroupResponse(
                    groupId, "Tuesday Squad", false, currentUserId, List.of(member),
                    OffsetDateTime.now(), OffsetDateTime.now()
            );

            when(playerGroupService.createGroup(eq(currentUserId), any(CreatePlayerGroupRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/player-groups")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(groupId.toString()))
                    .andExpect(jsonPath("$.name").value("Tuesday Squad"))
                    .andExpect(jsonPath("$.members[0].id").value(memberId.toString()));
        }

        @Test
        @DisplayName("[P1] Should return 400 Bad Request when name is blank or exceeds 50 chars")
        void shouldReturn400WhenNameInvalid() throws Exception {
            CreatePlayerGroupRequest blankNameRequest = new CreatePlayerGroupRequest("", List.of(UUID.randomUUID()), false);

            mockMvc.perform(post("/api/v1/player-groups")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(blankNameRequest))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/player-groups/{id} Endpoint Specs")
    class UpdatePlayerGroupSpecs {

        @Test
        @DisplayName("[P0] Should return 200 OK when updating group by creator")
        void shouldReturn200OnUpdate() throws Exception {
            UUID groupId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            UpdatePlayerGroupRequest request = new UpdatePlayerGroupRequest("Updated Squad", List.of(memberId), true);
            PlayerGroupResponse response = new PlayerGroupResponse(
                    groupId, "Updated Squad", true, currentUserId, List.of(new PlayerSummaryDto(memberId, "Bob", "avatar-bob")),
                    OffsetDateTime.now(), OffsetDateTime.now()
            );

            when(playerGroupService.updateGroup(eq(currentUserId), eq(groupId), any(UpdatePlayerGroupRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/api/v1/player-groups/{id}", groupId)
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Squad"))
                    .andExpect(jsonPath("$.isFavorite").value(true));
        }

        @Test
        @DisplayName("[P0] Should return 403 Forbidden when updating group owned by another user")
        void shouldReturn403OnUpdateByNonCreator() throws Exception {
            UUID foreignGroupId = UUID.randomUUID();
            UpdatePlayerGroupRequest request = new UpdatePlayerGroupRequest("Hacked Squad", List.of(UUID.randomUUID()), false);

            when(playerGroupService.updateGroup(eq(currentUserId), eq(foreignGroupId), any(UpdatePlayerGroupRequest.class)))
                    .thenThrow(new AccessDeniedException("Access denied to player group"));

            mockMvc.perform(put("/api/v1/player-groups/{id}", foreignGroupId)
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/player-groups/{id} Endpoint Specs")
    class DeletePlayerGroupSpecs {

        @Test
        @DisplayName("[P0] Should return 204 No Content when deleting group by creator")
        void shouldReturn204OnDelete() throws Exception {
            UUID groupId = UUID.randomUUID();
            doNothing().when(playerGroupService).deleteGroup(eq(currentUserId), eq(groupId));

            mockMvc.perform(delete("/api/v1/player-groups/{id}", groupId)
                            .principal(auth))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("[P0] Should return 403 Forbidden when deleting group owned by another user")
        void shouldReturn403OnDeleteByNonCreator() throws Exception {
            UUID foreignGroupId = UUID.randomUUID();
            doThrow(new AccessDeniedException("Access denied to player group"))
                    .when(playerGroupService).deleteGroup(eq(currentUserId), eq(foreignGroupId));

            mockMvc.perform(delete("/api/v1/player-groups/{id}", foreignGroupId)
                            .principal(auth))
                    .andExpect(status().isForbidden());
        }
    }
}
