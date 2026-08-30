package com.tictactore.service.operation;

import com.tictactore.annotation.Idempotent;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.MatchChallenge;
import com.tictactore.repository.MatchChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChallengeOperation {

    private final MatchChallengeRepository matchChallengeRepository;

    @Idempotent
    @Transactional
    public MatchChallenge saveChallenge(MatchChallenge challenge) {
        return matchChallengeRepository.save(challenge);
    }

    @Idempotent
    @Transactional
    public MatchChallenge acceptChallenge(UUID challengeId, UUID userId, Collection<UUID> groupIds) {
        MatchChallenge challenge = matchChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found: " + challengeId));
        challenge.accept(userId, groupIds);
        return matchChallengeRepository.save(challenge);
    }

    @Idempotent
    @Transactional
    public MatchChallenge declineChallenge(UUID challengeId, UUID userId, Collection<UUID> groupIds) {
        MatchChallenge challenge = matchChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found: " + challengeId));
        challenge.decline(userId, groupIds);
        return matchChallengeRepository.save(challenge);
    }

    @Idempotent
    @Transactional
    public MatchChallenge cancelChallenge(UUID challengeId, UUID userId) {
        MatchChallenge challenge = matchChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Challenge not found: " + challengeId));
        challenge.cancel(userId);
        return matchChallengeRepository.save(challenge);
    }
}
