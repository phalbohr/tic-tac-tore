package com.tictactore.repository;

import com.tictactore.model.Match;
import com.tictactore.model.MatchStatus;
import com.tictactore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Match entities.
 */
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
           "((m.teamBAttacker = :user OR m.teamBDefender = :user) AND (m.creator = m.teamAAttacker OR m.creator = m.teamADefender)) OR " +
           "((m.teamAAttacker = :user OR m.teamADefender = :user) AND (m.creator = m.teamBAttacker OR m.creator = m.teamBDefender))" +
           ")")
    List<Match> findPendingApprovalsForUser(@Param("user") User user, @Param("status") MatchStatus status);
}
