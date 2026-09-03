package com.tictactore.service.tournament;

import com.tictactore.dto.tournament.TournamentStandingResponse;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
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
import com.tictactore.service.tournament.impl.TournamentStandingsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Disabled("ATDD red phase: Story 8.7 - Standings service calculation enhancement")
@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentStandingsServiceImpl Unit Tests")
class TournamentStandingsServiceTest {

    @Mock
    private TournamentRegistrationRepository registrationRepository;

    @Mock
    private TournamentMatchRepository tournamentMatchRepository;

    @InjectMocks
    private TournamentStandingsServiceImpl standingsService;

    private UUID tournamentId;
    private Tournament tournament;
    private User aliceUser;
    private User bobUser;
    private User charlieUser;
    private TournamentRegistration regAlice;
    private TournamentRegistration regBob;
    private TournamentRegistration regCharlie;

    @BeforeEach
    void setUp() {
        tournamentId = UUID.randomUUID();
        tournament = Tournament.builder()
                .id(tournamentId)
                .title("Championship 2026")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.ONE_VS_ONE)
                .status(TournamentStatus.IN_PROGRESS)
                .build();

        aliceUser = User.builder().id(UUID.randomUUID()).nickname("Alice").avatarUrl("alice.png").build();
        bobUser = User.builder().id(UUID.randomUUID()).nickname("Bob").avatarUrl("bob.png").build();
        charlieUser = User.builder().id(UUID.randomUUID()).nickname("Charlie").avatarUrl("charlie.png").build();

