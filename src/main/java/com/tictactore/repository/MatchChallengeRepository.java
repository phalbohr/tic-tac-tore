package com.tictactore.repository;

import com.tictactore.model.ChallengeStatus;
import com.tictactore.model.MatchChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchChallengeRepository extends JpaRepository<MatchChallenge, UUID> {

    @Query("SELECT mc FROM MatchChallenge mc " +
           "LEFT JOIN FETCH mc.challenger " +
           "LEFT JOIN FETCH mc.targetPlayer " +
           "LEFT JOIN FETCH mc.targetGroup " +
           "LEFT JOIN FETCH mc.ruleConfig " +
           "WHERE mc.status = :status AND (" +
           "  mc.targetPlayer.id = :userId OR " +
           "  (:hasGroups = true AND mc.targetGroup.id IN :groupIds)" +
           ") ORDER BY mc.createdAt DESC")
    List<MatchChallenge> findIncomingChallengesInternal(
            @Param("userId") UUID userId,
            @Param("groupIds") Collection<UUID> groupIds,
            @Param("hasGroups") boolean hasGroups,
            @Param("status") ChallengeStatus status
    );

    default List<MatchChallenge> findIncomingChallenges(UUID userId, Collection<UUID> groupIds, ChallengeStatus status) {
        boolean hasGroups = groupIds != null && !groupIds.isEmpty();
        Collection<UUID> safeGroupIds = hasGroups ? groupIds : List.of(UUID.randomUUID());
        return findIncomingChallengesInternal(userId, safeGroupIds, hasGroups, status);
    }

    @Query("SELECT mc FROM MatchChallenge mc " +
           "LEFT JOIN FETCH mc.challenger " +
           "LEFT JOIN FETCH mc.targetPlayer " +
           "LEFT JOIN FETCH mc.targetGroup " +
           "LEFT JOIN FETCH mc.ruleConfig " +
           "WHERE mc.challenger.id = :challengerId AND mc.status = :status " +
           "ORDER BY mc.createdAt DESC")
    List<MatchChallenge> findByChallengerIdAndStatus(@Param("challengerId") UUID challengerId, @Param("status") ChallengeStatus status);

    boolean existsByChallengerIdAndTargetPlayerIdAndStatus(UUID challengerId, UUID targetPlayerId, ChallengeStatus status);

    boolean existsByChallengerIdAndTargetGroupIdAndStatus(UUID challengerId, UUID targetGroupId, ChallengeStatus status);

    @Query("SELECT mc FROM MatchChallenge mc " +
           "LEFT JOIN FETCH mc.challenger " +
           "LEFT JOIN FETCH mc.targetPlayer " +
           "LEFT JOIN FETCH mc.targetGroup " +
           "LEFT JOIN FETCH mc.ruleConfig " +
           "WHERE mc.id = :id")
    Optional<MatchChallenge> findByIdWithDetails(@Param("id") UUID id);
}
