package com.tictactore.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(name = "provider_id")
    private String providerId;

    private String avatar;
    private String language;

    @Column(name = "last_nickname_update", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private java.time.Instant lastNicknameUpdate;

    @Version
    private Long version;
}
