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

    @Version
    private Long version;
}