        regAlice = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(aliceUser).build();
        regBob = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(bobUser).build();
        regCharlie = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(charlieUser).build();
    }

    @Test
    @DisplayName("Should calculate points, game difference, and rank participants using multi-tier tie-breakers")
    void shouldCalculatePointsAndRankWithMultiTierTieBreakers() {
        when(registrationRepository.findByTournamentId(tournamentId)).thenReturn(List.of(regAlice, regBob, regCharlie));

        var match1Game1 = Game.builder().scoreTeamA(10).scoreTeamB(5).build();
        var match1Game2 = Game.builder().scoreTeamA(10).scoreTeamB(8).build();
        var match1Core = Match.builder().id(UUID.randomUUID()).games(List.of(match1Game1, match1Game2)).build();

        var tm1 = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .participant1(regAlice)
                .participant2(regBob)
                .winner(regAlice)
                .match(match1Core)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        var match2Game1 = Game.builder().scoreTeamA(10).scoreTeamB(4).build();
        var match2Game2 = Game.builder().scoreTeamA(6).scoreTeamB(10).build();
        var match2Game3 = Game.builder().scoreTeamA(10).scoreTeamB(7).build();
        var match2Core = Match.builder().id(UUID.randomUUID()).games(List.of(match2Game1, match2Game2, match2Game3)).build();

        var tm2 = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .participant1(regBob)
                .participant2(regCharlie)
                .winner(regBob)
                .match(match2Core)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        when(tournamentMatchRepository.findByTournamentIdAndStatusIn(tournamentId, List.of(TournamentMatchStatus.COMPLETED)))
                .thenReturn(List.of(tm1, tm2));

        var standings = standingsService.calculateStandings(tournamentId);

        assertThat(standings).hasSize(3);

        var first = standings.get(0);
        assertThat(first.registrationId()).isEqualTo(regAlice.getId());
        assertThat(first.nickname()).isEqualTo("Alice");
        assertThat(first.matchesPlayed()).isEqualTo(1);
        assertThat(first.wins()).isEqualTo(1);
        assertThat(first.losses()).isEqualTo(0);
        assertThat(first.gamesWon()).isEqualTo(2);
        assertThat(first.gamesLost()).isEqualTo(0);
        assertThat(first.gameDifference()).isEqualTo(2);
        assertThat(first.points()).isEqualTo(3);
        assertThat(first.rank()).isEqualTo(1);

        var second = standings.get(1);
        assertThat(second.registrationId()).isEqualTo(regBob.getId());
        assertThat(second.nickname()).isEqualTo("Bob");
        assertThat(second.matchesPlayed()).isEqualTo(2);
        assertThat(second.wins()).isEqualTo(1);
        assertThat(second.losses()).isEqualTo(1);
        assertThat(second.points()).isEqualTo(3);
        assertThat(second.rank()).isEqualTo(2);

        var third = standings.get(2);
        assertThat(third.registrationId()).isEqualTo(regCharlie.getId());
        assertThat(third.nickname()).isEqualTo("Charlie");
        assertThat(third.matchesPlayed()).isEqualTo(1);
        assertThat(third.wins()).isEqualTo(0);
        assertThat(third.losses()).isEqualTo(1);
        assertThat(third.points()).isEqualTo(0);
        assertThat(third.rank()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should exclude stub substitute match points and stats in 2v2 random pairing mode")
    void shouldExcludeStubPartnerSubstituteMatchesFromStubPlayerStandings() {
        var randomPairingTournament = Tournament.builder()
                .id(tournamentId)
                .title("Random 2v2")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS)
                .status(TournamentStatus.IN_PROGRESS)
                .build();

        var daveUser = User.builder().id(UUID.randomUUID()).nickname("Dave").avatarUrl("dave.png").build();
        var regDave = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(randomPairingTournament).player(daveUser).build();

        when(registrationRepository.findByTournamentId(tournamentId)).thenReturn(List.of(regAlice, regBob, regCharlie, regDave));

        var tm = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(randomPairingTournament)
                .participant1(regAlice)
                .participant1Partner(regDave)
                .isParticipant1Stub(true)
                .participant2(regBob)
                .participant2Partner(regCharlie)
                .isParticipant2Stub(false)
                .winner(regAlice)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        when(tournamentMatchRepository.findByTournamentIdAndStatusIn(tournamentId, List.of(TournamentMatchStatus.COMPLETED)))
                .thenReturn(List.of(tm));

        var standings = standingsService.calculateStandings(tournamentId);

        var aliceStanding = standings.stream().filter(s -> s.registrationId().equals(regAlice.getId())).findFirst().orElseThrow();
        var daveStanding = standings.stream().filter(s -> s.registrationId().equals(regDave.getId())).findFirst().orElseThrow();

        assertThat(aliceStanding.points()).isEqualTo(3);
        assertThat(aliceStanding.matchesPlayed()).isEqualTo(1);
        assertThat(daveStanding.points()).isEqualTo(0);
        assertThat(daveStanding.matchesPlayed()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should flag eliminated participants in CUP knockout tournaments")
    void shouldFlagEliminatedParticipantsInKnockoutFormat() {
        var cupTournament = Tournament.builder()
                .id(tournamentId)
                .title("Summer Cup")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE)
                .status(TournamentStatus.IN_PROGRESS)
                .build();

        when(registrationRepository.findByTournamentId(tournamentId)).thenReturn(List.of(regAlice, regBob));

        var tm = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(cupTournament)
                .participant1(regAlice)
                .participant2(regBob)
                .winner(regAlice)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        when(tournamentMatchRepository.findByTournamentIdAndStatusIn(tournamentId, List.of(TournamentMatchStatus.COMPLETED)))
                .thenReturn(List.of(tm));

        var standings = standingsService.calculateStandings(tournamentId);

        var aliceStanding = standings.stream().filter(s -> s.registrationId().equals(regAlice.getId())).findFirst().orElseThrow();
        var bobStanding = standings.stream().filter(s -> s.registrationId().equals(regBob.getId())).findFirst().orElseThrow();

        assertThat(aliceStanding.isEliminated()).isFalse();
        assertThat(bobStanding.isEliminated()).isTrue();
    }

    @Test
    @DisplayName("Should gracefully handle GDPR deleted users by returning Anonymous and default placeholder")
    void shouldHandleGdprDeletedUsersGracefully() {
        var regDeleted = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .player(null)
                .build();

        when(registrationRepository.findByTournamentId(tournamentId)).thenReturn(List.of(regDeleted));
        when(tournamentMatchRepository.findByTournamentIdAndStatusIn(tournamentId, List.of(TournamentMatchStatus.COMPLETED)))
                .thenReturn(List.of());

        var standings = standingsService.calculateStandings(tournamentId);

        assertThat(standings).hasSize(1);
        var standing = standings.get(0);
        assertThat(standing.nickname()).isEqualTo("Anonymous");
        assertThat(standing.avatarUrl()).isNull();
    }
}
