package com.tictactore.repository;

import com.tictactore.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    Optional<Match> findByIdempotencyKey(String idempotencyKey);

    @Query("""
        SELECT m FROM Match m
        WHERE m.createdAt >= :startOfDay AND m.createdAt <= :endOfDay
        AND (
            (m.teamAAttackerId = :p1 OR m.teamADefenderId = :p1 OR m.teamBAttackerId = :p1 OR m.teamBDefenderId = :p1)
            AND (m.teamAAttackerId = :p2 OR m.teamADefenderId = :p2 OR m.teamBAttackerId = :p2 OR m.teamBDefenderId = :p2)
        )
        """)
    List<Match> findDuplicatesOnDate(
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay,
            @Param("p1") UUID p1,
            @Param("p2") UUID p2
    );
}
