package com.tictactore.model;

import com.tictactore.exception.InvalidMatchStateException;
import com.tictactore.exception.UnauthorizedMatchActionException;
import com.tictactore.rules.VerificationRules;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Column(name = "confirmed_by_user_id")
    private UUID confirmedByUserId;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "rejected_by_user_id")
    private UUID rejectedByUserId;

    @Column(name = "rejected_at")
    private Instant rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "entry_mode")
    private String entryMode;

    @Column(name = "match_format")
    private String matchFormat;

    @Column(name = "confirmed_by_opponent_ids")
    private String confirmedByOpponentIds;

    @Column(name = "rule_config_id")
    private UUID ruleConfigId;

    @Column(name = "cooldown_expires_at")
    private Instant cooldownExpiresAt;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Game> games = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    public static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    public static final String STATUS_PARTIALLY_CONFIRMED = "PARTIALLY_CONFIRMED";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_REJECTED = "REJECTED";

    public static final String ENTRY_MODE_PARTICIPANT = "PARTICIPANT";
    public static final String ENTRY_MODE_REFEREE = "REFEREE";

    public static final String MATCH_FORMAT_STANDARD = "STANDARD";
    public static final String MATCH_FORMAT_RANDOM = "RANDOM";

    public void addGame(Game game) {
        games.add(game);
        game.setMatch(this);
    }

    public boolean isParticipant(UUID userId) {
        if (userId == null) return false;
        return userId.equals(teamAAttackerId) || userId.equals(teamADefenderId)
                || userId.equals(teamBAttackerId) || userId.equals(teamBDefenderId);
    }

    public boolean isOpponent(UUID userId) {
        if (userId == null || !isParticipant(userId) || userId.equals(creatorId)) {
            return false;
        }
        boolean creatorInTeamA = creatorId.equals(teamAAttackerId) || creatorId.equals(teamADefenderId);
        boolean creatorInTeamB = creatorId.equals(teamBAttackerId) || creatorId.equals(teamBDefenderId);

        if (creatorInTeamA) {
            return userId.equals(teamBAttackerId) || userId.equals(teamBDefenderId);
        } else if (creatorInTeamB) {
            return userId.equals(teamAAttackerId) || userId.equals(teamADefenderId);
        } else {
            return isParticipant(userId);
        }
    }

    public void confirmByOpponent(UUID opponentId) {
        if (!STATUS_PENDING_APPROVAL.equals(this.status)
                && !STATUS_PARTIALLY_CONFIRMED.equals(this.status)) {
            throw new InvalidMatchStateException("Match is not in PENDING_APPROVAL or PARTIALLY_CONFIRMED status");
        }
        if (java.util.Objects.equals(this.creatorId, opponentId) || !isOpponent(opponentId)) {
            throw new UnauthorizedMatchActionException("User " + opponentId + " is not an opponent for match " + this.id);
        }
        if (hasConfirmed(opponentId)) {
            return;
        }
        addConfirmation(opponentId);
        if (VerificationRules.isFullyConfirmed(this)) {
            this.status = STATUS_CONFIRMED;
            this.confirmedByUserId = opponentId;
            this.confirmedAt = Instant.now();
            this.cooldownExpiresAt = null;
        } else if (VerificationRules.supportsPartialConfirmation(this)) {
            this.status = STATUS_PARTIALLY_CONFIRMED;
            if (VerificationRules.requiresCooldown(this)) {
                this.cooldownExpiresAt = Instant.now().plusSeconds(24 * 60 * 60);
            }
        }
    }

    public void rejectByOpponent(UUID opponentId, String reason, String customReason) {
        if (!STATUS_PENDING_APPROVAL.equals(this.status)
                && !STATUS_PARTIALLY_CONFIRMED.equals(this.status)) {
            throw new InvalidMatchStateException("Match is not in PENDING_APPROVAL or PARTIALLY_CONFIRMED status");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidMatchStateException("Rejection reason is required");
        }
        if (java.util.Objects.equals(this.creatorId, opponentId) || !isOpponent(opponentId)) {
            throw new UnauthorizedMatchActionException("User " + opponentId + " is not an opponent for match " + this.id);
        }
        String finalReason = reason.trim();
        if (customReason != null && !customReason.trim().isEmpty()) {
            finalReason = finalReason + ": " + customReason.trim();
        }
        this.status = STATUS_REJECTED;
        this.rejectedByUserId = opponentId;
        this.rejectedAt = Instant.now();
        this.rejectionReason = finalReason;
        this.cooldownExpiresAt = null;
    }

    public boolean isInCooldown() {
        return cooldownExpiresAt != null && cooldownExpiresAt.isAfter(Instant.now());
    }

    public boolean isCooldownExpired() {
        return cooldownExpiresAt != null && !cooldownExpiresAt.isAfter(Instant.now());
    }

    public void publishAfterCooldown() {
        if (!STATUS_PARTIALLY_CONFIRMED.equals(this.status)) {
            throw new InvalidMatchStateException("Match is not in PARTIALLY_CONFIRMED status");
        }
        if (!isCooldownExpired()) {
            throw new InvalidMatchStateException("Cooldown has not expired yet");
        }
        this.status = STATUS_CONFIRMED;
        this.confirmedAt = Instant.now();
        this.cooldownExpiresAt = null;
    }

    public boolean hasConfirmed(UUID userId) {
        if (userId == null) return false;
        if (confirmedByOpponentIds != null && !confirmedByOpponentIds.isBlank()) {
            return Arrays.stream(confirmedByOpponentIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(UUID::fromString)
                    .anyMatch(id -> id.equals(userId));
        }
        return userId.equals(this.confirmedByUserId);
    }

    public List<UUID> getConfirmedByOpponentIdsList() {
        if (confirmedByOpponentIds == null || confirmedByOpponentIds.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.stream(confirmedByOpponentIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(UUID::fromString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public int getConfirmedByOpponentCount() {
        return getConfirmedByOpponentIdsList().size();
    }

    public void addConfirmation(UUID opponentId) {
        if (confirmedByOpponentIds == null || confirmedByOpponentIds.isBlank()) {
            confirmedByOpponentIds = opponentId.toString();
        } else if (!hasConfirmed(opponentId)) {
            confirmedByOpponentIds = confirmedByOpponentIds + "," + opponentId.toString();
        }
    }

    public List<UUID> getOpponentIds() {
        List<UUID> opponents = new ArrayList<>();
        boolean creatorInTeamA = creatorId != null && (creatorId.equals(teamAAttackerId) || creatorId.equals(teamADefenderId));
        boolean creatorInTeamB = creatorId != null && (creatorId.equals(teamBAttackerId) || creatorId.equals(teamBDefenderId));

        if (creatorInTeamA) {
            if (teamBAttackerId != null) opponents.add(teamBAttackerId);
            if (teamBDefenderId != null) opponents.add(teamBDefenderId);
        } else if (creatorInTeamB) {
            if (teamAAttackerId != null) opponents.add(teamAAttackerId);
            if (teamADefenderId != null) opponents.add(teamADefenderId);
        } else {
            if (teamAAttackerId != null) opponents.add(teamAAttackerId);
            if (teamADefenderId != null) opponents.add(teamADefenderId);
            if (teamBAttackerId != null) opponents.add(teamBAttackerId);
            if (teamBDefenderId != null) opponents.add(teamBDefenderId);
        }
        return opponents;
    }
}
