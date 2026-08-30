package com.tictactore.service.impl;

import com.tictactore.dto.AchievementDto;
import com.tictactore.dto.PlayerAchievementsSummaryResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.Achievement;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.PlayerAchievement;
import com.tictactore.model.User;
import com.tictactore.repository.AchievementRepository;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.PlayerAchievementRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.AchievementService;
import com.tictactore.service.achievement.AchievementEvaluator;
import com.tictactore.service.achievement.PlayerStatsContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementRepository achievementRepository;
    private final PlayerAchievementRepository playerAchievementRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final List<AchievementEvaluator> evaluators;

    @Override
    @Transactional(readOnly = true)
    public PlayerAchievementsSummaryResponse getPlayerAchievements(UUID playerId) {
        if (playerId == null) {
            throw new ResourceNotFoundException("Player ID must not be null");
        }

        List<Achievement> allCatalog = achievementRepository.findAll();
        List<PlayerAchievement> unlocked = playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(playerId);

        Map<UUID, PlayerAchievement> unlockedMap = unlocked.stream()
                .filter(pa -> pa.getAchievement() != null && pa.getAchievement().getId() != null)
                .collect(Collectors.toMap(pa -> pa.getAchievement().getId(), pa -> pa, (a, b) -> a));

        Map<String, AchievementEvaluator> evaluatorMap = evaluators.stream()
                .collect(Collectors.toMap(AchievementEvaluator::getAchievementCode, e -> e, (a, b) -> a));

        PlayerStatsContext statsContext = buildPlayerStatsContext(playerId);

        List<AchievementDto> dtos = allCatalog.stream()
                .sorted(Comparator.comparing(Achievement::getCode))
                .map(achievement -> mapToDto(
                        achievement,
                        unlockedMap.get(achievement.getId()),
                        evaluatorMap.get(achievement.getCode()),
                        playerId,
                        statsContext
                ))
                .toList();

        return new PlayerAchievementsSummaryResponse(
                playerId,
                unlockedMap.size(),
                allCatalog.size(),
                dtos
        );
    }

    @Override
    @Transactional
    public void evaluateMatchAchievements(UUID matchId, List<UUID> participantIds) {
        if (matchId == null || participantIds == null || participantIds.isEmpty()) {
            return;
        }

        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            log.warn("Match not found for achievement evaluation: {}", matchId);
            return;
        }
        Match match = matchOpt.get();

        List<Achievement> catalog = achievementRepository.findAll();
        Map<String, Achievement> catalogByCode = catalog.stream()
                .collect(Collectors.toMap(Achievement::getCode, a -> a, (a, b) -> a));

        for (UUID participantId : participantIds) {
            evaluateParticipant(participantId, match, catalogByCode);
        }
    }

    private void evaluateParticipant(UUID participantId, Match match, Map<String, Achievement> catalogByCode) {
        Optional<User> userOpt = userRepository.findById(participantId);
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();

        List<PlayerAchievement> existingUnlocked = playerAchievementRepository.findByUserIdOrderByUnlockedAtDesc(participantId);
        Set<String> unlockedCodes = existingUnlocked.stream()
                .filter(pa -> pa.getAchievement() != null)
                .map(pa -> pa.getAchievement().getCode())
                .collect(Collectors.toSet());

        PlayerStatsContext statsContext = buildPlayerStatsContext(participantId);

        for (AchievementEvaluator evaluator : evaluators) {
            String code = evaluator.getAchievementCode();
            if (unlockedCodes.contains(code)) {
                continue;
            }

            boolean eligible = evaluator.evaluate(participantId, match, statsContext);
            if (eligible) {
                Achievement achievement = catalogByCode.get(code);
                if (achievement != null) {
                    awardAchievement(user, achievement);
                    unlockedCodes.add(code);
                }
            }
        }
    }

    private void awardAchievement(User user, Achievement achievement) {
        try {
            if (playerAchievementRepository.existsByUserIdAndAchievementId(user.getId(), achievement.getId())) {
                return;
            }
            PlayerAchievement playerAchievement = PlayerAchievement.builder()
                    .user(user)
                    .achievement(achievement)
                    .unlockedAt(Instant.now())
                    .build();
            playerAchievementRepository.save(playerAchievement);
            log.info("Awarded achievement {} to user {}", achievement.getCode(), user.getId());
        } catch (DataIntegrityViolationException e) {
            log.debug("Achievement {} already awarded to user {} (concurrent insert)", achievement.getCode(), user.getId());
        }
    }

    private PlayerStatsContext buildPlayerStatsContext(UUID playerId) {
        long totalMatches = matchRepository.countConfirmedMatchesByPlayerId(playerId);
        long totalMatchesAsDefender = matchRepository.countConfirmedMatchesAsDefender(playerId);
        long totalGoalsAsAttacker = matchRepository.sumGoalsAsAttacker(playerId);
        long totalWins = countTotalWins(playerId);

        return new PlayerStatsContext(
                playerId,
                totalMatches,
                totalWins,
                totalGoalsAsAttacker,
                totalMatchesAsDefender
        );
    }

    private long countTotalWins(UUID playerId) {
        List<Match> matches = matchRepository.findConfirmedMatchesByPlayerId(playerId);
        long wins = 0;
        for (Match match : matches) {
            if (isPlayerWinnerInMatch(playerId, match)) {
                wins++;
            }
        }
        return wins;
    }

    private boolean isPlayerWinnerInMatch(UUID playerId, Match match) {
        if (match == null || match.getGames() == null || match.getGames().isEmpty()) {
            return false;
        }
        boolean onTeamA = playerId.equals(match.getTeamAAttackerId()) || playerId.equals(match.getTeamADefenderId());
        boolean onTeamB = playerId.equals(match.getTeamBAttackerId()) || playerId.equals(match.getTeamBDefenderId());
        if (!onTeamA && !onTeamB) {
            return false;
        }
        long teamAWins = match.getGames().stream().filter(g -> g.getTeamAScore() > g.getTeamBScore()).count();
        long teamBWins = match.getGames().stream().filter(g -> g.getTeamBScore() > g.getTeamAScore()).count();
        return (onTeamA && teamAWins > teamBWins) || (onTeamB && teamBWins > teamAWins);
    }

    private AchievementDto mapToDto(
            Achievement achievement,
            PlayerAchievement unlockedRecord,
            AchievementEvaluator evaluator,
            UUID playerId,
            PlayerStatsContext statsContext
    ) {
        boolean isUnlocked = unlockedRecord != null;
        OffsetDateTime unlockedAt = isUnlocked && unlockedRecord.getUnlockedAt() != null
                ? unlockedRecord.getUnlockedAt().atOffset(ZoneOffset.UTC)
                : null;

        var progressInfo = evaluator != null
                ? evaluator.getProgress(playerId, statsContext)
                : new com.tictactore.service.achievement.ProgressInfo(0, 0, false);

        Long currentProgress = null;
        Long targetValue = null;
        boolean hasProgress = progressInfo.hasProgress();

        if (hasProgress) {
            targetValue = progressInfo.target();
            if (isUnlocked) {
                currentProgress = targetValue;
            } else {
                currentProgress = Math.min(progressInfo.current(), Math.max(0L, targetValue - 1));
            }
        }

        return new AchievementDto(
                achievement.getId(),
                achievement.getCode(),
                achievement.getCategory(),
                achievement.getNameKey(),
                achievement.getDescriptionKey(),
                achievement.getIcon(),
                isUnlocked,
                unlockedAt,
                currentProgress,
                targetValue,
                hasProgress
        );
    }
}
