package com.tictactore.service.tournament;

import com.tictactore.dto.CreateMatchRequest;
import com.tictactore.dto.GameDto;
import com.tictactore.exception.TournamentConflictException;
import com.tictactore.exception.TournamentRuleMismatchException;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
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

@DisplayName("TournamentMatchValidator ATDD Unit Tests")
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
    @DisplayName("Should pass validation when tournament is IN_PROGRESS, ruleConfigId matches, and participants match")
    void shouldPassValidationWhenAllConstraintsAreMet() {
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
    @DisplayName("Should throw TournamentConflictException when tournament is not IN_PROGRESS")
    void shouldThrowWhenTournamentIsNotInProgress() {
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
    @DisplayName("Should throw TournamentRuleMismatchException when request ruleConfigId is null")
    void shouldThrowWhenRuleConfigIdIsNull() {
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
    @DisplayName("Should throw TournamentRuleMismatchException when request ruleConfigId does not match tournament ruleConfigId")
    void shouldThrowWhenRuleConfigIdMismatches() {
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
    @DisplayName("Should throw TournamentConflictException when participants do not match tournament match slot")
    void shouldThrowWhenParticipantsMismatch() {
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
}
