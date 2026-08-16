package com.tictactore.repository;

import com.tictactore.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LeaderboardRepository extends JpaRepository<Match, UUID> {

    @Query("""
        SELECT m FROM Match m
        WHERE m.status = 'CONFIRMED'
        AND (cast(:matchFormat as string) IS NULL OR m.matchFormat = :matchFormat)
        AND (cast(:matchType as string) IS NULL OR
            (:matchType = '1v1' AND m.teamADefenderId IS NULL AND m.teamBDefenderId IS NULL)
            OR (:matchType = '2v2' AND m.teamADefenderId IS NOT NULL AND m.teamBDefenderId IS NOT NULL)
        )
        AND (cast(:startDate as java.time.Instant) IS NULL OR m.createdAt >= :startDate)
        AND (cast(:endDate as java.time.Instant) IS NULL OR m.createdAt <= :endDate)
        """)
    List<Match> findConfirmedMatchesWithFilters(
            @Param("matchFormat") String matchFormat,
            @Param("matchType") String matchType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}
