package com.tictactore.repository;

import com.tictactore.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for Match entities.
 */
@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {
}
