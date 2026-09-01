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
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tournament_registration")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TournamentRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private User player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private User partner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RegistrationStatus status;

    @Column(name = "seed")
    private Integer seed;

    @Column(name = "strength_score")
    private Double strengthScore;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    public void accept(UUID userId) {
        if (this.status != RegistrationStatus.PENDING_CONFIRMATION) {
            throw new com.tictactore.exception.TournamentConflictException("Cannot accept invitation in status " + this.status);
        }
        if (this.partner == null || !this.partner.getId().equals(userId)) {
            throw new AccessDeniedException("Only the invited partner can accept this invitation");
        }
        this.status = RegistrationStatus.CONFIRMED;
    }

    public void decline(UUID userId) {
        if (this.status != RegistrationStatus.PENDING_CONFIRMATION) {
            throw new com.tictactore.exception.TournamentConflictException("Cannot decline invitation in status " + this.status);
        }
        if (this.partner == null || !this.partner.getId().equals(userId)) {
            throw new AccessDeniedException("Only the invited partner can decline this invitation");
        }
        this.status = RegistrationStatus.DECLINED;
    }

    public void cancel(UUID userId) {
        boolean isPlayer = this.player != null && this.player.getId().equals(userId);
        boolean isPartner = this.partner != null && this.partner.getId().equals(userId);
        if (!isPlayer && !isPartner) {
            throw new AccessDeniedException("Only registered participants can cancel registration");
        }
        this.status = RegistrationStatus.CANCELLED;
    }
}
