package com.tictactore.repository;

import com.tictactore.model.RegistrationStatus;
import com.tictactore.model.TournamentRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentRegistrationRepository extends JpaRepository<TournamentRegistration, UUID> {

    @Query("SELECT r FROM TournamentRegistration r " +
           "LEFT JOIN FETCH r.player " +
           "LEFT JOIN FETCH r.partner " +
           "LEFT JOIN FETCH r.tournament " +
           "WHERE r.tournament.id = :tournamentId " +
           "ORDER BY r.createdAt ASC")
    List<TournamentRegistration> findByTournamentId(@Param("tournamentId") UUID tournamentId);

    @Query("SELECT r FROM TournamentRegistration r " +
           "LEFT JOIN FETCH r.player " +
           "LEFT JOIN FETCH r.partner " +
           "LEFT JOIN FETCH r.tournament " +
           "WHERE r.tournament.id = :tournamentId AND r.status = :status " +
           "ORDER BY r.createdAt ASC")
    List<TournamentRegistration> findByTournamentIdAndStatus(
            @Param("tournamentId") UUID tournamentId,
            @Param("status") RegistrationStatus status
    );

    long countByTournamentIdAndStatus(UUID tournamentId, RegistrationStatus status);

    @Query("SELECT r FROM TournamentRegistration r " +
           "LEFT JOIN FETCH r.player " +
           "LEFT JOIN FETCH r.partner " +
           "LEFT JOIN FETCH r.tournament " +
           "WHERE r.tournament.id = :tournamentId " +
           "AND (r.player.id = :userId OR r.partner.id = :userId) " +
           "AND r.status IN (:statuses)")
    Optional<TournamentRegistration> findActiveUserRegistration(
            @Param("tournamentId") UUID tournamentId,
            @Param("userId") UUID userId,
            @Param("statuses") Collection<RegistrationStatus> statuses
    );

    @Query("SELECT r FROM TournamentRegistration r " +
           "LEFT JOIN FETCH r.tournament " +
           "LEFT JOIN FETCH r.player " +
           "LEFT JOIN FETCH r.partner " +
           "WHERE r.partner.id = :userId AND r.status = 'PENDING_CONFIRMATION' " +
           "ORDER BY r.createdAt DESC")
    List<TournamentRegistration> findPendingInvitationsForUser(@Param("userId") UUID userId);

    @Query("SELECT r FROM TournamentRegistration r " +
           "LEFT JOIN FETCH r.tournament " +
           "LEFT JOIN FETCH r.player " +
           "LEFT JOIN FETCH r.partner " +
           "WHERE r.id = :id")
    Optional<TournamentRegistration> findByIdWithDetails(@Param("id") UUID id);
}
