package com.tictactore.rules;

import com.tictactore.model.Match;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationRules Unit Tests")
class VerificationRulesTest {

    private UUID p1, p2, p3, p4;

    @BeforeEach
    void setUp() {
        p1 = UUID.randomUUID();
        p2 = UUID.randomUUID();
        p3 = UUID.randomUUID();
        p4 = UUID.randomUUID();
    }

    @Nested
    @DisplayName("getRequiredConfirmations")
    class GetRequiredConfirmations {

        @Test
        @DisplayName("[P0] 1v1 participant-entered requires 1 confirmation")
        void shouldReturn1ForSinglesParticipant() {
            Match match = Match.builder()
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            assertThat(VerificationRules.getRequiredConfirmations(match)).isEqualTo(1);
        }

        @Test
        @DisplayName("[P0] 1v1 referee-entered requires 2 confirmations")
        void shouldReturn2ForSinglesReferee() {
            Match match = Match.builder()
                    .creatorId(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            assertThat(VerificationRules.getRequiredConfirmations(match)).isEqualTo(2);
        }

        @Test
        @DisplayName("[P0] 2v2 standard requires 2 confirmations")
        void shouldReturn2ForDoublesStandard() {
            Match match = Match.builder()
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            assertThat(VerificationRules.getRequiredConfirmations(match)).isEqualTo(2);
        }

        @Test
        @DisplayName("[P0] 2v2 random requires 2 confirmations")
        void shouldReturn2ForDoublesRandom() {
            Match match = Match.builder()
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_RANDOM)
                    .build();

            assertThat(VerificationRules.getRequiredConfirmations(match)).isEqualTo(2);
        }

