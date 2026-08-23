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

    @EntityGraph(attributePaths = {"members"})
    List<PlayerGroup> findByCreatorIdOrderByCreatedAtAsc(UUID creatorId);

    @EntityGraph(attributePaths = {"members"})
    Optional<PlayerGroup> findByIdAndCreatorId(UUID id, UUID creatorId);

    boolean existsByCreatorIdAndNameIgnoreCase(UUID creatorId, String name);

    boolean existsByCreatorIdAndNameIgnoreCaseAndIdNot(UUID creatorId, String name, UUID id);
}
