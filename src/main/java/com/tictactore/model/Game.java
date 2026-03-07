package com.tictactore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Game number is required")
    @Min(value = 1, message = "Game number must be at least 1")
    @Max(value = 3, message = "Game number must be at most 3")
    @Column(name = "game_number", nullable = false)
    @ToString.Include
    private Integer gameNumber;

    @NotNull(message = "Team A score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @Column(name = "team_a_score", nullable = false)
    @ToString.Include
    private Integer teamAScore = 0;

    @NotNull(message = "Team B score is required")
    @Min(value = 0, message = "Score cannot be negative")
    @Column(name = "team_b_score", nullable = false)
    @ToString.Include
    private Integer teamBScore = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;
}
