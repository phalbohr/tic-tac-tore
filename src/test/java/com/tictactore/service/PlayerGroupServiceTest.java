package com.tictactore.service;

import com.tictactore.dto.CreatePlayerGroupRequest;
import com.tictactore.dto.UpdatePlayerGroupRequest;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.exception.ValidationException;
import com.tictactore.model.PlayerGroup;
import com.tictactore.model.User;
import com.tictactore.repository.PlayerGroupRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.impl.PlayerGroupServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlayerGroupService Tests")
class PlayerGroupServiceTest {

    @Mock
    private PlayerGroupRepository playerGroupRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlayerGroupServiceImpl playerGroupService;

    @Test
    void shouldReturnGroupsForCreator() {
        UUID creatorId = UUID.randomUUID();
        User member = User.builder().id(UUID.randomUUID()).nickname("Bob").avatar("avatar-1").build();
        PlayerGroup group = PlayerGroup.builder()
                .id(UUID.randomUUID())
                .name("Alpha")
                .creatorId(creatorId)
                .isFavorite(true)
                .members(Set.of(member))
                .build();
        when(playerGroupRepository.existsByCreatorIdAndIsFavoriteTrue(creatorId)).thenReturn(true);
        when(playerGroupRepository.findByCreatorIdOrderByCreatedAtAsc(creatorId)).thenReturn(List.of(group));

        var responses = playerGroupService.getGroups(creatorId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("Alpha");
        assertThat(responses.get(0).isFavorite()).isTrue();
        assertThat(responses.get(0).members()).hasSize(1);
        assertThat(responses.get(0).members().get(0).nickname()).isEqualTo("Bob");
    }

    @Test
    void shouldGetGroupByIdForCreator() {
        UUID creatorId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        PlayerGroup group = PlayerGroup.builder()
                .id(groupId)
                .name("Favorites")
                .creatorId(creatorId)
                .isFavorite(true)
                .build();
        when(playerGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        var response = playerGroupService.getGroupById(creatorId, groupId);

        assertThat(response.id()).isEqualTo(groupId);
        assertThat(response.name()).isEqualTo("Favorites");
    }

    @Test
    void shouldThrowAccessDeniedWhenGettingGroupOfAnotherUser() {
        UUID creatorId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        PlayerGroup group = PlayerGroup.builder()
                .id(groupId)
                .name("Private Group")
                .creatorId(creatorId)
                .build();
        when(playerGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> playerGroupService.getGroupById(otherUserId, groupId))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldCreateGroupSuccessfully() {
        UUID creatorId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        User member = User.builder().id(memberId).nickname("Charlie").avatar("avatar-c").build();
        CreatePlayerGroupRequest request = new CreatePlayerGroupRequest("Squad", List.of(memberId), true);

        when(playerGroupRepository.existsByCreatorIdAndNameIgnoreCase(creatorId, "Squad")).thenReturn(false);
        when(userRepository.findAllById(List.of(memberId))).thenReturn(List.of(member));
        when(playerGroupRepository.save(any(PlayerGroup.class))).thenAnswer(invocation -> {
            PlayerGroup g = invocation.getArgument(0);
            return PlayerGroup.builder()
                    .id(UUID.randomUUID())
                    .name(g.getName())
                    .creatorId(g.getCreatorId())
                    .isFavorite(g.isFavorite())
                    .members(g.getMembers())
                    .build();
        });

        var response = playerGroupService.createGroup(creatorId, request);

        assertThat(response.name()).isEqualTo("Squad");
        assertThat(response.isFavorite()).isTrue();
        assertThat(response.members()).hasSize(1);
    }

    @Test
    void shouldThrowValidationExceptionWhenCreatingGroupWithDuplicateName() {
        UUID creatorId = UUID.randomUUID();
        CreatePlayerGroupRequest request = new CreatePlayerGroupRequest("Squad", List.of(), false);
        when(playerGroupRepository.existsByCreatorIdAndNameIgnoreCase(creatorId, "Squad")).thenReturn(true);

        assertThatThrownBy(() -> playerGroupService.createGroup(creatorId, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldUpdateGroupSuccessfully() {
        UUID creatorId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        User member = User.builder().id(memberId).nickname("Dave").avatar("avatar-d").build();
        PlayerGroup group = PlayerGroup.builder()
                .id(groupId)
                .name("Old Squad")
                .creatorId(creatorId)
                .isFavorite(false)
                .build();
        UpdatePlayerGroupRequest request = new UpdatePlayerGroupRequest("New Squad", List.of(memberId), true);

        when(playerGroupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(playerGroupRepository.existsByCreatorIdAndNameIgnoreCaseAndIdNot(creatorId, "New Squad", groupId)).thenReturn(false);
        when(userRepository.findAllById(List.of(memberId))).thenReturn(List.of(member));
        when(playerGroupRepository.save(any(PlayerGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = playerGroupService.updateGroup(creatorId, groupId, request);

        assertThat(response.name()).isEqualTo("New Squad");
        assertThat(response.isFavorite()).isTrue();
        assertThat(response.members()).hasSize(1);
    }

    @Test
    void shouldAutoCreateFavoritesGroupWhenNotPresentInGetGroups() {
        UUID creatorId = UUID.randomUUID();
        when(playerGroupRepository.existsByCreatorIdAndIsFavoriteTrue(creatorId)).thenReturn(false);
        when(playerGroupRepository.existsByCreatorIdAndNameIgnoreCase(creatorId, "Favorites")).thenReturn(false);
        when(playerGroupRepository.findByCreatorIdOrderByCreatedAtAsc(creatorId)).thenReturn(List.of(
                PlayerGroup.builder().id(UUID.randomUUID()).name("Favorites").creatorId(creatorId).isFavorite(true).build()
        ));

        var responses = playerGroupService.getGroups(creatorId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("Favorites");
        assertThat(responses.get(0).isFavorite()).isTrue();
        verify(playerGroupRepository).save(any(PlayerGroup.class));
    }

    @Test
    void shouldThrowValidationExceptionWhenGroupLimitExceeded() {
        UUID creatorId = UUID.randomUUID();
        CreatePlayerGroupRequest request = new CreatePlayerGroupRequest("New Group", List.of(), false);
        when(playerGroupRepository.countByCreatorId(creatorId)).thenReturn(10L);

        assertThatThrownBy(() -> playerGroupService.createGroup(creatorId, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Maximum limit of 10 groups reached");
    }

    @Test
    void shouldThrowValidationExceptionWhenMemberLimitExceeded() {
        UUID creatorId = UUID.randomUUID();
        List<UUID> tooManyMembers = java.util.stream.IntStream.range(0, 13)
                .mapToObj(i -> UUID.randomUUID())
                .toList();
        CreatePlayerGroupRequest request = new CreatePlayerGroupRequest("Huge Squad", tooManyMembers, false);

        assertThatThrownBy(() -> playerGroupService.createGroup(creatorId, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Maximum 12 members allowed per group");
    }

    @Test
    void shouldThrowValidationExceptionWhenMemberIdDoesNotExist() {
        UUID creatorId = UUID.randomUUID();
        UUID validId = UUID.randomUUID();
        UUID invalidId = UUID.randomUUID();
        User validUser = User.builder().id(validId).nickname("Valid").build();
        CreatePlayerGroupRequest request = new CreatePlayerGroupRequest("Squad", List.of(validId, invalidId), false);
        when(userRepository.findAllById(any())).thenReturn(List.of(validUser));

        assertThatThrownBy(() -> playerGroupService.createGroup(creatorId, request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("One or more selected player IDs are invalid or not found");
    }

    @Test
    void shouldThrowOptimisticLockingFailureExceptionWhenVersionMismatch() {
        UUID creatorId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        PlayerGroup group = PlayerGroup.builder()
                .id(groupId)
                .name("Squad")
                .creatorId(creatorId)
                .version(2L)
                .build();
        UpdatePlayerGroupRequest request = new UpdatePlayerGroupRequest("Updated", List.of(), false, 1L);
        when(playerGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> playerGroupService.updateGroup(creatorId, groupId, request))
                .isInstanceOf(org.springframework.dao.OptimisticLockingFailureException.class);
    }

    @Test
    void shouldDeleteGroupSuccessfully() {
        UUID creatorId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        PlayerGroup group = PlayerGroup.builder()
                .id(groupId)
                .name("To Delete")
                .creatorId(creatorId)
                .build();
        when(playerGroupRepository.findById(groupId)).thenReturn(Optional.of(group));

        playerGroupService.deleteGroup(creatorId, groupId);

        verify(playerGroupRepository).delete(group);
    }
}
