package com.tictactore.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "game")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @Column(name = "game_order", nullable = false)
    private int gameOrder;

    @Column(name = "team_a_score", nullable = false)
    private int teamAScore;

    @Column(name = "team_b_score", nullable = false)
    private int teamBScore;

    @Column(name = "team_a_attacker_id")
    private UUID teamAAttackerId;

    @Column(name = "team_a_defender_id")
    private UUID teamADefenderId;

    @Column(name = "team_b_attacker_id")
    private UUID teamBAttackerId;

    @Column(name = "team_b_defender_id")
    private UUID teamBDefenderId;

    @Version
    private Long version;

    public Position getPositionForPlayer(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        if (playerId.equals(teamAAttackerId) || playerId.equals(teamBAttackerId)) {
            return Position.ATTACKER;
        }
        if (playerId.equals(teamADefenderId) || playerId.equals(teamBDefenderId)) {
            return Position.DEFENDER;
        }
        return null;
    }
}
