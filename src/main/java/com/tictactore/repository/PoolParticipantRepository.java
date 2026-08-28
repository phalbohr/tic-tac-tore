package com.tictactore.repository;

import com.tictactore.model.PoolParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PoolParticipantRepository extends JpaRepository<PoolParticipant, UUID> {
}
