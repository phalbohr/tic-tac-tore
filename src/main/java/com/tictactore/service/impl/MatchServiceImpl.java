package com.tictactore.service.impl;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.GameDto;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.DuplicatePlayerException;
import com.tictactore.exception.DuplicatePositionException;
import com.tictactore.exception.InvalidMatchScoreException;
import com.tictactore.exception.InvalidPositionException;
import com.tictactore.exception.ParticipantNotFoundException;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.MatchService;
import com.tictactore.service.operation.MatchOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Retryable
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MatchOperation matchOperation;
    private final com.tictactore.service.PushNotificationService pushNotificationService;

    @Override
    public MatchResponse createMatch(CreateMatchRequest request) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            var existing = matchRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) {
                return mapToResponse(existing.get());
            }
        }

        if (request.teamAAttackerId() == null || request.teamBAttackerId() == null) {
            throw new InvalidPositionException("Attacker IDs must not be null");
        }

        if ((request.teamADefenderId() == null) != (request.teamBDefenderId() == null)) {
            throw new InvalidPositionException("Asymmetric defenders: both teams must have a defender in 2v2 matches");
        }

        List<UUID> playerIds = new ArrayList<>();
        playerIds.add(request.teamAAttackerId());
        if (request.teamADefenderId() != null) playerIds.add(request.teamADefenderId());
        playerIds.add(request.teamBAttackerId());
        if (request.teamBDefenderId() != null) playerIds.add(request.teamBDefenderId());

        Set<UUID> uniqueIds = new HashSet<>(playerIds);
        if (uniqueIds.size() != playerIds.size()) {
            throw new DuplicatePlayerException("Same player selected in multiple positions");
        }

        if (request.creatorId() != null && !uniqueIds.contains(request.creatorId())) {
            throw new ParticipantNotFoundException("Creator must be a participant in the match");
        }

        List<User> foundUsers = userRepository.findAllById(playerIds);
        if (foundUsers.size() != playerIds.size()) {
            Set<UUID> foundIds = foundUsers.stream().map(User::getId).collect(Collectors.toSet());
            UUID missingId = playerIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElse(null);
            throw new ParticipantNotFoundException("Player not found with ID: " + missingId);
        }

        if (request.games() == null || request.games().isEmpty() || request.games().size() > 3) {
            throw new InvalidMatchScoreException("Match must have between 1 and 3 games");
        }

        for (GameDto gameDto : request.games()) {
            if (gameDto.teamAScore() < 0 || gameDto.teamAScore() > 100 ||
                gameDto.teamBScore() < 0 || gameDto.teamBScore() > 100) {
                throw new InvalidMatchScoreException("Game scores must be between 0 and 100");
            }
        }

        UUID creator = request.creatorId() != null ? request.creatorId() : request.teamAAttackerId();

        Match match = Match.builder()
                .idempotencyKey(request.idempotencyKey())
                .creatorId(creator)
                .teamAAttackerId(request.teamAAttackerId())
                .teamADefenderId(request.teamADefenderId())
                .teamBAttackerId(request.teamBAttackerId())
                .teamBDefenderId(request.teamBDefenderId())
                .status("PENDING_APPROVAL")
                .createdAt(Instant.now())
                .build();

        for (int i = 0; i < request.games().size(); i++) {
            GameDto dto = request.games().get(i);

            if (request.teamADefenderId() == null) {
                if (dto.teamAAttackerId() != null || dto.teamADefenderId() != null ||
                    dto.teamBAttackerId() != null || dto.teamBDefenderId() != null) {
                    throw new InvalidPositionException("1v1 matches must not contain positional data");
                }
            } else {
                if (dto.teamAAttackerId() == null || dto.teamADefenderId() == null ||
                    dto.teamBAttackerId() == null || dto.teamBDefenderId() == null) {
                    throw new InvalidPositionException("2v2 games must contain positional data");
                }

                Set<UUID> gamePositions = new HashSet<>(Arrays.asList(
                    dto.teamAAttackerId(), dto.teamADefenderId(),
                    dto.teamBAttackerId(), dto.teamBDefenderId()
                ));
                if (gamePositions.size() != 4) {
                    throw new DuplicatePositionException("Same player selected in multiple positions");
                }

                if (!gamePositions.equals(uniqueIds)) {
                    throw new InvalidPositionException("Game players must match match players");
                }
                
                Set<UUID> matchTeamA = Set.of(request.teamAAttackerId(), request.teamADefenderId());
                Set<UUID> matchTeamB = Set.of(request.teamBAttackerId(), request.teamBDefenderId());
                Set<UUID> gameTeamA = Set.of(dto.teamAAttackerId(), dto.teamADefenderId());
                Set<UUID> gameTeamB = Set.of(dto.teamBAttackerId(), dto.teamBDefenderId());

                if (!matchTeamA.equals(gameTeamA) || !matchTeamB.equals(gameTeamB)) {
                    throw new InvalidPositionException("Team A players cannot be assigned to Team B positions");
                }
            }

            Game game = Game.builder()
                    .gameOrder(i + 1)
                    .teamAScore(dto.teamAScore())
                    .teamBScore(dto.teamBScore())
                    .teamAAttackerId(dto.teamAAttackerId())
                    .teamADefenderId(dto.teamADefenderId())
                    .teamBAttackerId(dto.teamBAttackerId())
                    .teamBDefenderId(dto.teamBDefenderId())
                    .build();
            match.addGame(game);
        }

        Match savedMatch = matchOperation.saveMatch(match);

        try {
            java.time.ZonedDateTime nowUtc = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC);
            Instant startOfDay = nowUtc.toLocalDate().atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
            Instant endOfDay = nowUtc.toLocalDate().atTime(23, 59, 59, 999_999_999).atZone(java.time.ZoneOffset.UTC).toInstant();

            List<Match> candidateDuplicates = matchRepository.findDuplicatesOnDate(
                    startOfDay, endOfDay,
                    request.teamAAttackerId(), request.teamBAttackerId(),
                    request.teamADefenderId(), request.teamBDefenderId()
            );

            boolean isDuplicateWarning = candidateDuplicates.stream()
                    .filter(m -> !m.getId().equals(savedMatch.getId()))
                    .anyMatch(m -> isIdenticalMatch(m, savedMatch, uniqueIds));

            List<UUID> opponentIds = resolveOpponentIds(request, uniqueIds);
            List<User> opponents = userRepository.findAllById(opponentIds);
            pushNotificationService.sendConfirmationRequest(savedMatch, opponents, isDuplicateWarning);
        } catch (Exception e) {
            log.error("Failed to dispatch push notifications for match {}", savedMatch.getId(), e);
        }

        return mapToResponse(savedMatch);
    }

    @Override
    public com.tictactore.dto.PendingMatchesResponse getPendingMatches(UUID currentUserId) {
        if (currentUserId == null) {
            return new com.tictactore.dto.PendingMatchesResponse(0, List.of());
        }
        List<Match> pendingMatches = matchRepository.findByStatus("PENDING_APPROVAL");
        List<Match> rejectedMatches = matchRepository.findByStatus("REJECTED");

        List<Match> userPendingMatches = new ArrayList<>();
        pendingMatches.stream()
                .filter(m -> isUserPendingApprover(m, currentUserId))
                .forEach(userPendingMatches::add);
        rejectedMatches.stream()
                .filter(m -> currentUserId.equals(m.getCreatorId()))
                .forEach(userPendingMatches::add);

        Set<UUID> allUserIds = new HashSet<>();
        for (Match m : userPendingMatches) {
            if (m.getCreatorId() != null) allUserIds.add(m.getCreatorId());
            if (m.getTeamAAttackerId() != null) allUserIds.add(m.getTeamAAttackerId());
            if (m.getTeamADefenderId() != null) allUserIds.add(m.getTeamADefenderId());
            if (m.getTeamBAttackerId() != null) allUserIds.add(m.getTeamBAttackerId());
            if (m.getTeamBDefenderId() != null) allUserIds.add(m.getTeamBDefenderId());
            if (m.getRejectedByUserId() != null) allUserIds.add(m.getRejectedByUserId());
        }

        Map<UUID, String> userNicknameMap = new HashMap<>();
        if (userRepository != null && !allUserIds.isEmpty()) {
            try {
                for (User u : userRepository.findAllById(allUserIds)) {
                    if (u != null && u.getId() != null) {
                        String name = u.getNickname();
                        if (name == null || name.isBlank()) {
                            name = u.getEmail();
                        }
                        if (name != null) {
                            userNicknameMap.put(u.getId(), name);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to resolve user nicknames for pending matches", e);
            }
        }

        List<MatchResponse> userPendingResponses = userPendingMatches.stream()
                .map(m -> mapToResponseWithUserMap(m, userNicknameMap))
                .toList();

        return new com.tictactore.dto.PendingMatchesResponse(userPendingResponses.size(), userPendingResponses);
    }

    private boolean isUserPendingApprover(Match match, UUID userId) {
        if (userId == null || userId.equals(match.getCreatorId())) {
            return false;
        }
        UUID creatorId = match.getCreatorId();
        boolean creatorOnTeamA = creatorId != null && (creatorId.equals(match.getTeamAAttackerId()) || creatorId.equals(match.getTeamADefenderId()));
        boolean creatorOnTeamB = creatorId != null && (creatorId.equals(match.getTeamBAttackerId()) || creatorId.equals(match.getTeamBDefenderId()));

        if (creatorOnTeamA) {
            return userId.equals(match.getTeamBAttackerId()) || userId.equals(match.getTeamBDefenderId());
        } else if (creatorOnTeamB) {
            return userId.equals(match.getTeamAAttackerId()) || userId.equals(match.getTeamADefenderId());
        } else {
            return userId.equals(match.getTeamAAttackerId()) || userId.equals(match.getTeamADefenderId())
                || userId.equals(match.getTeamBAttackerId()) || userId.equals(match.getTeamBDefenderId());
        }
    }

    private List<UUID> resolveOpponentIds(CreateMatchRequest request, Collection<UUID> allParticipants) {
        UUID creatorId = request.creatorId();
        boolean isOnTeamA = creatorId != null && (creatorId.equals(request.teamAAttackerId()) || creatorId.equals(request.teamADefenderId()));
        boolean isOnTeamB = creatorId != null && (creatorId.equals(request.teamBAttackerId()) || creatorId.equals(request.teamBDefenderId()));

        List<UUID> opponents = new ArrayList<>();
        if (isOnTeamA) {
            if (request.teamBAttackerId() != null) opponents.add(request.teamBAttackerId());
            if (request.teamBDefenderId() != null) opponents.add(request.teamBDefenderId());
        } else if (isOnTeamB) {
            if (request.teamAAttackerId() != null) opponents.add(request.teamAAttackerId());
            if (request.teamADefenderId() != null) opponents.add(request.teamADefenderId());
        } else {
            opponents.addAll(allParticipants);
            if (creatorId != null) {
                opponents.remove(creatorId);
            }
        }
        return opponents;
    }

    private boolean isIdenticalMatch(Match candidate, Match current, Collection<UUID> currentParticipants) {
        List<UUID> candidateParticipants = new ArrayList<>();
        if (candidate.getTeamAAttackerId() != null) candidateParticipants.add(candidate.getTeamAAttackerId());
        if (candidate.getTeamADefenderId() != null) candidateParticipants.add(candidate.getTeamADefenderId());
        if (candidate.getTeamBAttackerId() != null) candidateParticipants.add(candidate.getTeamBAttackerId());
        if (candidate.getTeamBDefenderId() != null) candidateParticipants.add(candidate.getTeamBDefenderId());

        if (new HashSet<>(candidateParticipants).equals(new HashSet<>(currentParticipants))) {
            return true;
        }
        return false;
    }

    @Override
    public MatchResponse confirmMatch(UUID matchId, UUID userId, String idempotencyKey) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new com.tictactore.exception.ResourceNotFoundException("Match not found with ID: " + matchId));

        if ("CONFIRMED".equals(match.getStatus())) {
            if (userId.equals(match.getConfirmedByUserId())) {
                return mapToResponse(match);
            }
            throw new com.tictactore.exception.InvalidMatchStateException("Match is already confirmed");
        }

        Match updatedMatch = matchOperation.confirmMatch(match, userId);
        return mapToResponse(updatedMatch);
    }

    @Override
    public MatchResponse rejectMatch(UUID matchId, UUID userId, com.tictactore.dto.MatchRejectionRequest request, String idempotencyKey) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new com.tictactore.exception.ResourceNotFoundException("Match not found with ID: " + matchId));

        if ("REJECTED".equals(match.getStatus())) {
            if (userId.equals(match.getRejectedByUserId())) {
                return mapToResponse(match);
            }
            throw new com.tictactore.exception.InvalidMatchStateException("Match is already rejected");
        }

        if ("CONFIRMED".equals(match.getStatus())) {
            throw new com.tictactore.exception.InvalidMatchStateException("Match is already confirmed");
        }

        String reason = request != null ? request.reason() : null;
        String customReason = request != null ? request.customReason() : null;

        Match updatedMatch = matchOperation.rejectMatch(matchId, userId, reason, customReason);

        try {
            if (updatedMatch.getCreatorId() != null) {
                userRepository.findById(updatedMatch.getCreatorId()).ifPresent(creator -> {
                    pushNotificationService.sendRejectionNotification(updatedMatch, creator, updatedMatch.getRejectionReason());
                });
            }
        } catch (Exception e) {
            log.error("Failed to dispatch push notification for rejected match {}", updatedMatch.getId(), e);
        }

        return mapToResponse(updatedMatch);
    }

    private MatchResponse mapToResponse(Match match) {
        Set<UUID> allUserIds = new HashSet<>();
        if (match.getCreatorId() != null) allUserIds.add(match.getCreatorId());
        if (match.getTeamAAttackerId() != null) allUserIds.add(match.getTeamAAttackerId());
        if (match.getTeamADefenderId() != null) allUserIds.add(match.getTeamADefenderId());
        if (match.getTeamBAttackerId() != null) allUserIds.add(match.getTeamBAttackerId());
        if (match.getTeamBDefenderId() != null) allUserIds.add(match.getTeamBDefenderId());

        Map<UUID, String> userNicknameMap = new HashMap<>();
        if (userRepository != null && !allUserIds.isEmpty()) {
            try {
                for (User u : userRepository.findAllById(allUserIds)) {
                    if (u != null && u.getId() != null) {
                        String name = u.getNickname();
                        if (name == null || name.isBlank()) {
                            name = u.getEmail();
                        }
                        if (name != null) {
                            userNicknameMap.put(u.getId(), name);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to resolve user nicknames", e);
            }
        }

        return mapToResponseWithUserMap(match, userNicknameMap);
    }

    private MatchResponse mapToResponseWithUserMap(Match match, Map<UUID, String> userNicknameMap) {
        List<GameDto> gameDtos = match.getGames().stream()
                .map(g -> new GameDto(
                    g.getTeamAScore(), g.getTeamBScore(),
                    g.getTeamAAttackerId(), g.getTeamADefenderId(),
                    g.getTeamBAttackerId(), g.getTeamBDefenderId()
                ))
                .collect(Collectors.toList());

        return new MatchResponse(
                match.getId(),
                match.getIdempotencyKey(),
                match.getCreatorId(),
                match.getTeamAAttackerId(),
                match.getTeamADefenderId(),
                match.getTeamBAttackerId(),
                match.getTeamBDefenderId(),
                match.getStatus(),
                gameDtos,
                match.getCreatedAt(),
                match.getConfirmedByUserId(),
                match.getConfirmedAt(),
                match.getRejectedByUserId(),
                match.getRejectedAt(),
                match.getRejectionReason(),
                userNicknameMap.get(match.getCreatorId()),
                userNicknameMap.get(match.getTeamAAttackerId()),
                userNicknameMap.get(match.getTeamADefenderId()),
                userNicknameMap.get(match.getTeamBAttackerId()),
                userNicknameMap.get(match.getTeamBDefenderId())
        );
    }
}
