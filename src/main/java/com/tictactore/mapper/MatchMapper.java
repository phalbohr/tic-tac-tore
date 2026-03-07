package com.tictactore.mapper;

import com.tictactore.dto.GameResponse;
import com.tictactore.dto.MatchResponse;
import com.tictactore.model.Match;
import org.springframework.stereotype.Component;

@Component
public class MatchMapper {

    /**
     * Maps Match entity to MatchResponse record.
     */
    public MatchResponse toResponse(Match match) {
        if (match == null) return null;

        return MatchResponse.builder()
                .id(match.getId())
                .creatorName(match.getCreator().getName())
                .teamAAttackerName(match.getTeamAAttacker().getName())
                .teamADefenderName(match.getTeamADefender().getName())
                .teamBAttackerName(match.getTeamBAttacker().getName())
                .teamBDefenderName(match.getTeamBDefender().getName())
                .status(match.getStatus())
                .createdAt(match.getCreatedAt())
                .games(match.getGames().stream()
                        .map(g -> GameResponse.builder()
                                .gameNumber(g.getGameNumber())
                                .teamAScore(g.getTeamAScore())
                                .teamBScore(g.getTeamBScore())
                                .build())
                        .toList()) // Returns an unmodifiable list
                .build();
    }
}
