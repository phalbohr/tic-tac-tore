package com.tictactore.service;

import com.tictactore.dto.CreatePoolRequest;
import com.tictactore.dto.PoolParticipantDto;
import com.tictactore.dto.PoolResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.MatchmakingPool;
import com.tictactore.model.MatchType;
import com.tictactore.model.PoolParticipant;
import com.tictactore.model.PoolParticipantRole;
import com.tictactore.model.PoolStatus;
import com.tictactore.model.SkillLevel;
import com.tictactore.model.StartCondition;
import com.tictactore.model.User;
import com.tictactore.repository.MatchmakingPoolRepository;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PoolServiceImpl implements PoolService {

    private static final int MAX_ACTIVE_POOLS_PER_CREATOR = 3;

    private final MatchmakingPoolRepository matchmakingPoolRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PoolResponse createPool(UUID creatorId, CreatePoolRequest request) {
        validateCreationRequest(request);
        validateCreatorQuota(creatorId);

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + creatorId));

        MatchmakingPool pool = MatchmakingPool.builder()
                .creator(creator)
                .matchType(request.matchType())
                .startCondition(request.startCondition())
                .scheduledTime(request.scheduledTime())
                .skillLevel(request.skillLevel() != null ? request.skillLevel() : SkillLevel.OPEN_FOR_ALL)
                .status(PoolStatus.OPEN)
                .participants(new ArrayList<>())
                .build();

        PoolParticipant hostParticipant = PoolParticipant.builder()
                .pool(pool)
                .user(creator)
                .role(PoolParticipantRole.HOST)
                .joinedAt(Instant.now())
                .build();
        pool.addParticipant(hostParticipant);

        MatchmakingPool savedPool = matchmakingPoolRepository.save(pool);
        return mapToPoolResponse(savedPool);
    }

    @Override
    @Transactional(readOnly = true)
    public PoolResponse getPoolById(UUID poolId) {
        MatchmakingPool pool = matchmakingPoolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Pool not found: " + poolId));
        return mapToPoolResponse(pool);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PoolResponse> getActivePools() {
        return matchmakingPoolRepository.findByStatusOrderByCreatedAtDesc(PoolStatus.OPEN)
                .stream()
                .map(this::mapToPoolResponse)
                .toList();
    }

    @Override
    @Transactional
    public PoolResponse joinPool(UUID poolId, UUID userId) {
        MatchmakingPool pool = matchmakingPoolRepository.findById(poolId)
                .orElseThrow(() -> new ResourceNotFoundException("Pool not found: " + poolId));

        User joiner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        PoolParticipant participant = PoolParticipant.builder()
                .user(joiner)
                .role(PoolParticipantRole.PLAYER)
                .joinedAt(Instant.now())
                .build();
        pool.addParticipant(participant);

        MatchmakingPool savedPool = matchmakingPoolRepository.save(pool);
        return mapToPoolResponse(savedPool);
    }

    private void validateCreationRequest(CreatePoolRequest request) {
        if (request.startCondition() == StartCondition.SCHEDULED_TIME) {
            if (request.scheduledTime() == null) {
                throw new IllegalArgumentException("Scheduled time is required for scheduled pools");
            }
            Instant now = Instant.now();
            if (request.scheduledTime().isBefore(now)) {
                throw new IllegalArgumentException("Scheduled time must be in the future (within 7 days)");
            }
            if (request.scheduledTime().isAfter(now.plus(7, ChronoUnit.DAYS))) {
                throw new IllegalArgumentException("Scheduled time cannot exceed 7 days");
            }
        } else if (request.startCondition() == StartCondition.FILL_BASED) {
            if (request.scheduledTime() != null) {
                throw new IllegalArgumentException("Scheduled time must not be set for fill-based pools");
            }
        }
    }

    private void validateCreatorQuota(UUID creatorId) {
        long activeCount = matchmakingPoolRepository.countByCreatorIdAndStatus(creatorId, PoolStatus.OPEN);
        if (activeCount >= MAX_ACTIVE_POOLS_PER_CREATOR) {
            throw new IllegalArgumentException("Maximum active pools limit reached (3)");
        }
    }

    private PoolResponse mapToPoolResponse(MatchmakingPool pool) {
        int requiredPlayers = pool.getMatchType() == MatchType.ONE_VS_ONE ? 2 : 4;
        List<PoolParticipantDto> participantDtos = pool.getParticipants() != null
                ? pool.getParticipants().stream()
                .map(p -> new PoolParticipantDto(
                        p.getUser().getId(),
                        p.getUser().getNickname(),
                        p.getUser().getAvatar(),
                        p.getRole(),
                        p.getJoinedAt()
                ))
                .toList()
                : List.of();

        return new PoolResponse(
                pool.getId(),
                pool.getCreator().getId(),
                pool.getCreator().getNickname(),
                pool.getMatchType(),
                pool.getStartCondition(),
                pool.getScheduledTime(),
                pool.getSkillLevel(),
                pool.getStatus(),
                requiredPlayers,
                participantDtos.size(),
                participantDtos,
                pool.getCreatedAt()
        );
    }
}
