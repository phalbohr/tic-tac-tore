package com.tictactore.service;

import com.tictactore.dto.CreatePlayerGroupRequest;
import com.tictactore.dto.PlayerGroupResponse;
import com.tictactore.dto.UpdatePlayerGroupRequest;

import java.util.List;
import java.util.UUID;

public interface PlayerGroupService {
    List<PlayerGroupResponse> getGroups(UUID creatorId);
    PlayerGroupResponse getGroupById(UUID creatorId, UUID groupId);
    PlayerGroupResponse createGroup(UUID creatorId, CreatePlayerGroupRequest request);
    PlayerGroupResponse updateGroup(UUID creatorId, UUID groupId, UpdatePlayerGroupRequest request);
    void deleteGroup(UUID creatorId, UUID groupId);
}
