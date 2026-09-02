package com.tictactore.repository;

import com.tictactore.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.tictactore.repository.projection.PlayerMatchStatsProjection;
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
        AND (cast(:ruleConfigId as java.util.UUID) IS NULL OR m.ruleConfigId = :ruleConfigId)
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
            @Param("ruleConfigId") UUID ruleConfigId,
            @Param("matchType") String matchType
    );

    @Query("""
        SELECT m FROM Match m
        WHERE (
            m.teamAAttackerId = :currentUserId OR m.teamADefenderId = :currentUserId
            OR m.teamBAttackerId = :currentUserId OR m.teamBDefenderId = :currentUserId
            OR m.creatorId = :currentUserId
        )
        AND (
            (:status = 'CONFIRMED' AND m.status IN ('CONFIRMED', 'PUBLISHED'))
            OR (:status = 'PENDING' AND m.status IN ('PENDING_APPROVAL', 'PARTIALLY_CONFIRMED'))
            OR (:status = 'ALL')
            OR (m.status = :status)
        )
        AND (
            cast(:filterPlayerId as java.util.UUID) IS NULL OR
            (m.teamAAttackerId = :filterPlayerId OR m.teamADefenderId = :filterPlayerId
             OR m.teamBAttackerId = :filterPlayerId OR m.teamBDefenderId = :filterPlayerId
             OR m.creatorId = :filterPlayerId)
        )
        AND (cast(:ruleConfigId as java.util.UUID) IS NULL OR m.ruleConfigId = :ruleConfigId)
        AND (cast(:matchType as string) IS NULL OR
            (:matchType = '1v1' AND m.teamADefenderId IS NULL AND m.teamBDefenderId IS NULL)
            OR (:matchType = '2v2' AND m.teamADefenderId IS NOT NULL AND m.teamBDefenderId IS NOT NULL)
        )
        ORDER BY m.createdAt DESC
        """)
    Page<Match> findMatchHistory(
            @Param("currentUserId") UUID currentUserId,
            @Param("status") String status,
            @Param("filterPlayerId") UUID filterPlayerId,
            @Param("ruleConfigId") UUID ruleConfigId,
            @Param("matchType") String matchType,
            Pageable pageable
    );

    @Query("""
        SELECT m FROM Match m
        WHERE (
            m.teamAAttackerId = :currentUserId OR m.teamADefenderId = :currentUserId
            OR m.teamBAttackerId = :currentUserId OR m.teamBDefenderId = :currentUserId
            OR m.creatorId = :currentUserId
        )
        AND (
            (:status = 'CONFIRMED' AND m.status IN ('CONFIRMED', 'PUBLISHED'))
            OR (:status = 'PENDING' AND m.status IN ('PENDING_APPROVAL', 'PARTIALLY_CONFIRMED'))
            OR (:status = 'ALL')
            OR (m.status = :status)
        )
        AND (
            cast(:filterPlayerId as java.util.UUID) IS NULL OR
            (m.teamAAttackerId = :filterPlayerId OR m.teamADefenderId = :filterPlayerId
             OR m.teamBAttackerId = :filterPlayerId OR m.teamBDefenderId = :filterPlayerId
             OR m.creatorId = :filterPlayerId)
        )
        AND (
            (m.teamAAttackerId IN (:groupMemberIds) OR m.teamADefenderId IN (:groupMemberIds)
             OR m.teamBAttackerId IN (:groupMemberIds) OR m.teamBDefenderId IN (:groupMemberIds)
             OR m.creatorId IN (:groupMemberIds))
        )
        AND (cast(:ruleConfigId as java.util.UUID) IS NULL OR m.ruleConfigId = :ruleConfigId)
        AND (cast(:matchType as string) IS NULL OR
            (:matchType = '1v1' AND m.teamADefenderId IS NULL AND m.teamBDefenderId IS NULL)
            OR (:matchType = '2v2' AND m.teamADefenderId IS NOT NULL AND m.teamBDefenderId IS NOT NULL)
        )
        ORDER BY m.createdAt DESC
        """)
    Page<Match> findMatchHistoryWithGroupMembers(
            @Param("currentUserId") UUID currentUserId,
            @Param("status") String status,
            @Param("filterPlayerId") UUID filterPlayerId,
            @Param("groupMemberIds") List<UUID> groupMemberIds,
            @Param("ruleConfigId") UUID ruleConfigId,
            @Param("matchType") String matchType,
            Pageable pageable
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

    @Query("""
        SELECT COUNT(m) FROM Match m
        WHERE m.status IN ('CONFIRMED', 'PUBLISHED')
        AND (m.teamAAttackerId = :userId OR m.teamADefenderId = :userId OR m.teamBAttackerId = :userId OR m.teamBDefenderId = :userId)
        """)
    long countConfirmedMatchesByPlayerId(@Param("userId") UUID userId);

    @Query("""
        SELECT COUNT(m) FROM Match m
        WHERE m.status IN ('CONFIRMED', 'PUBLISHED')
        AND (m.teamADefenderId = :userId OR m.teamBDefenderId = :userId)
        """)
    long countConfirmedMatchesAsDefender(@Param("userId") UUID userId);

    @Query("""
        SELECT COALESCE(SUM(
            CASE
                WHEN g.teamAAttackerId = :userId THEN g.teamAScore
                WHEN g.teamBAttackerId = :userId THEN g.teamBScore
                WHEN m.teamADefenderId IS NULL AND m.teamBDefenderId IS NULL AND m.teamAAttackerId = :userId THEN g.teamAScore
                WHEN m.teamADefenderId IS NULL AND m.teamBDefenderId IS NULL AND m.teamBAttackerId = :userId THEN g.teamBScore
                ELSE 0
            END
        ), 0)
        FROM Game g JOIN g.match m
        WHERE m.status IN ('CONFIRMED', 'PUBLISHED')
        AND (m.teamAAttackerId = :userId OR m.teamBAttackerId = :userId OR g.teamAAttackerId = :userId OR g.teamBAttackerId = :userId)
        """)
    Long sumGoalsAsAttacker(@Param("userId") UUID userId);

    @Query("""
        SELECT DISTINCT m FROM Match m
        LEFT JOIN FETCH m.games g
        WHERE m.status IN ('CONFIRMED', 'PUBLISHED')
        AND (m.teamAAttackerId = :userId OR m.teamADefenderId = :userId OR m.teamBAttackerId = :userId OR m.teamBDefenderId = :userId)
        """)
    List<Match> findConfirmedMatchesByPlayerId(@Param("userId") UUID userId);

    @Query(value = """
        SELECT
            COUNT(DISTINCT m.id) AS totalMatches,
            COUNT(DISTINCT CASE
                WHEN (m.team_a_attacker_id = :userId OR m.team_a_defender_id = :userId)
                     AND COALESCE(g_a.wins, 0) > COALESCE(g_b.wins, 0) THEN m.id
                WHEN (m.team_b_attacker_id = :userId OR m.team_b_defender_id = :userId)
                     AND COALESCE(g_b.wins, 0) > COALESCE(g_a.wins, 0) THEN m.id
            END) AS wins
        FROM match m
        LEFT JOIN (
            SELECT match_id, COUNT(*) AS wins FROM game WHERE team_a_score > team_b_score GROUP BY match_id
        ) g_a ON g_a.match_id = m.id
        LEFT JOIN (
            SELECT match_id, COUNT(*) AS wins FROM game WHERE team_b_score > team_a_score GROUP BY match_id
        ) g_b ON g_b.match_id = m.id
        WHERE m.status IN ('CONFIRMED', 'PUBLISHED')
          AND (m.team_a_attacker_id = :userId OR m.team_a_defender_id = :userId OR m.team_b_attacker_id = :userId OR m.team_b_defender_id = :userId)
        """, nativeQuery = true)
    PlayerMatchStatsProjection getPlayerMatchStats(@Param("userId") UUID userId);
}
