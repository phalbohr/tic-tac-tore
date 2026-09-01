package com.tictactore.repository;

import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, UUID> {

    List<TournamentMatch> findByTournamentIdOrderByRoundAscMatchOrderAsc(UUID tournamentId);

    List<TournamentMatch> findByTournamentIdAndRoundOrderByMatchOrderAsc(UUID tournamentId, int round);

    List<TournamentMatch> findByTournamentIdAndStatus(UUID tournamentId, TournamentMatchStatus status);

    @Query("SELECT tm FROM TournamentMatch tm " +
           "WHERE tm.tournament.id = :tournamentId " +
           "AND (tm.participant1.id = :regId OR tm.participant2.id = :regId)")
    List<TournamentMatch> findByParticipantRegistrationId(
            @Param("tournamentId") UUID tournamentId,
            @Param("regId") UUID regId
    );
}
