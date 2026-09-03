package com.tictactore.service.tournament;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.GameDto;
import com.tictactore.exception.TournamentConflictException;
import com.tictactore.exception.TournamentRuleMismatchException;
import com.tictactore.model.Match;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import com.tictactore.service.tournament.impl.TournamentMatchValidatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TournamentMatchValidator Unit Tests")
class TournamentMatchValidatorTest {

    private TournamentMatchValidator validator;

    private Tournament tournament;
    private RuleConfiguration ruleConfig;
    private TournamentRegistration reg1;
    private TournamentRegistration reg2;
    private TournamentMatch tournamentMatch;
    private UUID user1Id;
    private UUID user2Id;
    private UUID ruleConfigId;

    @BeforeEach
    void setUp() {
        validator = new TournamentMatchValidatorImpl();

        ruleConfigId = UUID.randomUUID();
        ruleConfig = RuleConfiguration.builder()
                .id(ruleConfigId)
                .name("Official Standard")
                .gameLimit(3)
                .goalLimit(10)
                .build();

        tournament = Tournament.builder()
                .id(UUID.randomUUID())
                .name("Summer Championship")
                .status(TournamentStatus.IN_PROGRESS)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .ruleConfiguration(ruleConfig)
                .build();

        user1Id = UUID.randomUUID();
        user2Id = UUID.randomUUID();

        var user1 = User.builder().id(user1Id).nickname("Alice").build();
        var user2 = User.builder().id(user2Id).nickname("Bob").build();

        reg1 = TournamentRegistration.builder().id(UUID.randomUUID()).player(user1).build();
        reg2 = TournamentRegistration.builder().id(UUID.randomUUID()).player(user2).build();

        tournamentMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.IN_PROGRESS)
                .build();
    }

    @Test
    void shouldPass_whenAllConstraintsAreMet() {
        var request = new CreateMatchRequest(
                "idemp-1",
                user1Id,
                user1Id,
                null,
                user2Id,
                null,
                List.of(new GameDto(10, 8, null, null, null, null)),
                "MANUAL",
                "1v1",
                tournamentMatch.getId(),
                ruleConfigId
        );

        assertThatCode(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldPass_whenTeamsAreSwappedSides() {
        var request = new CreateMatchRequest(
                "idemp-swapped",
                user2Id,
                user2Id,
                null,
                user1Id,
                null,
                List.of(new GameDto(10, 8, null, null, null, null)),
                "MANUAL",
                "1v1",
                tournamentMatch.getId(),
                ruleConfigId
        );

        assertThatCode(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrow_whenTournamentIsNotInProgress() {
        tournament.setStatus(TournamentStatus.COMPLETED);

        var request = new CreateMatchRequest(
                "idemp-2",
                user1Id,
                user1Id,
                null,
                user2Id,
                null,
                List.of(new GameDto(10, 8, null, null, null, null)),
                "MANUAL",
                "1v1",
                tournamentMatch.getId(),
                ruleConfigId
        );

        assertThatThrownBy(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("Tournament is not in progress");
    }

    @Test
    void shouldThrow_whenMatchAlreadyCompleted() {
        tournamentMatch.setStatus(TournamentMatchStatus.COMPLETED);

        var request = new CreateMatchRequest(
                "idemp-completed",
                user1Id,
                user1Id,
                null,
                user2Id,
                null,
                List.of(new GameDto(10, 8, null, null, null, null)),
                "MANUAL",
                "1v1",
                tournamentMatch.getId(),
                ruleConfigId
        );

        assertThatThrownBy(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("Tournament match has already been completed");
    }

    @Test
    void shouldThrow_whenMatchAlreadyHasLinkedMatchEntity() {
        tournamentMatch.setMatch(Match.builder().id(UUID.randomUUID()).build());

        var request = new CreateMatchRequest(
                "idemp-dup",
                user1Id,
                user1Id,
                null,
                user2Id,
                null,
                List.of(new GameDto(10, 8, null, null, null, null)),
                "MANUAL",
                "1v1",
                tournamentMatch.getId(),
                ruleConfigId
        );

        assertThatThrownBy(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("Tournament match has already been completed");
    }

    @Test
    void shouldThrow_whenMatchStatusIsNotPlayable() {
        tournamentMatch.setStatus(TournamentMatchStatus.CANCELLED);

        var request = new CreateMatchRequest(
                "idemp-cancelled",
                user1Id,
                user1Id,
                null,
                user2Id,
                null,
                List.of(new GameDto(10, 8, null, null, null, null)),
                "MANUAL",
                "1v1",
                tournamentMatch.getId(),
                ruleConfigId
        );

        assertThatThrownBy(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("Tournament match is not in a playable state");
    }

    @Test
    void shouldThrow_whenMode1v1Receives2v2Request() {
        tournament.setMode(TournamentMode.ONE_VS_ONE_PERSONAL);

        var request = new CreateMatchRequest(
                "idemp-mode-mismatch-1",
                user1Id,
                user1Id,
                UUID.randomUUID(),
                user2Id,
                UUID.randomUUID(),
                List.of(new GameDto(10, 8, null, null, null, null)),
                "MANUAL",
                "2v2",
                tournamentMatch.getId(),
                ruleConfigId
        );

        assertThatThrownBy(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("Match format does not match tournament mode: expected 1v1");
    }

    @Test
    void shouldThrow_whenMode2v2Receives1v1Request() {
        tournament.setMode(TournamentMode.TWO_VS_TWO_FIXED_TEAMS);

        var request = new CreateMatchRequest(
                "idemp-mode-mismatch-2",
                user1Id,
                user1Id,
                null,
                user2Id,
                null,
                List.of(new GameDto(10, 8, null, null, null, null)),
                "MANUAL",
                "1v1",
                tournamentMatch.getId(),
                ruleConfigId
        );

        assertThatThrownBy(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("Match format does not match tournament mode: expected 2v2");
    }

    @Test
    void shouldThrow_whenRuleConfigIdIsNull() {
        var request = new CreateMatchRequest(
                "idemp-3",
                user1Id,
                user1Id,
                null,
                user2Id,
                null,
                List.of(new GameDto(10, 8, null, null, null, null)),
                "MANUAL",
                "1v1",
                tournamentMatch.getId(),
                null
        );

        assertThatThrownBy(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .isInstanceOf(TournamentRuleMismatchException.class)
                .hasMessageContaining("Rule configuration ID is required for tournament matches");
    }

    @Test
    void shouldThrow_whenRuleConfigIdMismatches() {
        var wrongRuleConfigId = UUID.randomUUID();

        var request = new CreateMatchRequest(
                "idemp-4",
                user1Id,
                user1Id,
                null,
                user2Id,
                null,
                List.of(new GameDto(10, 8, null, null, null, null)),
                "MANUAL",
                "1v1",
                tournamentMatch.getId(),
                wrongRuleConfigId
        );

        assertThatThrownBy(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .isInstanceOf(TournamentRuleMismatchException.class)
                .hasMessageContaining("does not match tournament rule configuration");
    }

    @Test
    void shouldThrow_whenParticipantsMismatch() {
        var wrongUserId = UUID.randomUUID();

        var request = new CreateMatchRequest(
                "idemp-5",
                user1Id,
                user1Id,
                null,
                wrongUserId,
                null,
                List.of(new GameDto(10, 8, null, null, null, null)),
                "MANUAL",
                "1v1",
                tournamentMatch.getId(),
                ruleConfigId
        );

        assertThatThrownBy(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("Participants do not match assigned tournament match roster");
    }

    @Test
    void shouldThrow_when2v2TeamMembersAreMixedAcrossSides() {
        tournament.setMode(TournamentMode.TWO_VS_TWO_FIXED_TEAMS);
        var u1 = User.builder().id(UUID.randomUUID()).build();
        var u1Partner = User.builder().id(UUID.randomUUID()).build();
        var u2 = User.builder().id(UUID.randomUUID()).build();
        var u2Partner = User.builder().id(UUID.randomUUID()).build();

        var tReg1 = TournamentRegistration.builder().id(UUID.randomUUID()).player(u1).partner(u1Partner).build();
        var tReg2 = TournamentRegistration.builder().id(UUID.randomUUID()).player(u2).partner(u2Partner).build();

        tournamentMatch.setParticipant1(tReg1);
        tournamentMatch.setParticipant2(tReg2);

        // Mix Team 1 player with Team 2 player on Team A
        var request = new CreateMatchRequest(
                "idemp-mixed-sides",
                u1.getId(),
                u1.getId(),
                u2.getId(),
                u1Partner.getId(),
                u2Partner.getId(),
                List.of(new GameDto(10, 8, u1.getId(), u2.getId(), u1Partner.getId(), u2Partner.getId())),
                "MANUAL",
                "2v2",
                tournamentMatch.getId(),
                ruleConfigId
        );

        assertThatThrownBy(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .isInstanceOf(TournamentConflictException.class)
                .hasMessageContaining("Participants do not match assigned tournament match roster");
    }

    @Test
    void shouldPass_when2v2TeamRosterIsValid() {
        tournament.setMode(TournamentMode.TWO_VS_TWO_FIXED_TEAMS);
        var u1 = User.builder().id(UUID.randomUUID()).build();
        var u1Partner = User.builder().id(UUID.randomUUID()).build();
        var u2 = User.builder().id(UUID.randomUUID()).build();
        var u2Partner = User.builder().id(UUID.randomUUID()).build();

        var tReg1 = TournamentRegistration.builder().id(UUID.randomUUID()).player(u1).partner(u1Partner).build();
        var tReg2 = TournamentRegistration.builder().id(UUID.randomUUID()).player(u2).partner(u2Partner).build();

        tournamentMatch.setParticipant1(tReg1);
        tournamentMatch.setParticipant2(tReg2);

        var request = new CreateMatchRequest(
                "idemp-2v2-valid",
                u1.getId(),
                u1.getId(),
                u1Partner.getId(),
                u2.getId(),
                u2Partner.getId(),
                List.of(new GameDto(10, 8, u1.getId(), u1Partner.getId(), u2.getId(), u2Partner.getId())),
                "MANUAL",
                "2v2",
                tournamentMatch.getId(),
                ruleConfigId
        );

        assertThatCode(() -> validator.validateTournamentMatchCreation(tournamentMatch, request))
                .doesNotThrowAnyException();
    }
}
