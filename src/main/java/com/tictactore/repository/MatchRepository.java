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

    List<Match> findByStatus(String status);

    @Query("""
        SELECT m FROM Match m
        WHERE m.createdAt >= :startOfDay AND m.createdAt <= :endOfDay
        AND (
            (m.teamAAttackerId IN (:p1, :p2, :p3, :p4) OR m.teamADefenderId IN (:p1, :p2, :p3, :p4)
             OR m.teamBAttackerId IN (:p1, :p2, :p3, :p4) OR m.teamBDefenderId IN (:p1, :p2, :p3, :p4))
        )
        """)
    List<Match> findDuplicatesOnDate(
            @Param("startOfDay") Instant startOfDay,
            @Param("endOfDay") Instant endOfDay,
            @Param("p1") UUID p1,
            @Param("p2") UUID p2,
            @Param("p3") UUID p3,
            @Param("p4") UUID p4
    );
}
