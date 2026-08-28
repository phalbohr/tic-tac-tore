package com.tictactore.repository;

import com.tictactore.model.MatchmakingPool;
import com.tictactore.model.PoolStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchmakingPoolRepository extends JpaRepository<MatchmakingPool, UUID> {

    long countByCreatorIdAndStatus(UUID creatorId, PoolStatus status);

    @EntityGraph(attributePaths = {"participants", "participants.user", "creator"})
    Optional<MatchmakingPool> findByIdAndStatus(UUID id, PoolStatus status);

    @EntityGraph(attributePaths = {"participants", "participants.user", "creator"})
    List<MatchmakingPool> findByStatusOrderByCreatedAtDesc(PoolStatus status);

    @Override
    @EntityGraph(attributePaths = {"participants", "participants.user", "creator"})
    Optional<MatchmakingPool> findById(UUID id);
}
