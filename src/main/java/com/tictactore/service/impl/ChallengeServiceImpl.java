package com.tictactore.service.impl;

import com.tictactore.dto.ChallengeActionResponse;
import com.tictactore.dto.ChallengeResponse;
import com.tictactore.dto.CreateChallengeRequest;
import com.tictactore.event.ChallengeAcceptedEvent;
import com.tictactore.event.ChallengeCreatedEvent;
import com.tictactore.event.ChallengeDeclinedEvent;
import com.tictactore.exception.ChallengeConflictException;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.exception.ValidationException;
import com.tictactore.model.ChallengeStatus;
import com.tictactore.model.MatchChallenge;
import com.tictactore.model.PlayerGroup;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.User;
import com.tictactore.repository.MatchChallengeRepository;
import com.tictactore.repository.PlayerGroupRepository;
import com.tictactore.repository.RuleConfigurationRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.ChallengeService;
import com.tictactore.service.operation.ChallengeOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Retryable
public class ChallengeServiceImpl implements ChallengeService {

    private final MatchChallengeRepository matchChallengeRepository;
    private final UserRepository userRepository;
    private final PlayerGroupRepository playerGroupRepository;
    private final RuleConfigurationRepository ruleConfigurationRepository;
    private final ChallengeOperation challengeOperation;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ChallengeResponse createChallenge(UUID challengerId, CreateChallengeRequest request) {
        if (request.targetPlayerId() == null && request.targetGroupId() == null) {
            throw new ValidationException("Target player or group must be specified");
        }
        if (request.targetPlayerId() != null && request.targetPlayerId().equals(challengerId)) {
            throw new ValidationException("Challenger cannot challenge themselves");
        }

        User challenger = userRepository.findById(challengerId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenger not found: " + challengerId));

        User targetPlayer = null;
        if (request.targetPlayerId() != null) {
            targetPlayer = userRepository.findById(request.targetPlayerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target player not found: " + request.targetPlayerId()));
            if (matchChallengeRepository.existsByChallengerIdAndTargetPlayerIdAndStatus(challengerId, request.targetPlayerId(), ChallengeStatus.PENDING)) {
                throw new ChallengeConflictException("An active pending challenge already exists for this player");
            }
        }

        PlayerGroup targetGroup = null;
        if (request.targetGroupId() != null) {
            targetGroup = playerGroupRepository.findById(request.targetGroupId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target group not found: " + request.targetGroupId()));
            if (matchChallengeRepository.existsByChallengerIdAndTargetGroupIdAndStatus(challengerId, request.targetGroupId(), ChallengeStatus.PENDING)) {
                throw new ChallengeConflictException("An active pending challenge already exists for this group");
            }
        }

        RuleConfiguration ruleConfig = null;
        if (request.ruleConfigId() != null) {
            ruleConfig = ruleConfigurationRepository.findById(request.ruleConfigId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rule configuration not found: " + request.ruleConfigId()));
        }

        MatchChallenge challenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetPlayer(targetPlayer)
                .targetGroup(targetGroup)
                .matchType(request.matchType())
                .ruleConfig(ruleConfig)
                .message(request.message())
                .status(ChallengeStatus.PENDING)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build();

        MatchChallenge saved = challengeOperation.saveChallenge(challenge);

        eventPublisher.publishEvent(new ChallengeCreatedEvent(
                saved.getId(),
                challenger.getId(),
                challenger.getNickname(),
                saved.getTargetPlayer() != null ? saved.getTargetPlayer().getId() : null,
                saved.getTargetGroup() != null ? saved.getTargetGroup().getId() : null,
                saved.getMatchType()
        ));

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChallengeResponse> getIncomingChallenges(UUID userId) {
        List<UUID> groupIds = playerGroupRepository.findGroupIdsByMemberId(userId);
        return matchChallengeRepository.findIncomingChallenges(userId, groupIds, ChallengeStatus.PENDING).stream()
                .filter(challenge -> !challenge.getChallenger().getId().equals(userId))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChallengeResponse> getOutgoingChallenges(UUID userId) {
        return matchChallengeRepository.findByChallengerIdAndStatus(userId, ChallengeStatus.PENDING).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ChallengeResponse getChallengeById(UUID challengeId, UUID userId) {
        MatchChallenge challenge = matchChallengeRepository.findByIdWithDetails(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found: " + challengeId));

        List<UUID> groupIds = playerGroupRepository.findGroupIdsByMemberId(userId);
        boolean isChallenger = challenge.getChallenger() != null && challenge.getChallenger().getId().equals(userId);
        boolean isTargetPlayer = challenge.getTargetPlayer() != null && challenge.getTargetPlayer().getId().equals(userId);
        boolean isTargetGroupMember = challenge.getTargetGroup() != null && groupIds.contains(challenge.getTargetGroup().getId());

        if (!isChallenger && !isTargetPlayer && !isTargetGroupMember) {
            throw new AccessDeniedException("User is not authorized to view this challenge");
        }

        return toResponse(challenge);
    }

    @Override
    public ChallengeActionResponse acceptChallenge(UUID challengeId, UUID userId) {
        List<UUID> groupIds = playerGroupRepository.findGroupIdsByMemberId(userId);
        MatchChallenge challenge = challengeOperation.acceptChallenge(challengeId, userId, groupIds);

        User actingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        eventPublisher.publishEvent(new ChallengeAcceptedEvent(
                challenge.getId(),
                challenge.getChallenger().getId(),
                userId,
                actingUser.getNickname(),
                challenge.getMatchType()
        ));

        return new ChallengeActionResponse(challenge.getId(), ChallengeStatus.ACCEPTED, "Challenge accepted successfully");
    }

    @Override
    public ChallengeActionResponse declineChallenge(UUID challengeId, UUID userId) {
        List<UUID> groupIds = playerGroupRepository.findGroupIdsByMemberId(userId);
        MatchChallenge challenge = challengeOperation.declineChallenge(challengeId, userId, groupIds);

        User actingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        eventPublisher.publishEvent(new ChallengeDeclinedEvent(
                challenge.getId(),
                challenge.getChallenger().getId(),
                userId,
                actingUser.getNickname()
        ));

        return new ChallengeActionResponse(challenge.getId(), ChallengeStatus.DECLINED, "Challenge declined successfully");
    }

    @Override
    public ChallengeActionResponse cancelChallenge(UUID challengeId, UUID userId) {
        MatchChallenge challenge = challengeOperation.cancelChallenge(challengeId, userId);
        return new ChallengeActionResponse(challenge.getId(), ChallengeStatus.CANCELLED, "Challenge cancelled successfully");
    }

    private ChallengeResponse toResponse(MatchChallenge c) {
        return new ChallengeResponse(
                c.getId(),
                c.getChallenger() != null ? c.getChallenger().getId() : null,
                c.getChallenger() != null ? c.getChallenger().getNickname() : null,
                c.getChallenger() != null ? c.getChallenger().getAvatar() : null,
                c.getTargetPlayer() != null ? c.getTargetPlayer().getId() : null,
                c.getTargetPlayer() != null ? c.getTargetPlayer().getNickname() : null,
                c.getTargetPlayer() != null ? c.getTargetPlayer().getAvatar() : null,
                c.getTargetGroup() != null ? c.getTargetGroup().getId() : null,
                c.getTargetGroup() != null ? c.getTargetGroup().getName() : null,
                c.getMatchType(),
                c.getRuleConfig() != null ? c.getRuleConfig().getId() : null,
                c.getRuleConfig() != null ? c.getRuleConfig().getName() : null,
                c.getMessage(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getExpiresAt()
        );
    }
}
