package com.tictactore.service.tournament;

import com.tictactore.dto.tournament.TournamentStandingResponse;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.User;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRegistrationRepository;
import com.tictactore.service.tournament.impl.TournamentStandingsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentStandingsService Tests")
class TournamentStandingsServiceTest {

    @Mock
    private TournamentRegistrationRepository registrationRepository;

    @Mock
    private TournamentMatchRepository tournamentMatchRepository;

    private TournamentStandingsService standingsService;

    private Tournament tournament;
    private TournamentRegistration reg1;
    private TournamentRegistration reg2;
    private TournamentRegistration reg3;
    private TournamentRegistration reg4;
    private TournamentRegistration stubReg;

    @BeforeEach
    void setUp() {
        standingsService = new TournamentStandingsServiceImpl(registrationRepository, tournamentMatchRepository);

        tournament = Tournament.builder()
                .id(UUID.randomUUID())
                .name("Test Cup")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS)
                .build();

        reg1 = createReg("Player1");
        reg2 = createReg("Player2");
        reg3 = createReg("Player3");
        reg4 = createReg("Player4");
        stubReg = createReg("StubPlayer");
    }

    @Test
    void shouldIsolateStubPartnerStatisticsFromSubstituteMatch() {
        TournamentMatch match = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .participant1(reg1)
                .participant1Partner(stubReg)
                .participant2(reg2)
                .participant2Partner(reg3)
                .isParticipant1Stub(true)
                .winner(reg1)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        when(registrationRepository.findByTournamentId(tournament.getId()))
                .thenReturn(List.of(reg1, reg2, reg3, stubReg));
        when(tournamentMatchRepository.findByTournamentIdAndStatusIn(tournament.getId(), List.of(TournamentMatchStatus.COMPLETED)))
                .thenReturn(List.of(match));

        List<TournamentStandingResponse> standings = standingsService.calculateStandings(tournament.getId());

        TournamentStandingResponse stubStanding = standings.stream()
                .filter(s -> s.registrationId().equals(stubReg.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(stubStanding.matchesPlayed()).isZero();
        assertThat(stubStanding.wins()).isZero();
        assertThat(stubStanding.points()).isZero();

        TournamentStandingResponse reg1Standing = standings.stream()
                .filter(s -> s.registrationId().equals(reg1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(reg1Standing.matchesPlayed()).isEqualTo(1);
        assertThat(reg1Standing.wins()).isEqualTo(1);
        assertThat(reg1Standing.points()).isEqualTo(3);
    }

    @Test
    void shouldGrantKnockoutImmunityToStubPartnerWhenSubstituteTeamLoses() {
        TournamentMatch match = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .participant1(reg1)
                .participant1Partner(stubReg)
                .participant2(reg2)
                .participant2Partner(reg3)
                .isParticipant1Stub(true)
                .winner(reg2)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        when(registrationRepository.findByTournamentId(tournament.getId()))
                .thenReturn(List.of(reg1, reg2, reg3, stubReg));
        when(tournamentMatchRepository.findByTournamentIdAndStatusIn(tournament.getId(), List.of(TournamentMatchStatus.COMPLETED)))
                .thenReturn(List.of(match));

        List<TournamentStandingResponse> standings = standingsService.calculateStandings(tournament.getId());

        TournamentStandingResponse stubStanding = standings.stream()
                .filter(s -> s.registrationId().equals(stubReg.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(stubStanding.isEliminated()).isFalse();

        TournamentStandingResponse reg1Standing = standings.stream()
                .filter(s -> s.registrationId().equals(reg1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(reg1Standing.isEliminated()).isTrue();
    }

    @Test
    void shouldReturnEmptyListWhenTournamentIdIsNull() {
        List<TournamentStandingResponse> standings = standingsService.calculateStandings(null);

        assertThat(standings).isEmpty();
    }

    private TournamentRegistration createReg(String nickname) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .nickname(nickname)
                .build();
        return TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .player(user)
                .build();
    }
}
