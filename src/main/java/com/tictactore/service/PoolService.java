package com.tictactore.service;

import com.tictactore.dto.CreatePoolRequest;
import com.tictactore.dto.PoolResponse;

import java.util.List;
import java.util.UUID;

public interface PoolService {

    PoolResponse createPool(UUID creatorId, CreatePoolRequest request);

    PoolResponse getPoolById(UUID poolId);

    List<PoolResponse> getActivePools();

    PoolResponse joinPool(UUID poolId, UUID userId);
}
