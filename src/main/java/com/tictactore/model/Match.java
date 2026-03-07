package com.tictactore.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Root entity for a 2v2 Foosball Match record.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_attacker_id", nullable = false)
    private User teamAAttacker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_a_defender_id", nullable = false)
    private User teamADefender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_attacker_id", nullable = false)
    private User teamBAttacker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_b_defender_id", nullable = false)
    private User teamBDefender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ToString.Include
    private MatchStatus status = MatchStatus.DRAFT;

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
