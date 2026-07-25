package com.tictactore.repository;

import com.tictactore.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    Optional<Match> findByIdempotencyKey(String idempotencyKey);
}
