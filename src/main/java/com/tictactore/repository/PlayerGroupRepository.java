package com.tictactore.repository;

import com.tictactore.model.PlayerGroup;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerGroupRepository extends JpaRepository<PlayerGroup, UUID> {

    @Override
    @EntityGraph(attributePaths = {"members"})
    Optional<PlayerGroup> findById(UUID id);

    @EntityGraph(attributePaths = {"members"})
    List<PlayerGroup> findByCreatorIdOrderByCreatedAtAsc(UUID creatorId);

    @EntityGraph(attributePaths = {"members"})
    Optional<PlayerGroup> findByIdAndCreatorId(UUID id, UUID creatorId);

    boolean existsByCreatorIdAndNameIgnoreCase(UUID creatorId, String name);

    boolean existsByCreatorIdAndNameIgnoreCaseAndIdNot(UUID creatorId, String name, UUID id);

    long countByCreatorId(UUID creatorId);

    boolean existsByCreatorIdAndIsFavoriteTrue(UUID creatorId);

    List<PlayerGroup> findByCreatorIdAndIsFavoriteTrue(UUID creatorId);

    @org.springframework.data.jpa.repository.Query("SELECT pg.id FROM PlayerGroup pg JOIN pg.members m WHERE m.id = :userId")
    List<UUID> findGroupIdsByMemberId(@org.springframework.data.repository.query.Param("userId") UUID userId);
}
