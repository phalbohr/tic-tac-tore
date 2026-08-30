package com.tictactore.model;

import com.tictactore.exception.ChallengeConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MatchChallenge Domain Model Tests")
class MatchChallengeTest {

    private UUID challengerId;
    private User challenger;
    private UUID targetPlayerId;
    private User targetPlayer;
    private UUID targetGroupId;
    private PlayerGroup targetGroup;

    @BeforeEach
    void setUp() {
        challengerId = UUID.randomUUID();
        challenger = User.builder().id(challengerId).nickname("Challenger").build();

        targetPlayerId = UUID.randomUUID();
        targetPlayer = User.builder().id(targetPlayerId).nickname("Target").build();

        targetGroupId = UUID.randomUUID();
        targetGroup = PlayerGroup.builder().id(targetGroupId).name("Team Alpha").build();
    }

    @Test
    void shouldAcceptChallenge_whenDirectTargetUser() {
        var challenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetPlayer(targetPlayer)
                .status(ChallengeStatus.PENDING)
                .build();

        challenge.accept(targetPlayerId, List.of());

        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.ACCEPTED);
    }

    @Test
    void shouldAcceptChallenge_whenTargetGroupMember() {
        var groupMemberId = UUID.randomUUID();
        var challenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetGroup(targetGroup)
                .status(ChallengeStatus.PENDING)
                .build();

        challenge.accept(groupMemberId, List.of(targetGroupId));

        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.ACCEPTED);
    }

    @Test
    void shouldThrowAccessDenied_whenChallengerAttemptsToAcceptOwnGroupChallenge() {
        var challenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetGroup(targetGroup)
                .status(ChallengeStatus.PENDING)
                .build();

        assertThatThrownBy(() -> challenge.accept(challengerId, List.of(targetGroupId)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Challenger cannot accept their own challenge");
    }

    @Test
    void shouldThrowAccessDenied_whenChallengerAttemptsToDeclineOwnGroupChallenge() {
        var challenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetGroup(targetGroup)
                .status(ChallengeStatus.PENDING)
                .build();

        assertThatThrownBy(() -> challenge.decline(challengerId, List.of(targetGroupId)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Challenger cannot decline their own challenge");
    }

    @Test
    void shouldThrowAccessDenied_whenNonTargetUserAttemptsToAccept() {
        var strangerId = UUID.randomUUID();
        var challenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetPlayer(targetPlayer)
                .status(ChallengeStatus.PENDING)
                .build();

        assertThatThrownBy(() -> challenge.accept(strangerId, List.of()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User is not authorized to accept this challenge");
    }

    @Test
    void shouldThrowConflict_whenAcceptingNonPendingChallenge() {
        var challenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetPlayer(targetPlayer)
                .status(ChallengeStatus.ACCEPTED)
                .build();

        assertThatThrownBy(() -> challenge.accept(targetPlayerId, List.of()))
                .isInstanceOf(ChallengeConflictException.class)
                .hasMessageContaining("Cannot accept challenge in status ACCEPTED");
    }

    @Test
    void shouldDeclineChallenge_whenDirectTargetUser() {
        var challenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetPlayer(targetPlayer)
                .status(ChallengeStatus.PENDING)
                .build();

        challenge.decline(targetPlayerId, List.of());

        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.DECLINED);
    }

    @Test
    void shouldCancelChallenge_whenChallenger() {
        var challenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetPlayer(targetPlayer)
                .status(ChallengeStatus.PENDING)
                .build();

        challenge.cancel(challengerId);

        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.CANCELLED);
    }

    @Test
    void shouldThrowAccessDenied_whenNonChallengerAttemptsToCancel() {
        var challenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetPlayer(targetPlayer)
                .status(ChallengeStatus.PENDING)
                .build();

        assertThatThrownBy(() -> challenge.cancel(targetPlayerId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only challenger can cancel the challenge");
    }
}
