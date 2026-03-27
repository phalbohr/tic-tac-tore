package com.tictactore.repository;

import com.tictactore.model.Match;
import com.tictactore.model.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {

    @Query("SELECT DISTINCT m FROM Match m " +
           "JOIN FETCH m.creator " +
           "JOIN FETCH m.teamAAttacker " +
           "JOIN FETCH m.teamADefender " +
           "JOIN FETCH m.teamBAttacker " +
           "JOIN FETCH m.teamBDefender " +
           "LEFT JOIN FETCH m.games " +
           "WHERE m.status = :status AND (" +
           "  ((m.creator.id = m.teamAAttacker.id OR m.creator.id = m.teamADefender.id) AND (m.teamBAttacker.id = :userId OR m.teamBDefender.id = :userId)) OR " +
           "  ((m.creator.id = m.teamBAttacker.id OR m.creator.id = m.teamBDefender.id) AND (m.teamAAttacker.id = :userId OR m.teamADefender.id = :userId))" +
           ")")
    List<Match> findPendingApprovalsForUser(@Param("userId") UUID userId, @Param("status") MatchStatus status);

    @Query("SELECT m FROM Match m " +
           "JOIN FETCH m.teamAAttacker " +
           "JOIN FETCH m.teamADefender " +
           "JOIN FETCH m.teamBAttacker " +
           "JOIN FETCH m.teamBDefender " +
           "WHERE m.status = com.tictactore.model.MatchStatus.CONFIRMED AND (" +
           "  m.teamAAttacker.id = :userId OR m.teamADefender.id = :userId OR " +
           "  m.teamBAttacker.id = :userId OR m.teamBDefender.id = :userId" +
           ")")
    List<Match> findAllConfirmedMatchesForUser(@Param("userId") UUID userId);

    @Query(value = """
        WITH player_participation AS (
            SELECT team_a_attacker_id as player_id, (winner_team = 'TEAM_A') as is_win, 'ATTACKER' as role FROM matches WHERE status = 'CONFIRMED' AND created_at >= :since
            UNION ALL
            SELECT team_a_defender_id, (winner_team = 'TEAM_A'), 'DEFENDER' FROM matches WHERE status = 'CONFIRMED' AND created_at >= :since
            UNION ALL
            SELECT team_b_attacker_id, (winner_team = 'TEAM_B'), 'ATTACKER' FROM matches WHERE status = 'CONFIRMED' AND created_at >= :since
            UNION ALL
            SELECT team_b_defender_id, (winner_team = 'TEAM_B'), 'DEFENDER' FROM matches WHERE status = 'CONFIRMED' AND created_at >= :since
        ),
        player_stats AS (
            SELECT 
                p.player_id,
                COUNT(*) as total_matches,
                COUNT(CASE WHEN p.is_win THEN 1 END) as wins,
                COUNT(CASE WHEN NOT p.is_win THEN 1 END) as losses
            FROM player_participation p
            WHERE (:type = 'OVERALL' OR p.role = :type)
            GROUP BY p.player_id
            HAVING COUNT(*) >= :minMatches
        )
        SELECT 
            CAST(u.id AS VARCHAR) as playerId,
            u.name as playerName,
            ps.total_matches as totalMatches,
            ps.wins as wins,
            ps.losses as losses,
            CAST(ps.wins * 100.0 / ps.total_matches AS DOUBLE) as winRate
        FROM player_stats ps
        JOIN users u ON ps.player_id = u.id
        ORDER BY winRate DESC, totalMatches DESC
        """, 
        countQuery = """
        SELECT COUNT(*) FROM (
            SELECT p.player_id
            FROM (
                SELECT team_a_attacker_id as player_id, 'ATTACKER' as role FROM matches WHERE status = 'CONFIRMED' AND created_at >= :since
                UNION ALL
                SELECT team_a_defender_id, 'DEFENDER' FROM matches WHERE status = 'CONFIRMED' AND created_at >= :since
                UNION ALL
                SELECT team_b_attacker_id, 'ATTACKER' FROM matches WHERE status = 'CONFIRMED' AND created_at >= :since
                UNION ALL
                SELECT team_b_defender_id, 'DEFENDER' FROM matches WHERE status = 'CONFIRMED' AND created_at >= :since
            ) p
            WHERE (:type = 'OVERALL' OR p.role = :type)
            GROUP BY p.player_id
            HAVING COUNT(*) >= :minMatches
        )
        """,
        nativeQuery = true)
    Page<LeaderboardProjection> findLeaderboard(
            @Param("type") String type,
            @Param("since") LocalDateTime since,
            @Param("minMatches") Integer minMatches,
            Pageable pageable);

    @Query(value = """
        WITH match_info AS (
            SELECT 
                m.*,
                CASE 
                    WHEN m.team_a_attacker_id = :userId THEN 'ATTACKER'
                    WHEN m.team_a_defender_id = :userId THEN 'DEFENDER'
                    WHEN m.team_b_attacker_id = :userId THEN 'ATTACKER'
                    WHEN m.team_b_defender_id = :userId THEN 'DEFENDER'
                END as my_role,
                CASE
                    WHEN m.team_a_attacker_id = :userId OR m.team_a_defender_id = :userId THEN 'TEAM_A'
                    ELSE 'TEAM_B'
                END as my_team
            FROM matches m
            WHERE m.status = 'CONFIRMED' AND m.created_at >= :since 
              AND (m.team_a_attacker_id = :userId OR m.team_a_defender_id = :userId OR m.team_b_attacker_id = :userId OR m.team_b_defender_id = :userId)
        ),
        opponent_participation AS (
            SELECT m.id as match_id, m.my_role, (m.my_team = m.winner_team) as is_win, m.team_b_attacker_id as opponent_id, 'ATTACKER' as opponent_role FROM match_info m WHERE m.my_team = 'TEAM_A'
            UNION ALL
            SELECT m.id as match_id, m.my_role, (m.my_team = m.winner_team) as is_win, m.team_b_defender_id as opponent_id, 'DEFENDER' as opponent_role FROM match_info m WHERE m.my_team = 'TEAM_A'
            UNION ALL
            SELECT m.id as match_id, m.my_role, (m.my_team = m.winner_team) as is_win, m.team_a_attacker_id as opponent_id, 'ATTACKER' as opponent_role FROM match_info m WHERE m.my_team = 'TEAM_B'
            UNION ALL
            SELECT m.id as match_id, m.my_role, (m.my_team = m.winner_team) as is_win, m.team_a_defender_id as opponent_id, 'DEFENDER' as opponent_role FROM match_info m WHERE m.my_team = 'TEAM_B'
        ),
        filtered_pairs AS (
            SELECT * FROM opponent_participation
            WHERE (:myPosition = 'OVERALL' OR my_role = :myPosition)
              AND (:opponentPosition = 'OVERALL' OR opponent_role = :opponentPosition)
        ),
        h2h_stats AS (
            SELECT opponent_id, COUNT(*) as total_matches, COUNT(CASE WHEN is_win THEN 1 END) as wins, COUNT(CASE WHEN NOT is_win THEN 1 END) as losses FROM filtered_pairs GROUP BY opponent_id
        )
        SELECT CAST(u.id AS VARCHAR) as opponentId, u.name as opponentName, hs.total_matches as totalMatches, hs.wins as wins, hs.losses as losses, CAST(hs.wins * 100.0 / hs.total_matches AS DOUBLE) as winRate
        FROM h2h_stats hs JOIN users u ON hs.opponent_id = u.id 
        ORDER BY winRate DESC, totalMatches DESC, opponentName ASC
        """, 
        countQuery = """
        WITH match_info AS (
            SELECT 
                m.*,
                CASE 
                    WHEN m.team_a_attacker_id = :userId THEN 'ATTACKER'
                    WHEN m.team_a_defender_id = :userId THEN 'DEFENDER'
                    WHEN m.team_b_attacker_id = :userId THEN 'ATTACKER'
                    WHEN m.team_b_defender_id = :userId THEN 'DEFENDER'
                END as my_role,
                CASE
                    WHEN m.team_a_attacker_id = :userId OR m.team_a_defender_id = :userId THEN 'TEAM_A'
                    ELSE 'TEAM_B'
                END as my_team
            FROM matches m
            WHERE m.status = 'CONFIRMED' AND m.created_at >= :since 
              AND (m.team_a_attacker_id = :userId OR m.team_a_defender_id = :userId OR m.team_b_attacker_id = :userId OR m.team_b_defender_id = :userId)
        ),
        opponent_participation AS (
            SELECT m.my_role, m.team_b_attacker_id as opponent_id, 'ATTACKER' as opponent_role FROM match_info m WHERE m.my_team = 'TEAM_A'
            UNION ALL
            SELECT m.my_role, m.team_b_defender_id as opponent_id, 'DEFENDER' as opponent_role FROM match_info m WHERE m.my_team = 'TEAM_A'
            UNION ALL
            SELECT m.my_role, m.team_a_attacker_id as opponent_id, 'ATTACKER' as opponent_role FROM match_info m WHERE m.my_team = 'TEAM_B'
            UNION ALL
            SELECT m.my_role, m.team_a_defender_id as opponent_id, 'DEFENDER' as opponent_role FROM match_info m WHERE m.my_team = 'TEAM_B'
        )
        SELECT COUNT(DISTINCT opponent_id) FROM opponent_participation
        WHERE (:myPosition = 'OVERALL' OR my_role = :myPosition)
          AND (:opponentPosition = 'OVERALL' OR opponent_role = :opponentPosition)
        """,
        nativeQuery = true)
    Page<H2HProjection> findH2HStats(
            @Param("userId") UUID userId, 
            @Param("since") LocalDateTime since, 
            @Param("myPosition") String myPosition,
            @Param("opponentPosition") String opponentPosition,
            Pageable pageable);

    @Query(value = """
        WITH participation AS (
            SELECT 
                (winner_team = 'TEAM_A' AND (team_a_attacker_id = :userId OR team_a_defender_id = :userId)) OR (winner_team = 'TEAM_B' AND (team_b_attacker_id = :userId OR team_b_defender_id = :userId)) as is_win,
                (team_a_attacker_id = :userId OR team_b_attacker_id = :userId) as is_attacker,
                (team_a_defender_id = :userId OR team_b_defender_id = :userId) as is_defender
            FROM matches 
            WHERE status = 'CONFIRMED' AND created_at >= :since AND (team_a_attacker_id = :userId OR team_a_defender_id = :userId OR team_b_attacker_id = :userId OR team_b_defender_id = :userId)
        )
        SELECT 
            COUNT(*) as overallMatches,
            COUNT(CASE WHEN is_win THEN 1 END) as overallWins,
            COUNT(CASE WHEN is_attacker THEN 1 END) as attackerMatches,
            COUNT(CASE WHEN is_attacker AND is_win THEN 1 END) as attackerWins,
            COUNT(CASE WHEN is_defender THEN 1 END) as defenderMatches,
            COUNT(CASE WHEN is_defender AND is_win THEN 1 END) as defenderWins
        FROM participation
        """, nativeQuery = true)
    PlayerStatsProjection findPlayerStats(@Param("userId") UUID userId, @Param("since") LocalDateTime since);
}
