package com.tictactore.repository;

import com.tictactore.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByMatchIdAndRecipientId(UUID matchId, UUID recipientId);

    List<NotificationLog> findByRecipientId(UUID recipientId);
}
