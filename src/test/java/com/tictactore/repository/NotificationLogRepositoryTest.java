package com.tictactore.repository;

import com.tictactore.model.NotificationLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("NotificationLogRepository Tests")
class NotificationLogRepositoryTest {

    @Autowired
    private NotificationLogRepository notificationLogRepository;

    @Test
    @DisplayName("Save NotificationLog - should persist and retrieve poolId")
    void saveNotificationLog_PersistsAndRetrievesPoolId() {
        var recipientId = UUID.randomUUID();
        var poolId = UUID.randomUUID();
        var logEntry = NotificationLog.builder()
                .recipientId(recipientId)
                .poolId(poolId)
                .type("POOL_CREATED")
                .payload("{\"poolId\":\"" + poolId + "\"}")
                .status("DELIVERED")
                .sentAt(Instant.now())
                .build();

        var saved = notificationLogRepository.save(logEntry);
        var reloaded = notificationLogRepository.findById(saved.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getPoolId()).isEqualTo(poolId);
        assertThat(reloaded.get().getType()).isEqualTo("POOL_CREATED");
        assertThat(reloaded.get().getStatus()).isEqualTo("DELIVERED");
    }
}
