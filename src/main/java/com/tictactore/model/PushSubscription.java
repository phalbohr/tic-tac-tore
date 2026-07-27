package com.tictactore.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "push_subscription",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_push_sub_endpoint", columnNames = {"endpoint"})
    },
    indexes = {
        @Index(name = "idx_push_sub_user_id", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 1024)
    private String endpoint;

    @Column(nullable = false, length = 512)
    private String p256dh;

    @Column(nullable = false, length = 256)
    private String auth;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;
}
