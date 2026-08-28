package com.tictactore.service;

import com.tictactore.dto.CreatePoolRequest;
import com.tictactore.dto.PoolResponse;

import java.util.UUID;

public interface PoolService {

    PoolResponse createPool(UUID creatorId, CreatePoolRequest request);

    PoolResponse getPoolById(UUID poolId);
}
