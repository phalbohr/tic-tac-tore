package com.tictactore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "rule_configuration")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RuleConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RuleConfigurationType type;

    @Column(name = "goal_limit", nullable = false)
    private int goalLimit;

    @Column(name = "game_limit", nullable = false)
    private int gameLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_format", nullable = false, length = 30, columnDefinition = "varchar(30) default 'BEST_OF_N'")
    private MatchFormat matchFormat;

    @Column(name = "games_to_win", nullable = false, columnDefinition = "integer default 3")
    private int gamesToWin;

    @Enumerated(EnumType.STRING)
    @Column(name = "win_by_two_rule", nullable = false, length = 30, columnDefinition = "varchar(30) default 'ALL_GAMES'")
    private WinByTwoRule winByTwoRule;

    @Column(name = "absolute_score_cap")
    private Integer absoluteScoreCap;

    @Column(name = "timeouts_per_game", nullable = false)
    private int timeoutsPerGame;

    @Column(name = "timeout_duration_seconds", nullable = false)
    private int timeoutDurationSeconds;

    @Column(name = "possession_limit_5bar_seconds", nullable = false)
    private int possessionLimit5BarSeconds;

    @Column(name = "possession_limit_other_seconds", nullable = false)
    private int possessionLimitOtherSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "side_swap_rule", nullable = false, length = 30)
    private SideSwapRule sideSwapRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "restart_rule", nullable = false, length = 30)
    private RestartRule restartRule;

    @Column(name = "spinning_allowed", nullable = false)
    private boolean spinningAllowed;

    @Column(name = "aerials_allowed", nullable = false)
    private boolean aerialsAllowed;

    @Enumerated(EnumType.STRING)
    @Column(name = "position_swap_rule", nullable = false, length = 30)
    private PositionSwapRule positionSwapRule;

    @Enumerated(EnumType.STRING)
    @Column(name = "point_distribution", nullable = false, length = 30)
    private PointDistribution pointDistribution;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Version
    private Long version;
}
