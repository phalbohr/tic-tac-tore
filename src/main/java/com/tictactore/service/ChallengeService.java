package com.tictactore.service;

import com.tictactore.dto.ChallengeActionResponse;
import com.tictactore.dto.ChallengeResponse;
import com.tictactore.dto.CreateChallengeRequest;

import java.util.List;
import java.util.UUID;

public interface ChallengeService {

    ChallengeResponse createChallenge(UUID challengerId, CreateChallengeRequest request);

    List<ChallengeResponse> getIncomingChallenges(UUID userId);

    List<ChallengeResponse> getOutgoingChallenges(UUID userId);

    ChallengeResponse getChallengeById(UUID challengeId, UUID userId);

    ChallengeActionResponse acceptChallenge(UUID challengeId, UUID userId);

    ChallengeActionResponse declineChallenge(UUID challengeId, UUID userId);

    ChallengeActionResponse cancelChallenge(UUID challengeId, UUID userId);
}
