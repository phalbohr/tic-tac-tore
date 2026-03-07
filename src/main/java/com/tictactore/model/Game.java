package com.tictactore.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity representing a single game (set) within a match.
 * Using onlyExplicitlyIncluded to safely handle JPA relationships in Lombok methods.
 */
@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(name = "game_number", nullable = false)
    @ToString.Include
    private Integer gameNumber;

    @Column(name = "team_a_score", nullable = false)
    @ToString.Include
    private Integer teamAScore = 0;

    @Column(name = "team_b_score", nullable = false)
    @ToString.Include
    private Integer teamBScore = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;
}
