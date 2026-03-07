package com.tictactore.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Record for incoming Match creation requests.
 */
public record MatchRequest(
    @NotNull(message = "Team A Attacker ID is required")
    UUID teamAAttackerId,

    @NotNull(message = "Team A Defender ID is required")
    UUID teamADefenderId,

    @NotNull(message = "Team B Attacker ID is required")
    UUID teamBAttackerId,

    @NotNull(message = "Team B Defender ID is required")
    UUID teamBDefenderId,

    @NotNull(message = "Games list is required")
    @Size(min = 1, max = 3, message = "Match must have between 1 and 3 games")
    @Valid
    List<GameRequest> games
) {
    @AssertTrue(message = "All players in a 2v2 match must be unique")
    @JsonIgnore
    public boolean isPlayersUnique() {
        if (teamAAttackerId == null || teamADefenderId == null || 
            teamBAttackerId == null || teamBDefenderId == null) {
            return true;
        }
        Set<UUID> players = new HashSet<>();
        players.add(teamAAttackerId);
        players.add(teamADefenderId);
        players.add(teamBAttackerId);
        players.add(teamBDefenderId);
        return players.size() == 4;
    }
}
