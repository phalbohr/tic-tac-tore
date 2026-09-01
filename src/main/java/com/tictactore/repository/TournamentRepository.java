package com.tictactore.repository;

import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, UUID> {

    List<Tournament> findByStatus(TournamentStatus status);

    List<Tournament> findByCreatorId(UUID creatorId);

    List<Tournament> findAllByOrderByCreatedAtDesc();
}
