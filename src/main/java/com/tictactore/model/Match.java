package com.tictactore.model;

import com.tictactore.exception.InvalidMatchStateException;
import com.tictactore.exception.UnauthorizedMatchActionException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "match")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "creator_id", nullable = false)
    private UUID creatorId;

    @Column(name = "team_a_attacker_id", nullable = false)
    private UUID teamAAttackerId;

    @Column(name = "team_a_defender_id")
    private UUID teamADefenderId;

    @Column(name = "team_b_attacker_id", nullable = false)
    private UUID teamBAttackerId;

    @Column(name = "team_b_defender_id")
    private UUID teamBDefenderId;

    @Column(nullable = false)
    private String status;

    @Column(name = "confirmed_by_user_id")
    private UUID confirmedByUserId;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Game> games = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    public void addGame(Game game) {
        games.add(game);
        game.setMatch(this);
    }

    public boolean isParticipant(UUID userId) {
        if (userId == null) return false;
        return userId.equals(teamAAttackerId) || userId.equals(teamADefenderId)
                || userId.equals(teamBAttackerId) || userId.equals(teamBDefenderId);
    }

    public boolean isOpponent(UUID userId) {
        if (userId == null || !isParticipant(userId) || userId.equals(creatorId)) {
            return false;
        }
        boolean creatorInTeamA = creatorId.equals(teamAAttackerId) || creatorId.equals(teamADefenderId);
        boolean creatorInTeamB = creatorId.equals(teamBAttackerId) || creatorId.equals(teamBDefenderId);

        if (creatorInTeamA) {
            return userId.equals(teamBAttackerId) || userId.equals(teamBDefenderId);
        } else if (creatorInTeamB) {
            return userId.equals(teamAAttackerId) || userId.equals(teamADefenderId);
        } else {
            return isParticipant(userId);
        }
    }

    public void confirmByOpponent(UUID opponentId) {
        if (!"PENDING_APPROVAL".equals(this.status)) {
            throw new InvalidMatchStateException("Match is not in PENDING_APPROVAL status");
        }
        if (this.creatorId.equals(opponentId) || !isOpponent(opponentId)) {
            throw new UnauthorizedMatchActionException("User " + opponentId + " is not an opponent for match " + this.id);
        }
        this.status = "CONFIRMED";
        this.confirmedByUserId = opponentId;
        this.confirmedAt = Instant.now();
    }
}
