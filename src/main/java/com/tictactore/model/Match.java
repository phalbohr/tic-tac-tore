package com.tictactore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @brief Root entity for a 2v2 Foosball Match record.
 *
 * @purpose
 * This entity stores the core data of a foosball match, including the participants, 
 * the games played (scores), and the current approval status. It serves as the primary 
 * record for ranking and history tracking.
 *
 * @usage
 * - Create a new {@code Match} when a user records a game result.
 * - Manage status transitions (DRAFT -> PENDING_APPROVAL -> CONFIRMED) through the lifecycle.
 * - Use {@link #addGame(Game)} to maintain the bidirectional relationship with games.
 *
 * @documentation
 * - See: conductor/product.md for match rules.
 *
 * @restrictions
 * - **DO NOT** modify a match once it is in {@code CONFIRMED} status.
 * - Participants (attacker/defender) must be unique across teams in a single match.
 *
 * @dependencies
 * - Governed by {@link MatchStatus} for lifecycle management.
 * - Contains a list of {@link Game} entities for scoring details.
 */
@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Version
    private long version;

    @NotNull(message = "Creator is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @NotNull(message = "Team A Attacker is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_attacker_id", nullable = false)
    private User teamAAttacker;

    @NotNull(message = "Team A Defender is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_defender_id", nullable = false)
    private User teamADefender;

    @NotNull(message = "Team B Attacker is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_attacker_id", nullable = false)
    private User teamBAttacker;

    @NotNull(message = "Team B Defender is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_defender_id", nullable = false)
    private User teamBDefender;

    @NotNull(message = "Match status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private MatchStatus status = MatchStatus.PENDING_APPROVAL;

    @Size(min = 1, max = 3, message = "Match must have between 1 and 3 games")
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Game> games = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @ToString.Include
    private LocalDateTime createdAt;

    /**
     * Helper method to maintain bidirectional relationship between Match and Game.
     * @param game The game to add to this match.
     */
    public void addGame(Game game) {
        games.add(game);
        game.setMatch(this);
    }
}
