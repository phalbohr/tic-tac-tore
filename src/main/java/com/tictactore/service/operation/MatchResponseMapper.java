package com.tictactore.service.operation;

import com.tictactore.dto.GameDto;
import com.tictactore.dto.MatchResponse;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.rules.VerificationRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchResponseMapper {

    private final UserRepository userRepository;

    public MatchResponse mapToResponse(Match match) {
        Set<UUID> allUserIds = new HashSet<>();
        if (match.getCreatorId() != null) allUserIds.add(match.getCreatorId());
        if (match.getTeamAAttackerId() != null) allUserIds.add(match.getTeamAAttackerId());
        if (match.getTeamADefenderId() != null) allUserIds.add(match.getTeamADefenderId());
        if (match.getTeamBAttackerId() != null) allUserIds.add(match.getTeamBAttackerId());
        if (match.getTeamBDefenderId() != null) allUserIds.add(match.getTeamBDefenderId());

        Map<UUID, User> userMap = new HashMap<>();
        if (!allUserIds.isEmpty()) {
            try {
                for (User u : userRepository.findAllById(allUserIds)) {
                    if (u != null && u.getId() != null) {
                        userMap.put(u.getId(), u);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to resolve users for match {}", match.getId(), e);
            }
        }

        return mapToResponseWithUserMap(match, userMap);
    }

    public MatchResponse mapToResponseWithUserMap(Match match, Map<UUID, User> userMap) {
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
                getDisplayName(userMap.get(match.getCreatorId())),
                getDisplayName(userMap.get(match.getTeamAAttackerId())),
                getDisplayName(userMap.get(match.getTeamADefenderId())),
                getDisplayName(userMap.get(match.getTeamBAttackerId())),
                getDisplayName(userMap.get(match.getTeamBDefenderId())),
                getAvatar(userMap.get(match.getCreatorId())),
                getAvatar(userMap.get(match.getTeamAAttackerId())),
                getAvatar(userMap.get(match.getTeamADefenderId())),
                getAvatar(userMap.get(match.getTeamBAttackerId())),
                getAvatar(userMap.get(match.getTeamBDefenderId())),
                match.getEntryMode(),
                match.getMatchFormat(),
                match.getConfirmedByOpponentIdsList(),
                VerificationRules.getRequiredConfirmations(match),
                match.getCooldownExpiresAt()
        );
    }

    public String getDisplayName(User user) {
        if (user == null) return null;
        if (user.getNickname() != null && user.getNickname().startsWith("ex-player-")) {
            return "Retired Player";
        }
        String name = user.getNickname();
        if (name == null || name.isBlank()) {
            return "Retired Player";
        }
        return name;
    }

    public String getAvatar(User user) {
        if (user != null && user.getNickname() != null && user.getNickname().startsWith("ex-player-")) {
            return null;
        }
        return user != null ? user.getAvatar() : null;
    }

    public String resolveDisplayName(UUID userId) {
        if (userId == null) return "A player";
        return userRepository.findById(userId)
                .map(u -> {
                    if (u.getNickname() != null && u.getNickname().startsWith("ex-player-")) {
                        return "Retired Player";
                    }
                    return (u.getNickname() != null && !u.getNickname().isBlank()) ? u.getNickname() : "Retired Player";
                })
                .orElse("Retired Player");
    }
}
