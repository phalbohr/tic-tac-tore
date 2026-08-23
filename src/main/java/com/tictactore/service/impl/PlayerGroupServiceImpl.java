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

    private final PlayerGroupRepository playerGroupRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlayerGroupResponse> getGroups(UUID creatorId) {
        if (creatorId == null) {
            return List.of();
        }
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
        if (playerGroupRepository.existsByCreatorIdAndNameIgnoreCase(creatorId, name)) {
            throw new ValidationException("A group with this name already exists");
        }

        Set<User> members = new HashSet<>();
        if (request.memberIds() != null && !request.memberIds().isEmpty()) {
            members.addAll(userRepository.findAllById(request.memberIds()));
        }

        var group = PlayerGroup.builder()
                .name(name)
                .creatorId(creatorId)
                .isFavorite(Boolean.TRUE.equals(request.isFavorite()))
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

        String name = request.name() != null ? request.name().trim() : "";
        if (name.isEmpty()) {
            throw new ValidationException("Group name cannot be blank");
        }
        if (playerGroupRepository.existsByCreatorIdAndNameIgnoreCaseAndIdNot(creatorId, name, groupId)) {
            throw new ValidationException("A group with this name already exists");
        }

        Set<User> members = new HashSet<>();
        if (request.memberIds() != null && !request.memberIds().isEmpty()) {
            members.addAll(userRepository.findAllById(request.memberIds()));
        }

        group.setName(name);
        group.setFavorite(Boolean.TRUE.equals(request.isFavorite()));
        group.setMembers(members);

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
                group.getUpdatedAt()
        );
    }
}