        @Test
        @DisplayName("[P1] null match returns 1")
        void shouldReturn1ForNullMatch() {
            assertThat(VerificationRules.getRequiredConfirmations(null)).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("supportsPartialConfirmation")
    class SupportsPartialConfirmation {

        @Test
        @DisplayName("[P0] 2v2 standard participant-entered supports partial")
        void shouldReturnTrueForDoublesStandardParticipant() {
            Match match = Match.builder()
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            assertThat(VerificationRules.supportsPartialConfirmation(match)).isTrue();
        }

        @Test
        @DisplayName("[P0] 2v2 random participant-entered supports partial confirmation for individual verification")
        void shouldReturnTrueForDoublesRandomParticipant() {
            Match match = Match.builder()
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_RANDOM)
                    .build();

            assertThat(VerificationRules.supportsPartialConfirmation(match)).isTrue();
        }

        @Test
        @DisplayName("[P0] 1v1 always does NOT support partial")
        void shouldReturnFalseForSingles() {
            Match match1v1 = Match.builder()
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            assertThat(VerificationRules.supportsPartialConfirmation(match1v1)).isFalse();
        }

        @Test
        @DisplayName("[P0] 2v2 referee-entered does NOT support partial")
        void shouldReturnFalseForDoublesReferee() {
            Match match = Match.builder()
                    .creatorId(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            assertThat(VerificationRules.supportsPartialConfirmation(match)).isFalse();
        }

        @Test
        @DisplayName("[P1] null match returns false")
        void shouldReturnFalseForNullMatch() {
            assertThat(VerificationRules.supportsPartialConfirmation(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("requiresCooldown")
    class RequiresCooldown {

        @Test
        @DisplayName("[P0] 2v2 standard participant-entered requires cooldown")
        void shouldReturnTrueForDoublesStandardParticipant() {
            Match match = Match.builder()
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            assertThat(VerificationRules.requiresCooldown(match)).isTrue();
        }

        @Test
        @DisplayName("[P0] 2v2 random participant-entered does NOT require cooldown")
        void shouldReturnFalseForDoublesRandomParticipant() {
            Match match = Match.builder()
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_RANDOM)
                    .build();

            assertThat(VerificationRules.requiresCooldown(match)).isFalse();
        }

        @Test
        @DisplayName("[P0] 1v1 always does NOT require cooldown")
        void shouldReturnFalseForSingles() {
            Match match1v1 = Match.builder()
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamBAttackerId(p2)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            assertThat(VerificationRules.requiresCooldown(match1v1)).isFalse();
        }

        @Test
        @DisplayName("[P0] 2v2 referee-entered does NOT require cooldown")
        void shouldReturnFalseForDoublesReferee() {
            Match match = Match.builder()
                    .creatorId(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();

            assertThat(VerificationRules.requiresCooldown(match)).isFalse();
        }

        @Test
        @DisplayName("[P1] null match returns false")
        void shouldReturnFalseForNullMatch() {
            assertThat(VerificationRules.requiresCooldown(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isFullyConfirmed")
    class IsFullyConfirmed {

        @Test
        @DisplayName("[P0] CONFIRMED status is fully confirmed")
        void shouldReturnTrueWhenStatusConfirmed() {
            Match match = Match.builder()
                    .status(Match.STATUS_CONFIRMED)
                    .build();

            assertThat(VerificationRules.isFullyConfirmed(match)).isTrue();
        }

        @Test
        @DisplayName("[P0] PENDING_APPROVAL status is NOT fully confirmed")
        void shouldReturnFalseWhenStatusPending() {
            Match match = Match.builder()
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .build();

            assertThat(VerificationRules.isFullyConfirmed(match)).isFalse();
        }

        @Test
        @DisplayName("[P0] PARTIALLY_CONFIRMED status is NOT fully confirmed")
        void shouldReturnFalseWhenPartiallyConfirmed() {
            Match match = Match.builder()
                    .status(Match.STATUS_PARTIALLY_CONFIRMED)
                    .build();

            assertThat(VerificationRules.isFullyConfirmed(match)).isFalse();
        }

        @Test
        @DisplayName("[P0] 1v1 participant with 1 confirmation is fully confirmed")
        void shouldReturnTrueForSinglesParticipantWithOneConfirmation() {
            Match match = Match.builder()
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();
            match.addConfirmation(p2);

            assertThat(VerificationRules.isFullyConfirmed(match)).isTrue();
        }

        @Test
        @DisplayName("[P0] 1v1 referee with 2 confirmations is fully confirmed")
        void shouldReturnTrueForSinglesRefereeWithTwoConfirmations() {
            Match match = Match.builder()
                    .creatorId(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(null)
                    .teamBAttackerId(p2)
                    .teamBDefenderId(null)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();
            match.addConfirmation(p1);
            match.addConfirmation(p2);

            assertThat(VerificationRules.isFullyConfirmed(match)).isTrue();
        }

        @Test
        @DisplayName("[P0] 2v2 with 2 opponent confirmations is fully confirmed")
        void shouldReturnTrueForDoublesWithTwoConfirmations() {
            Match match = Match.builder()
                    .creatorId(p1)
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status(Match.STATUS_PARTIALLY_CONFIRMED)
                    .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();
            match.addConfirmation(p3);
            match.addConfirmation(p4);

            assertThat(VerificationRules.isFullyConfirmed(match)).isTrue();
        }

        @Test
        @DisplayName("[P0] 2v2 referee with 1 per team is fully confirmed")
        void shouldReturnTrueForDoublesRefereeWithOnePerTeam() {
            Match match = Match.builder()
                    .creatorId(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();
            match.addConfirmation(p1);
            match.addConfirmation(p3);

            assertThat(VerificationRules.isFullyConfirmed(match)).isTrue();
        }

        @Test
        @DisplayName("[P1] 2v2 referee with both from same team is NOT fully confirmed")
        void shouldReturnFalseWhenRefereeDoublesBothFromSameTeam() {
            Match match = Match.builder()
                    .creatorId(UUID.randomUUID())
                    .teamAAttackerId(p1)
                    .teamADefenderId(p2)
                    .teamBAttackerId(p3)
                    .teamBDefenderId(p4)
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .entryMode(Match.ENTRY_MODE_REFEREE)
                    .matchFormat(Match.MATCH_FORMAT_STANDARD)
                    .build();
            match.addConfirmation(p1);
            match.addConfirmation(p2);

            assertThat(VerificationRules.isFullyConfirmed(match)).isFalse();
        }

        @Test
        @DisplayName("[P1] null match returns false")
        void shouldReturnFalseForNullMatch() {
            assertThat(VerificationRules.isFullyConfirmed(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("isPartiallyConfirmed")
    class IsPartiallyConfirmed {

        @Test
        @DisplayName("[P0] PARTIALLY_CONFIRMED status is partially confirmed")
        void shouldReturnTrueWhenStatusPartiallyConfirmed() {
            Match match = Match.builder()
                    .status(Match.STATUS_PARTIALLY_CONFIRMED)
                    .build();

            assertThat(VerificationRules.isPartiallyConfirmed(match)).isTrue();
        }

        @Test
        @DisplayName("[P0] PENDING_APPROVAL status is NOT partially confirmed")
        void shouldReturnFalseWhenStatusPending() {
            Match match = Match.builder()
                    .status(Match.STATUS_PENDING_APPROVAL)
                    .build();

            assertThat(VerificationRules.isPartiallyConfirmed(match)).isFalse();
        }

        @Test
        @DisplayName("[P1] null match returns false")
        void shouldReturnFalseForNullMatch() {
            assertThat(VerificationRules.isPartiallyConfirmed(null)).isFalse();
        }
    }
}
