package com.tictactore.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "matchmaking_pool")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchmakingPool {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 20)
    private MatchType matchType;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_condition", nullable = false, length = 20)
    private StartCondition startCondition;

    @Column(name = "scheduled_time")
    private Instant scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", nullable = false, length = 20)
    @Builder.Default
    private SkillLevel skillLevel = SkillLevel.OPEN_FOR_ALL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PoolStatus status = PoolStatus.OPEN;

    @OneToMany(mappedBy = "pool", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PoolParticipant> participants = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    public void addParticipant(PoolParticipant participant) {
        int requiredPlayers = this.matchType == MatchType.ONE_VS_ONE ? 2 : 4;
        if (this.status != PoolStatus.OPEN || (this.participants != null && this.participants.size() >= requiredPlayers)) {
            throw new com.tictactore.exception.PoolConflictException("Pool is no longer open for joining");
        }
        if (this.participants != null && participant.getUser() != null && participant.getUser().getId() != null
                && this.participants.stream().anyMatch(p -> p.getUser() != null && participant.getUser().getId().equals(p.getUser().getId()))) {
            throw new com.tictactore.exception.PoolConflictException("User is already a participant in this pool");
        }
        if (this.participants == null) {
            this.participants = new ArrayList<>();
        }
        this.participants.add(participant);
        participant.setPool(this);
        if (this.participants.size() >= requiredPlayers) {
            this.status = PoolStatus.FILLED;
        }
    }
}
