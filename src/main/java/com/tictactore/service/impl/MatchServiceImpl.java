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
    public com.tictactore.dto.PendingMatchesResponse getPendingMatches() {
        List<Match> pendingMatches = matchRepository.findByStatus("PENDING_APPROVAL");
        List<MatchResponse> responses = pendingMatches.stream().map(this::mapToResponse).toList();
        return new com.tictactore.dto.PendingMatchesResponse(responses.size(), responses);
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

    private MatchResponse mapToResponse(Match match) {
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
                match.getCreatedAt()
        );
    }
}
