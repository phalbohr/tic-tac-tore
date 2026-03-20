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
}
