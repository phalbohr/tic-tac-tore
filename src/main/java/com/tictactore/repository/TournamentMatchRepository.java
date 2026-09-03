package com.tictactore.repository;

import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, UUID> {

    @EntityGraph(attributePaths = {
            "tournament",
            "participant1",
            "participant1.player",
            "participant1.partner",
            "participant1Partner",
            "participant1Partner.player",
            "participant2",
            "participant2.player",
            "participant2.partner",
            "participant2Partner",
            "participant2Partner.player",
            "winner",
            "nextMatch"
    })
    List<TournamentMatch> findByTournamentIdOrderByRoundAscMatchOrderAsc(UUID tournamentId);

    @EntityGraph(attributePaths = {
            "tournament",
            "participant1",
            "participant1.player",
            "participant1.partner",
            "participant1Partner",
            "participant1Partner.player",
            "participant2",
            "participant2.player",
            "participant2.partner",
            "participant2Partner",
            "participant2Partner.player",
            "winner",
            "nextMatch"
    })
    List<TournamentMatch> findByTournamentIdAndRoundOrderByMatchOrderAsc(UUID tournamentId, int round);

    List<TournamentMatch> findByTournamentIdAndStatus(UUID tournamentId, TournamentMatchStatus status);

    List<TournamentMatch> findByTournamentIdAndStatusIn(UUID tournamentId, Collection<TournamentMatchStatus> statuses);

    Optional<TournamentMatch> findByMatchId(UUID matchId);

    @Query("SELECT tm FROM TournamentMatch tm " +
           "WHERE tm.tournament.id = :tournamentId " +
           "AND tm.status = :status " +
           "AND (tm.participant1.id IN :regIds " +
           "OR tm.participant2.id IN :regIds " +
           "OR tm.participant1Partner.id IN :regIds " +
           "OR tm.participant2Partner.id IN :regIds)")
    List<TournamentMatch> findActiveMatchesForParticipants(
            @Param("tournamentId") UUID tournamentId,
            @Param("status") TournamentMatchStatus status,
            @Param("regIds") Collection<UUID> regIds
    );

    @Query("SELECT tm FROM TournamentMatch tm " +
           "WHERE tm.tournament.id = :tournamentId " +
           "AND (tm.participant1.id = :regId OR tm.participant2.id = :regId)")
    List<TournamentMatch> findByParticipantRegistrationId(
            @Param("tournamentId") UUID tournamentId,
            @Param("regId") UUID regId
    );

    @Query("SELECT tm FROM TournamentMatch tm " +
           "WHERE tm.tournament.id = :tournamentId " +
           "AND (tm.participant1.id = :regId OR tm.participant2.id = :regId " +
           "OR tm.participant1Partner.id = :regId OR tm.participant2Partner.id = :regId)")
    List<TournamentMatch> findByAnyParticipantRegistrationId(
            @Param("tournamentId") UUID tournamentId,
            @Param("regId") UUID regId
    );
}
