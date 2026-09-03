package com.tictactore.service.tournament;

import com.tictactore.model.RegistrationStatus;
import com.tictactore.model.RuleConfiguration;
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
@DisplayName("TournamentMatchQueryService ATDD Unit Tests (Rule Association)")
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
    private UUID ruleConfigId;
    private String ruleConfigName;
    private Tournament tournament;
    private TournamentRegistration reg1;
    private TournamentRegistration reg2;

    @BeforeEach
    void setUp() {
        tournamentId = UUID.randomUUID();
        ruleConfigId = UUID.randomUUID();
        ruleConfigName = "Official 3-Game Standard";

        RuleConfiguration ruleConfiguration = RuleConfiguration.builder()
                .id(ruleConfigId)
                .name(ruleConfigName)
                .gameLimit(3)
                .scoreLimit(10)
                .build();

        tournament = Tournament.builder()
                .id(tournamentId)
                .name("Summer Championship 2026")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .status(TournamentStatus.IN_PROGRESS)
                .ruleConfiguration(ruleConfiguration)
                .build();

        User u1 = User.builder().id(UUID.randomUUID()).nickname("Alice").build();
        User u2 = User.builder().id(UUID.randomUUID()).nickname("Bob").build();

        reg1 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(u1).status(RegistrationStatus.CONFIRMED).seed(1).build();
        reg2 = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(u2).status(RegistrationStatus.CONFIRMED).seed(2).build();
    }

    @Test
    @DisplayName("Should populate ruleConfigurationId and ruleConfigurationName in TournamentMatchResponse")
    void shouldPopulateRuleConfigurationFieldsInMatchResponse() {
        TournamentMatch match = TournamentMatch.builder()
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
