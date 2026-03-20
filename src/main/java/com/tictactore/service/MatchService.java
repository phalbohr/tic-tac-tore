package com.tictactore.service;

import com.tictactore.dto.MatchRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.mapper.MatchMapper;
import com.tictactore.model.Match;
import com.tictactore.model.MatchStatus;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MatchMapper matchMapper;
    private final MatchOperation matchOperation;

    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void approveMatch(UUID matchId) {
        log.info("Attempting to approve match with ID: {}", matchId);
        var currentUser = getCurrentUser();
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        validateUserIsOpponent(currentUser, match);
        matchOperation.approveMatch(matchId);
        log.info("Match {} successfully approved by user {}", matchId, currentUser.getId());
    }

    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public void rejectMatch(UUID matchId) {
        log.info("Attempting to reject match with ID: {}", matchId);
        var currentUser = getCurrentUser();
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        validateUserIsOpponent(currentUser, match);
        matchOperation.rejectMatch(matchId);
        log.info("Match {} successfully rejected by user {}", matchId, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> getPendingMatchesForCurrentUser() {
        var currentUser = getCurrentUser();
        log.debug("Fetching pending matches for user ID: {}", currentUser.getId());
        
        var pendingMatches = matchRepository.findPendingApprovalsForUser(currentUser.getId(), MatchStatus.PENDING_APPROVAL);
        log.debug("Found {} pending matches", pendingMatches.size());
        
        return pendingMatches.stream()
                .map(matchMapper::toResponse)
                .toList();
    }

    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public MatchResponse createMatch(MatchRequest request) {
        log.info("Creating a new 2v2 match record");
        var creator = getCurrentUser();
        validateCreatorIsParticipant(creator.getId(), request);
        return matchOperation.createMatch(request, creator);
    }

    private User getCurrentUser() {
        var email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElseThrow(() -> new IllegalStateException("Authentication context is missing"));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateUserIsOpponent(User user, Match match) {
        var isCreatorTeamA = match.isUserInTeamA(match.getCreator());
        var isUserTeamA = match.isUserInTeamA(user);
        var isUserTeamB = match.isUserInTeamB(user);

        if (!isUserTeamA && !isUserTeamB) {
            throw new IllegalArgumentException("User is not a participant in this match");
        }

        if (isCreatorTeamA == isUserTeamA) {
            throw new IllegalArgumentException("Only an opponent can approve this match");
        }
    }

    private void validateCreatorIsParticipant(UUID creatorId, MatchRequest request) {
        var isParticipant = Stream.of(
                request.teamAAttackerId(), request.teamADefenderId(),
                request.teamBAttackerId(), request.teamBDefenderId()
        ).anyMatch(id -> id.equals(creatorId));

        if (!isParticipant) {
            log.warn("User {} attempted to record a match they didn't participate in", creatorId);
            throw new IllegalArgumentException("You must be a participant in the match to record it");
        }
    }
}
