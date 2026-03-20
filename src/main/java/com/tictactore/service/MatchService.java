package com.tictactore.service;

import com.tictactore.dto.GameRequest;
import com.tictactore.dto.MatchRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.mapper.MatchMapper;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.MatchStatus;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @purpose Orchestrates match-related business operations and lifecycle.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MatchMapper matchMapper;

    @Transactional
    public void approveMatch(UUID matchId) {
        log.info("Attempting to approve match with ID: {}", matchId);

        var currentUser = getCurrentUser();
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        validateUserIsOpponent(currentUser, match);

        match.approve();
        matchRepository.save(match);
        log.info("Match {} successfully approved by user {}", matchId, currentUser.getId());
    }

    @Transactional
    public void rejectMatch(UUID matchId) {
        log.info("Attempting to reject match with ID: {}", matchId);

        var currentUser = getCurrentUser();
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));

        validateUserIsOpponent(currentUser, match);

        match.reject();
        matchRepository.save(match);
        log.info("Match {} successfully rejected by user {}", matchId, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> getPendingMatchesForCurrentUser() {
        var currentUser = getCurrentUser();
        log.info("Fetching pending matches for user {}", currentUser.getEmail());
        
        var pendingMatches = matchRepository.findPendingApprovalsForUser(currentUser, MatchStatus.PENDING_APPROVAL);
        
        return pendingMatches.stream()
                .map(matchMapper::toResponse)
                .toList();
    }

    private User getCurrentUser() {
        var email = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElseThrow(() -> new IllegalStateException("Authentication context is missing"));

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validateUserIsOpponent(User user, Match match) {
        var isCreatorTeamA = isUserInTeamA(match.getCreator(), match);
        var isUserTeamA = isUserInTeamA(user, match);
        var isUserTeamB = isUserInTeamB(user, match);

        if (!isUserTeamA && !isUserTeamB) {
            throw new IllegalArgumentException("User is not a participant in this match");
        }

        if (isCreatorTeamA == isUserTeamA) {
            throw new IllegalArgumentException("Only an opponent can approve this match");
        }
    }

    private boolean isUserInTeamA(User user, Match match) {
        return user.getId().equals(match.getTeamAAttacker().getId()) ||
               user.getId().equals(match.getTeamADefender().getId());
    }

    private boolean isUserInTeamB(User user, Match match) {
        return user.getId().equals(match.getTeamBAttacker().getId()) ||
               user.getId().equals(match.getTeamBDefender().getId());
    }

    @Transactional
    public MatchResponse createMatch(MatchRequest request) {
        log.info("Creating a new 2v2 match record");

        var creatorEmail = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElseThrow(() -> {
                    log.error("Attempted to create match without authentication context");
                    return new IllegalStateException("Authentication context is missing");
                });

        var creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> {
                    log.error("Authenticated principal not found as a registered user");
                    return new ResourceNotFoundException("Creator not found");
                });

        validateCreatorIsParticipant(creator.getId(), request);

        var playerIds = List.of(
                request.teamAAttackerId(), request.teamADefenderId(),
                request.teamBAttackerId(), request.teamBDefenderId()
        );

        Map<UUID, User> usersMap = userRepository.findAllById(playerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        var match = new Match();
        match.setCreator(creator);
        match.setTeamAAttacker(getUserFromMap(usersMap, request.teamAAttackerId(), "Team A Attacker"));
        match.setTeamADefender(getUserFromMap(usersMap, request.teamADefenderId(), "Team A Defender"));
        match.setTeamBAttacker(getUserFromMap(usersMap, request.teamBAttackerId(), "Team B Attacker"));
        match.setTeamBDefender(getUserFromMap(usersMap, request.teamBDefenderId(), "Team B Defender"));

        for (int i = 0; i < request.games().size(); i++) {
            var gameReq = request.games().get(i);
            var game = new Game();
            game.setGameNumber(i + 1);
            game.setTeamAScore(gameReq.teamAScore());
            game.setTeamBScore(gameReq.teamBScore());
            match.addGame(game);
        }

        var savedMatch = matchRepository.save(match);
        log.info("Match created successfully with ID: {} and status: {}", savedMatch.getId(), savedMatch.getStatus());
        
        return matchMapper.toResponse(savedMatch);
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

    private User getUserFromMap(Map<UUID, User> usersMap, UUID id, String roleName) {
        return Optional.ofNullable(usersMap.get(id))
                .orElseThrow(() -> {
                    log.error("{} with ID {} not found during match creation", roleName, id);
                    return new ResourceNotFoundException(roleName + " not found");
                });
    }
}
