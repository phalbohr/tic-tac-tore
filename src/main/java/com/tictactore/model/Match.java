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

    public void approve() {
        if (this.status != MatchStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Match can only be approved if it is in PENDING_APPROVAL status");
        }
        this.status = MatchStatus.CONFIRMED;
    }

    public void reject() {
        if (this.status != MatchStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Match can only be rejected if it is in PENDING_APPROVAL status");
        }
        this.status = MatchStatus.DRAFT;
    }

    public boolean isUserInTeamA(User user) {
        return user.getId().equals(this.teamAAttacker.getId()) ||
               user.getId().equals(this.teamADefender.getId());
    }

    public boolean isUserInTeamB(User user) {
        return user.getId().equals(this.teamBAttacker.getId()) ||
               user.getId().equals(this.teamBDefender.getId());
    }

    public void addGame(Game game) {
        games.add(game);
        game.setMatch(this);
    }
}
