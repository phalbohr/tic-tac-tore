package com.tictactore.service.tournament;

import com.tictactore.model.RegistrationStatus;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.model.User;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRegistrationRepository;
import com.tictactore.repository.TournamentRepository;
import com.tictactore.service.tournament.impl.TournamentMatchQueryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentMatchQueryService Specifications")
class TournamentMatchQueryServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

    @Mock
    private TournamentMatchRepository tournamentMatchRepository;

    @Mock
    private TournamentRegistrationRepository registrationRepository;

    @InjectMocks
    private TournamentMatchQueryServiceImpl queryService;

    private UUID tournamentId;
    private Tournament tournament;
    private TournamentRegistration reg1;
    private TournamentRegistration reg2;
    private TournamentRegistration reg3;

    private UUID ruleConfigId;
    private String ruleConfigName;

    @BeforeEach
    void setUp() {
        tournamentId = UUID.randomUUID();
        ruleConfigId = UUID.randomUUID();
        ruleConfigName = "Official 3-Game Standard";

        var ruleConfiguration = com.tictactore.model.RuleConfiguration.builder()
                .id(ruleConfigId)
                .name(ruleConfigName)
                .gameLimit(3)
                .goalLimit(10)
                .build();

        tournament = Tournament.builder()
                .id(tournamentId)
                .name("Championship 2026")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .status(TournamentStatus.IN_PROGRESS)
                .ruleConfiguration(ruleConfiguration)
                .build();

        var u1 = User.builder().id(UUID.randomUUID()).nickname("Alice").build();
        var u2 = User.builder().id(UUID.randomUUID()).nickname("Bob").build();
        var u3 = User.builder().id(UUID.randomUUID()).nickname("Charlie").build();

        reg1 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(u1).status(RegistrationStatus.CONFIRMED).seed(1).build();
        reg2 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(u2).status(RegistrationStatus.CONFIRMED).seed(2).build();
        reg3 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(u3).status(RegistrationStatus.CONFIRMED).seed(3).build();
    }

    @Test
    void shouldReturnMatchesWithAvailabilityAndOpponentBusyStatus() {
        var activeMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.IN_PROGRESS)
                .build();

        var pendingMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .round(2)
                .matchOrder(1)
                .participant1(reg3)
                .participant2(reg2)
                .status(TournamentMatchStatus.READY)
                .build();

        var byeMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .round(1)
                .matchOrder(2)
                .participant1(reg3)
                .participant2(null)
                .isParticipant2Stub(true)
                .status(TournamentMatchStatus.BYE)
                .build();

        when(tournamentRepository.existsById(tournamentId)).thenReturn(true);
        when(tournamentMatchRepository.findByTournamentIdOrderByRoundAscMatchOrderAsc(tournamentId))
                .thenReturn(List.of(activeMatch, pendingMatch, byeMatch));
        when(tournamentMatchRepository.findByTournamentIdAndStatus(tournamentId, TournamentMatchStatus.IN_PROGRESS))
                .thenReturn(List.of(activeMatch));

        var response = queryService.getTournamentMatches(tournamentId, null);

        assertThat(response).hasSize(3);
        var activeDto = response.get(0);
        assertThat(activeDto.isAvailable()).isFalse();
        assertThat(activeDto.isOpponentBusy()).isFalse();

        var blockedDto = response.get(1);
        assertThat(blockedDto.isAvailable()).isFalse();
        assertThat(blockedDto.isOpponentBusy()).isTrue();
        assertThat(blockedDto.busyParticipantNicknames()).contains("Bob");

        var byeDto = response.get(2);
        assertThat(byeDto.isAvailable()).isFalse();
    }

    @Test
    void shouldReturnBracketWithSeededParticipantsAndRounds() {
        var matchRound1 = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.READY)
                .build();

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(tournamentMatchRepository.findByTournamentIdOrderByRoundAscMatchOrderAsc(tournamentId))
                .thenReturn(List.of(matchRound1));
        when(registrationRepository.findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED))
                .thenReturn(List.of(reg1, reg2, reg3));
        when(tournamentMatchRepository.findByTournamentIdAndStatus(tournamentId, TournamentMatchStatus.IN_PROGRESS))
                .thenReturn(List.of());

        var bracket = queryService.getTournamentBracket(tournamentId);

        assertThat(bracket).isNotNull();
        assertThat(bracket.tournamentId()).isEqualTo(tournamentId);
        assertThat(bracket.rounds()).hasSize(1);
        assertThat(bracket.rounds().get(0).matches().get(0).isAvailable()).isTrue();
        assertThat(bracket.seededParticipants()).hasSize(3);
    }

    @Test
    void shouldPopulateRuleConfigurationFieldsInMatchResponse() {
        var match = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .round(1)
                .matchOrder(1)
                .participant1(reg1)
                .participant2(reg2)
                .status(TournamentMatchStatus.READY)
                .build();

        when(tournamentRepository.existsById(tournamentId)).thenReturn(true);
        when(tournamentMatchRepository.findByTournamentIdOrderByRoundAscMatchOrderAsc(tournamentId))
                .thenReturn(List.of(match));
        when(tournamentMatchRepository.findByTournamentIdAndStatus(tournamentId, TournamentMatchStatus.IN_PROGRESS))
                .thenReturn(List.of());

        var response = queryService.getTournamentMatches(tournamentId, null);

        assertThat(response).hasSize(1);
        var matchResponse = response.get(0);
        assertThat(matchResponse.ruleConfigurationId()).isEqualTo(ruleConfigId);
        assertThat(matchResponse.ruleConfigurationName()).isEqualTo(ruleConfigName);
    }
}
