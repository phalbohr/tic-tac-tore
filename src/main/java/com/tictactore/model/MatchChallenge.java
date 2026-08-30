package com.tictactore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "match_challenge")
@org.hibernate.annotations.Check(constraints = "target_player_id IS NOT NULL OR target_group_id IS NOT NULL")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenger_id", nullable = false)
    private User challenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_player_id")
    private User targetPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_group_id")
    private PlayerGroup targetGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 20)
    private MatchType matchType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_config_id")
    private RuleConfiguration ruleConfig;

    @Column(name = "message", length = 255)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ChallengeStatus status = ChallengeStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Version
    private Long version;

    public void accept(UUID userId, Collection<UUID> userGroupIds) {
        validatePendingStatus("accept");
        validateTargetAuthorization(userId, userGroupIds, "accept");
        this.status = ChallengeStatus.ACCEPTED;
    }

    public void decline(UUID userId, Collection<UUID> userGroupIds) {
        validatePendingStatus("decline");
        validateTargetAuthorization(userId, userGroupIds, "decline");
        this.status = ChallengeStatus.DECLINED;
    }

    public void cancel(UUID userId) {
        validatePendingStatus("cancel");
        if (this.challenger == null || !this.challenger.getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Only challenger can cancel the challenge");
        }
        this.status = ChallengeStatus.CANCELLED;
    }

    private void validatePendingStatus(String action) {
        if (this.status != ChallengeStatus.PENDING) {
            throw new com.tictactore.exception.ChallengeConflictException("Cannot " + action + " challenge in status " + this.status);
        }
    }

    private void validateTargetAuthorization(UUID userId, Collection<UUID> userGroupIds, String action) {
        if (this.challenger != null && this.challenger.getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Challenger cannot " + action + " their own challenge");
        }
        boolean isDirectTarget = this.targetPlayer != null && this.targetPlayer.getId().equals(userId);
        boolean isGroupMember = this.targetGroup != null && userGroupIds != null && userGroupIds.contains(this.targetGroup.getId());

        if (!isDirectTarget && !isGroupMember) {
            throw new org.springframework.security.access.AccessDeniedException("User is not authorized to " + action + " this challenge");
        }
    }
}
