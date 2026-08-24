package com.tictactore.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "\"user\"")
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

    @Column(name = "tutorial_completed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean tutorialCompleted = false;

    @Column(name = "default_group_id")
    private UUID defaultGroupId;

    @Column(name = "default_rule_configuration_id")
    private UUID defaultRuleConfigurationId;

    @Version
    private Long version;
}
