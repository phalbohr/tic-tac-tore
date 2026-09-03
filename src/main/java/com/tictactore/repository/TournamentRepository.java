package com.tictactore.repository;

import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {

    List<Tournament> findByStatus(TournamentStatus status);

    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);

    List<Tournament> findByCreatorId(UUID creatorId);

    List<Tournament> findAllByOrderByCreatedAtDesc();

    Page<Tournament> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tournament t WHERE t.id = :id")
    Optional<Tournament> findByIdWithLock(@Param("id") UUID id);

    List<Tournament> findByStatusAndRegistrationDeadlineLessThanEqual(TournamentStatus status, java.time.Instant deadline);
}
