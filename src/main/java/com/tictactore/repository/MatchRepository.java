package com.tictactore.repository;

import com.tictactore.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.tictactore.repository.projection.TeamPairStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository extends JpaRepository<Match, UUID> {
    Optional<Match> findByIdempotencyKey(String idempotencyKey);

    List<Match> findByStatus(String status);

    List<Match> findByStatusAndCreatorId(String status, UUID creatorId);

    List<Match> findByStatusIn(List<String> statuses);

    List<Match> findByStatusInAndCreatorId(List<String> statuses, UUID creatorId);

    List<Match> findByCooldownExpiresAtBeforeAndStatus(Instant expiresAt, String status);

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

    @Query("""
        SELECT DISTINCT m FROM Match m
        LEFT JOIN FETCH m.games g
        WHERE m.status IN ('CONFIRMED', 'PUBLISHED')
        AND (
            (m.teamAAttackerId = :playerId OR m.teamADefenderId = :playerId OR m.teamBAttackerId = :playerId OR m.teamBDefenderId = :playerId)
            AND
            (m.teamAAttackerId = :opponentId OR m.teamADefenderId = :opponentId OR m.teamBAttackerId = :opponentId OR m.teamBDefenderId = :opponentId)
        )
        AND (cast(:startDate as java.time.Instant) IS NULL OR m.createdAt >= :startDate)
        AND (cast(:matchType as string) IS NULL OR
            (:matchType = '1v1' AND m.teamADefenderId IS NULL AND m.teamBDefenderId IS NULL)
            OR (:matchType = '2v2' AND m.teamADefenderId IS NOT NULL AND m.teamBDefenderId IS NOT NULL)
        )
        ORDER BY m.createdAt DESC
        """)
    List<Match> findHeadToHeadMatches(
            @Param("playerId") UUID playerId,
            @Param("opponentId") UUID opponentId,
            @Param("startDate") Instant startDate,
            @Param("matchType") String matchType
    );

    @Query(value = """
        WITH match_results AS (
            SELECT
                m.id AS match_id,
                m.team_a_attacker_id AS a_attacker,
                m.team_a_defender_id AS a_defender,
                m.team_b_attacker_id AS b_attacker,
                m.team_b_defender_id AS b_defender,
                CASE
                    WHEN SUM(CASE WHEN g.team_a_score > g.team_b_score THEN 1 ELSE 0 END) >
                         SUM(CASE WHEN g.team_b_score > g.team_a_score THEN 1 ELSE 0 END) THEN 1
                    ELSE 0
                END AS team_a_won,
                CASE
                    WHEN SUM(CASE WHEN g.team_b_score > g.team_a_score THEN 1 ELSE 0 END) >
                         SUM(CASE WHEN g.team_a_score > g.team_b_score THEN 1 ELSE 0 END) THEN 1
                    ELSE 0
                END AS team_b_won
            FROM match m
            JOIN game g ON g.match_id = m.id
            WHERE m.status = 'CONFIRMED'
              AND m.team_a_defender_id IS NOT NULL
              AND m.team_b_defender_id IS NOT NULL
              AND (:startDate IS NULL OR m.created_at >= :startDate)
            GROUP BY m.id, m.created_at, m.team_a_attacker_id, m.team_a_defender_id, m.team_b_attacker_id, m.team_b_defender_id
        ),
        pair_matches AS (
            SELECT
                a_attacker AS attacker_id,
                a_defender AS defender_id,
                team_a_won AS is_win,
                team_b_won AS is_loss
            FROM match_results
            UNION ALL
            SELECT
                b_attacker AS attacker_id,
                b_defender AS defender_id,
                team_b_won AS is_win,
                team_a_won AS is_loss
            FROM match_results
        )
        SELECT
            CAST(attacker_id AS VARCHAR) AS attackerId,
            CAST(defender_id AS VARCHAR) AS defenderId,
            COUNT(*) AS matches,
            SUM(is_win) AS wins,
            SUM(is_loss) AS losses,
            ROUND((SUM(is_win) * 100.0) / COUNT(*), 2) AS winRate
        FROM pair_matches
        WHERE (:playerId IS NULL OR attacker_id = :playerId OR defender_id = :playerId)
        GROUP BY attacker_id, defender_id
        HAVING COUNT(*) >= :minMatches
        ORDER BY winRate DESC, matches DESC, wins DESC
        """,
        countQuery = """
        SELECT COUNT(*) FROM (
            WITH match_results AS (
                SELECT
                    m.id AS match_id,
                    m.team_a_attacker_id AS a_attacker,
                    m.team_a_defender_id AS a_defender,
                    m.team_b_attacker_id AS b_attacker,
                    m.team_b_defender_id AS b_defender
                FROM match m
                JOIN game g ON g.match_id = m.id
                WHERE m.status = 'CONFIRMED'
                  AND m.team_a_defender_id IS NOT NULL
                  AND m.team_b_defender_id IS NOT NULL
                  AND (:startDate IS NULL OR m.created_at >= :startDate)
                GROUP BY m.id, m.created_at, m.team_a_attacker_id, m.team_a_defender_id, m.team_b_attacker_id, m.team_b_defender_id
            ),
            pair_matches AS (
                SELECT a_attacker AS attacker_id, a_defender AS defender_id FROM match_results
                UNION ALL
                SELECT b_attacker AS attacker_id, b_defender AS defender_id FROM match_results
            )
            SELECT attacker_id, defender_id
            FROM pair_matches
            WHERE (:playerId IS NULL OR attacker_id = :playerId OR defender_id = :playerId)
            GROUP BY attacker_id, defender_id
            HAVING COUNT(*) >= :minMatches
        ) sub
        """,
        nativeQuery = true)
    Page<TeamPairStatsProjection> aggregateTeamPairStats(
            @Param("playerId") UUID playerId,
            @Param("startDate") Instant startDate,
            @Param("ruleConfigId") UUID ruleConfigId,
            @Param("minMatches") int minMatches,
            Pageable pageable
    );
}
