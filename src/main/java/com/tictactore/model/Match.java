package com.tictactore.model;

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
}
