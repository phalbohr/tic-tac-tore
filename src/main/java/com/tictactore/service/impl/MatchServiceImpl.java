package com.tictactore.service.impl;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.GameDto;
import com.tictactore.dto.MatchRejectionRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.DuplicatePlayerException;
import com.tictactore.exception.DuplicatePositionException;
import com.tictactore.exception.InvalidMatchScoreException;
import com.tictactore.exception.InvalidMatchStateException;
import com.tictactore.exception.InvalidPositionException;
import com.tictactore.exception.ParticipantNotFoundException;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.MatchService;
import com.tictactore.service.PushNotificationService;
import com.tictactore.service.RateLimitService;
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
@Retryable
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MatchOperation matchOperation;
    private final PushNotificationService pushNotificationService;
    private final RateLimitService rateLimitService;
    private final com.tictactore.service.operation.MatchResponseMapper matchResponseMapper;
    private final com.tictactore.repository.PlayerGroupRepository playerGroupRepository;
    private final com.tictactore.repository.TournamentMatchRepository tournamentMatchRepository;
    private final com.tictactore.service.tournament.TournamentMatchValidator tournamentMatchValidator;
    private final com.tictactore.repository.RuleConfigurationRepository ruleConfigurationRepository;

    @org.springframework.beans.factory.annotation.Autowired
    public MatchServiceImpl(
            MatchRepository matchRepository, UserRepository userRepository,
            MatchOperation matchOperation, PushNotificationService pushNotificationService,
            RateLimitService rateLimitService, com.tictactore.service.operation.MatchResponseMapper matchResponseMapper,
            com.tictactore.repository.PlayerGroupRepository playerGroupRepository,
            com.tictactore.repository.TournamentMatchRepository tournamentMatchRepository,
            com.tictactore.service.tournament.TournamentMatchValidator tournamentMatchValidator,
            com.tictactore.repository.RuleConfigurationRepository ruleConfigurationRepository
    ) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.matchOperation = matchOperation;
        this.pushNotificationService = pushNotificationService;
        this.rateLimitService = rateLimitService;
        this.matchResponseMapper = matchResponseMapper != null ? matchResponseMapper : new com.tictactore.service.operation.MatchResponseMapper(userRepository);
        this.playerGroupRepository = playerGroupRepository;
        this.tournamentMatchRepository = tournamentMatchRepository;
        this.tournamentMatchValidator = tournamentMatchValidator;
        this.ruleConfigurationRepository = ruleConfigurationRepository;
    }

    public MatchServiceImpl(
            MatchRepository matchRepository, UserRepository userRepository,
            MatchOperation matchOperation, PushNotificationService pushNotificationService,
            RateLimitService rateLimitService
    ) {
        this(matchRepository, userRepository, matchOperation, pushNotificationService, rateLimitService, new com.tictactore.service.operation.MatchResponseMapper(userRepository), null, null, null, null);
    }

    @Override
    public MatchResponse createMatch(CreateMatchRequest request) {
        if (request.idempotencyKey() != null && !request.idempotencyKey().isBlank()) {
            var existing = matchRepository.findByIdempotencyKey(request.idempotencyKey());
            if (existing.isPresent()) return matchResponseMapper.mapToResponse(existing.get());
        }

        rateLimitService.checkSubmissionLimit(request.creatorId() != null ? request.creatorId() : request.teamAAttackerId());

        if (request.teamAAttackerId() == null || request.teamBAttackerId() == null) {
            throw new InvalidPositionException("Attacker IDs must not be null");
        }
        if ((request.teamADefenderId() == null) != (request.teamBDefenderId() == null)) {
            throw new InvalidPositionException("Asymmetric defenders: both teams must have a defender in 2v2 matches");
        }

        com.tictactore.model.TournamentMatch tournamentMatch = null;
        if (request.tournamentMatchId() != null) {
            if (tournamentMatchRepository == null) {
                throw new ResourceNotFoundException("TournamentMatch", request.tournamentMatchId().toString());
            }
            tournamentMatch = tournamentMatchRepository.findById(request.tournamentMatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("TournamentMatch", request.tournamentMatchId().toString()));
            if (tournamentMatchValidator != null) {
                tournamentMatchValidator.validateTournamentMatchCreation(tournamentMatch, request);
            }
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

        List<User> foundUsers = userRepository.findAllById(playerIds);
        if (foundUsers.size() != playerIds.size()) {
            Set<UUID> foundIds = foundUsers.stream().map(User::getId).collect(Collectors.toSet());
            UUID missingId = playerIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElse(null);
            throw new ParticipantNotFoundException("Player not found with ID: " + missingId);
        }

        int maxGames = 3;
        if (request.ruleConfigId() != null && ruleConfigurationRepository != null) {
            var ruleConfig = ruleConfigurationRepository.findById(request.ruleConfigId())
                    .orElseThrow(() -> new ResourceNotFoundException("RuleConfiguration", request.ruleConfigId().toString()));
            maxGames = ruleConfig.getGameLimit();
        }

        if (request.games() == null || request.games().isEmpty() || request.games().size() > maxGames) {
            throw new InvalidMatchScoreException("Match must have between 1 and " + maxGames + " games");
        }

        for (GameDto gameDto : request.games()) {
            if (gameDto.teamAScore() < 0 || gameDto.teamAScore() > 100 ||
                gameDto.teamBScore() < 0 || gameDto.teamBScore() > 100) {
                throw new InvalidMatchScoreException("Game scores must be between 0 and 100");
            }
        }

        UUID creator = request.creatorId() != null ? request.creatorId() : request.teamAAttackerId();
        boolean isParticipant = creator != null && (
                creator.equals(request.teamAAttackerId())
                        || creator.equals(request.teamADefenderId())
                        || creator.equals(request.teamBAttackerId())
                        || creator.equals(request.teamBDefenderId()));
        String entryMode = request.entryMode() != null ? request.entryMode() :
                (isParticipant ? Match.ENTRY_MODE_PARTICIPANT : Match.ENTRY_MODE_REFEREE);
        String matchFormat = request.matchFormat() != null ? request.matchFormat() : Match.MATCH_FORMAT_STANDARD;

        Match match = Match.builder()
                .idempotencyKey(request.idempotencyKey())
                .creatorId(creator)
                .teamAAttackerId(request.teamAAttackerId())
                .teamADefenderId(request.teamADefenderId())
                .teamBAttackerId(request.teamBAttackerId())
                .teamBDefenderId(request.teamBDefenderId())
                .status("PENDING_APPROVAL")
                .entryMode(entryMode)
                .matchFormat(matchFormat)
                .ruleConfigId(request.ruleConfigId())
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
        if (tournamentMatch != null) {
            tournamentMatch.setMatch(savedMatch);
            tournamentMatchRepository.save(tournamentMatch);
        }

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

        return matchResponseMapper.mapToResponse(savedMatch);
    }

    @Override
    public com.tictactore.dto.PendingMatchesResponse getPendingMatches(UUID currentUserId) {
        if (currentUserId == null) {
            return new com.tictactore.dto.PendingMatchesResponse(0, List.of());
        }
        List<Match> pendingMatches = matchRepository.findByStatusIn(
                List.of(Match.STATUS_PENDING_APPROVAL, Match.STATUS_PARTIALLY_CONFIRMED));
        List<Match> rejectedMatches = matchRepository.findByStatusAndCreatorId("REJECTED", currentUserId);

        List<Match> userPendingMatches = new ArrayList<>();
        pendingMatches.stream()
                .filter(m -> isUserPendingApprover(m, currentUserId))
                .forEach(userPendingMatches::add);
        if (rejectedMatches != null) {
            userPendingMatches.addAll(rejectedMatches);
        }

        Set<UUID> allUserIds = new HashSet<>();
        for (Match m : userPendingMatches) {
            allUserIds.addAll(m.getParticipantIds());
            if (m.getCreatorId() != null) allUserIds.add(m.getCreatorId());
            if (m.getRejectedByUserId() != null) allUserIds.add(m.getRejectedByUserId());
        }

        Map<UUID, User> userMap = new HashMap<>();
        if (userRepository != null && !allUserIds.isEmpty()) {
            try {
                userRepository.findAllById(allUserIds).forEach(u -> {
                    if (u != null && u.getId() != null) userMap.put(u.getId(), u);
                });
            } catch (Exception e) {
                log.warn("Failed to resolve users for pending matches", e);
            }
        }

        List<MatchResponse> userPendingResponses = userPendingMatches.stream()
                .map(m -> matchResponseMapper.mapToResponseWithUserMap(m, userMap))
                .toList();

        return new com.tictactore.dto.PendingMatchesResponse(userPendingResponses.size(), userPendingResponses);
    }

    private boolean isUserPendingApprover(Match match, UUID userId) {
        if (userId == null || userId.equals(match.getCreatorId())) {
            return false;
        }
        if (match.hasConfirmed(userId)) {
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
            if (creatorId != null) opponents.remove(creatorId);
        }
        return opponents;
    }

    private boolean isIdenticalMatch(Match candidate, Match current, Collection<UUID> currentParticipants) {
        return new HashSet<>(candidate.getParticipantIds()).equals(new HashSet<>(currentParticipants));
    }

    @Override
    @Retryable
    public MatchResponse confirmMatch(UUID matchId, UUID userId, String idempotencyKey) {
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + matchId));

        if (Match.STATUS_CONFIRMED.equals(match.getStatus())) {
            if (match.hasConfirmed(userId)) return matchResponseMapper.mapToResponse(match);
            throw new InvalidMatchStateException("Match is already confirmed");
        }

        if (Match.STATUS_PARTIALLY_CONFIRMED.equals(match.getStatus()) && match.hasConfirmed(userId)) {
            return matchResponseMapper.mapToResponse(match);
        }

        var updatedMatch = matchOperation.confirmMatch(match, userId);

        if (Match.STATUS_PARTIALLY_CONFIRMED.equals(updatedMatch.getStatus())) {
            try {
                List<UUID> remaining = updatedMatch.getOpponentIds().stream()
                        .filter(oppId -> !updatedMatch.hasConfirmed(oppId))
                        .toList();
                if (!remaining.isEmpty()) {
                    List<User> remainingUsers = userRepository.findAllById(remaining);
                    String confirmerName = matchResponseMapper.resolveDisplayName(userId);
                    pushNotificationService.sendPartialConfirmationNotification(updatedMatch, remainingUsers, confirmerName);
                }
            } catch (Exception e) {
                log.error("Failed to dispatch partial confirmation notification for match {}", updatedMatch.getId(), e);
            }
        }

        return matchResponseMapper.mapToResponse(updatedMatch);
    }

    @Override
    @Retryable
    public MatchResponse rejectMatch(UUID matchId, UUID userId, MatchRejectionRequest request, String idempotencyKey) {
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with ID: " + matchId));

        if (Match.STATUS_REJECTED.equals(match.getStatus())) {
            if (userId.equals(match.getRejectedByUserId())) {
                return matchResponseMapper.mapToResponse(match);
            }
            throw new InvalidMatchStateException("Match is already rejected");
        }

        if (Match.STATUS_CONFIRMED.equals(match.getStatus())) {
            throw new InvalidMatchStateException("Match is already confirmed");
        }

        var reason = request != null ? request.reason() : null;
        var customReason = request != null ? request.customReason() : null;

        var updatedMatch = matchOperation.rejectMatch(matchId, userId, reason, customReason);

        rateLimitService.recordRejection(userId);

        try {
            if (updatedMatch.getCreatorId() != null) {
                userRepository.findById(updatedMatch.getCreatorId()).ifPresent(creator -> {
                    pushNotificationService.sendRejectionNotification(updatedMatch, creator, updatedMatch.getRejectionReason());
                });
            }
        } catch (Exception e) {
            log.error("Failed to dispatch push notification for rejected match {}", updatedMatch.getId(), e);
        }

        return matchResponseMapper.mapToResponse(updatedMatch);
    }

    @Override
    public com.tictactore.dto.PagedResponse<MatchResponse> getMatchHistory(
            UUID currentUserId,
            String status,
            UUID filterPlayerId,
            UUID ruleConfigId,
            String matchType,
            int page,
            int size
    ) {
        return getMatchHistory(currentUserId, status, filterPlayerId, null, ruleConfigId, matchType, page, size);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public com.tictactore.dto.PagedResponse<MatchResponse> getMatchHistory(
            UUID currentUserId,
            String status,
            UUID filterPlayerId,
            UUID groupId,
            UUID ruleConfigId,
            String matchType,
            int page,
            int size
    ) {
        if (currentUserId == null) {
            return new com.tictactore.dto.PagedResponse<>(List.of(), 0, size > 0 ? size : 10, 0L, 0);
        }
        String normalizedStatus = (status == null || status.isBlank()) ? "CONFIRMED" : status.toUpperCase().trim();
        String normalizedMatchType = (matchType == null || matchType.isBlank()) ? null : matchType.trim();
        int safePage = Math.max(0, page);
        int safeSize = size > 0 ? Math.min(size, 100) : 10;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(safePage, safeSize);

        org.springframework.data.domain.Page<Match> matchPage;
        if (groupId != null && playerGroupRepository != null) {
            var group = playerGroupRepository.findById(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found with ID: " + groupId));
            if (!currentUserId.equals(group.getCreatorId())) {
                throw new org.springframework.security.access.AccessDeniedException("Access denied to player group");
            }
            List<UUID> groupMemberIds = group.getMembers() != null
                    ? group.getMembers().stream().map(User::getId).toList()
                    : List.of();
            if (groupMemberIds.isEmpty()) {
                return new com.tictactore.dto.PagedResponse<>(List.of(), safePage, safeSize, 0L, 0);
            }
            matchPage = matchRepository.findMatchHistoryWithGroupMembers(
                    currentUserId, normalizedStatus, filterPlayerId, groupMemberIds, ruleConfigId, normalizedMatchType, pageable
            );
        } else {
            matchPage = matchRepository.findMatchHistory(
                    currentUserId, normalizedStatus, filterPlayerId, ruleConfigId, normalizedMatchType, pageable
            );
        }

        Set<UUID> allUserIds = new HashSet<>();
        for (Match m : matchPage.getContent()) {
            allUserIds.addAll(m.getParticipantIds());
            if (m.getCreatorId() != null) allUserIds.add(m.getCreatorId());
            if (m.getConfirmedByUserId() != null) allUserIds.add(m.getConfirmedByUserId());
            if (m.getRejectedByUserId() != null) allUserIds.add(m.getRejectedByUserId());
        }

        Map<UUID, User> userMap = new HashMap<>();
        if (!allUserIds.isEmpty()) {
            try {
                userRepository.findAllById(allUserIds).forEach(u -> {
                    if (u != null && u.getId() != null) userMap.put(u.getId(), u);
                });
            } catch (Exception e) {
                log.warn("Failed to resolve users for match history", e);
            }
        }

        List<MatchResponse> responses = matchPage.getContent().stream()
                .map(m -> matchResponseMapper.mapToResponseWithUserMap(m, userMap))
                .toList();

        return new com.tictactore.dto.PagedResponse<>(
                responses,
                matchPage.getNumber(),
                matchPage.getSize(),
                matchPage.getTotalElements(),
                matchPage.getTotalPages()
        );
    }

    @Override
    public void deleteMatch(UUID matchId, UUID userId) {
        matchOperation.deleteMatch(matchId, userId);
    }
}
