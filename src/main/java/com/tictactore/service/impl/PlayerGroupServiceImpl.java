package com.tictactore.service.impl;

import com.tictactore.dto.CreatePlayerGroupRequest;
import com.tictactore.dto.PlayerGroupResponse;
import com.tictactore.dto.PlayerSummaryDto;
import com.tictactore.dto.UpdatePlayerGroupRequest;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.exception.ValidationException;
import com.tictactore.model.PlayerGroup;
import com.tictactore.model.User;
import com.tictactore.repository.PlayerGroupRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.PlayerGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerGroupServiceImpl implements PlayerGroupService {

    public static final int MAX_GROUPS_PER_USER = 10;
    public static final int MAX_MEMBERS_PER_GROUP = 12;
    public static final String DEFAULT_FAVORITES_NAME = "Favorites";

    private final PlayerGroupRepository playerGroupRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public List<PlayerGroupResponse> getGroups(UUID creatorId) {
        if (creatorId == null) {
            return List.of();
        }
        ensureFavoritesGroupExists(creatorId);
        return playerGroupRepository.findByCreatorIdOrderByCreatedAtAsc(creatorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlayerGroupResponse getGroupById(UUID creatorId, UUID groupId) {
        if (groupId == null) {
            throw new ResourceNotFoundException("Group not found");
        }
        var group = playerGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        if (creatorId == null || !group.getCreatorId().equals(creatorId)) {
            throw new AccessDeniedException("Access denied to player group");
        }
        return mapToResponse(group);
    }

    @Override
    @Transactional
    public PlayerGroupResponse createGroup(UUID creatorId, CreatePlayerGroupRequest request) {
        if (creatorId == null) {
            throw new AccessDeniedException("User must be authenticated");
        }
        String name = request.name() != null ? request.name().trim() : "";
        if (name.isEmpty()) {
            throw new ValidationException("Group name cannot be blank");
        }
        if (name.length() > 50) {
            throw new ValidationException("Group name cannot exceed 50 characters");
        }
        if (playerGroupRepository.countByCreatorId(creatorId) >= MAX_GROUPS_PER_USER) {
            throw new ValidationException("Maximum limit of " + MAX_GROUPS_PER_USER + " groups reached");
        }
        if (playerGroupRepository.existsByCreatorIdAndNameIgnoreCase(creatorId, name)) {
            throw new ValidationException("A group with this name already exists");
        }

        Set<User> members = resolveMembers(request.memberIds());

        boolean isFavorite = Boolean.TRUE.equals(request.isFavorite());
        if (isFavorite) {
            unmarkOtherFavorites(creatorId, null);
        }

        var group = PlayerGroup.builder()
                .name(name)
                .creatorId(creatorId)
                .isFavorite(isFavorite)
                .members(members)
                .build();

        var saved = playerGroupRepository.save(group);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public PlayerGroupResponse updateGroup(UUID creatorId, UUID groupId, UpdatePlayerGroupRequest request) {
        if (groupId == null) {
            throw new ResourceNotFoundException("Group not found");
        }
        var group = playerGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        if (creatorId == null || !group.getCreatorId().equals(creatorId)) {
            throw new AccessDeniedException("Access denied to player group");
        }

        if (request.version() != null && !request.version().equals(group.getVersion())) {
            throw new OptimisticLockingFailureException("Group has been modified by another transaction");
        }

        String name = request.name() != null ? request.name().trim() : "";
        if (name.isEmpty()) {
            throw new ValidationException("Group name cannot be blank");
        }
        if (name.length() > 50) {
            throw new ValidationException("Group name cannot exceed 50 characters");
        }
        if (playerGroupRepository.existsByCreatorIdAndNameIgnoreCaseAndIdNot(creatorId, name, groupId)) {
            throw new ValidationException("A group with this name already exists");
        }

        Set<User> members = resolveMembers(request.memberIds());

        boolean isFavorite = Boolean.TRUE.equals(request.isFavorite());
        if (isFavorite) {
            unmarkOtherFavorites(creatorId, groupId);
        }

        group.setName(name);
        group.setFavorite(isFavorite);
        if (group.getMembers() == null) {
            group.setMembers(new HashSet<>(members));
        } else {
            group.getMembers().clear();
            group.getMembers().addAll(members);
        }

        var saved = playerGroupRepository.save(group);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteGroup(UUID creatorId, UUID groupId) {
        if (groupId == null) {
            throw new ResourceNotFoundException("Group not found");
        }
        var group = playerGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));

        if (creatorId == null || !group.getCreatorId().equals(creatorId)) {
            throw new AccessDeniedException("Access denied to player group");
        }

        playerGroupRepository.delete(group);
    }

    private void ensureFavoritesGroupExists(UUID creatorId) {
        if (!playerGroupRepository.existsByCreatorIdAndIsFavoriteTrue(creatorId)) {
            if (!playerGroupRepository.existsByCreatorIdAndNameIgnoreCase(creatorId, DEFAULT_FAVORITES_NAME)) {
                var defaultFavorites = PlayerGroup.builder()
                        .name(DEFAULT_FAVORITES_NAME)
                        .creatorId(creatorId)
                        .isFavorite(true)
                        .members(new HashSet<>())
                        .build();
                playerGroupRepository.save(defaultFavorites);
            }
        }
    }

    private void unmarkOtherFavorites(UUID creatorId, UUID currentGroupId) {
        List<PlayerGroup> existingFavorites = playerGroupRepository.findByCreatorIdAndIsFavoriteTrue(creatorId);
        for (PlayerGroup fav : existingFavorites) {
            if (currentGroupId == null || !fav.getId().equals(currentGroupId)) {
                fav.setFavorite(false);
                playerGroupRepository.save(fav);
            }
        }
    }

    private Set<User> resolveMembers(List<UUID> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return new HashSet<>();
        }
        if (memberIds.size() > MAX_MEMBERS_PER_GROUP) {
            throw new ValidationException("Maximum " + MAX_MEMBERS_PER_GROUP + " members allowed per group");
        }
        Set<UUID> uniqueIds = new HashSet<>(memberIds);
        List<User> found = userRepository.findAllById(memberIds);
        if (found.size() != uniqueIds.size()) {
            throw new ValidationException("One or more selected player IDs are invalid or not found");
        }
        return new HashSet<>(found);
    }

    private PlayerGroupResponse mapToResponse(PlayerGroup group) {
        List<PlayerSummaryDto> memberDtos = new ArrayList<>();
        if (group.getMembers() != null) {
            memberDtos = group.getMembers().stream()
                    .map(u -> new PlayerSummaryDto(u.getId(), u.getNickname(), u.getAvatar()))
                    .toList();
        }
        return new PlayerGroupResponse(
                group.getId(),
                group.getName(),
                group.isFavorite(),
                group.getCreatorId(),
                memberDtos,
                group.getCreatedAt(),
                group.getUpdatedAt(),
                group.getVersion() != null ? group.getVersion() : 0L
        );
    }
}
