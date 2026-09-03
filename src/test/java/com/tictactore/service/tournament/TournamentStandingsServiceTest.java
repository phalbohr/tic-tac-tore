package com.tictactore.service.tournament;

import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
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
import com.tictactore.service.tournament.impl.TournamentStandingsServiceImpl;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TournamentStandingsServiceImpl Unit Tests")
class TournamentStandingsServiceTest {

    @Mock
    private TournamentRepository tournamentRepository;

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
                .name("Championship 2026")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .status(TournamentStatus.IN_PROGRESS)
                .build();

        aliceUser = User.builder().id(UUID.randomUUID()).nickname("Alice").avatar("alice.png").build();
        bobUser = User.builder().id(UUID.randomUUID()).nickname("Bob").avatar("bob.png").build();
        charlieUser = User.builder().id(UUID.randomUUID()).nickname("Charlie").avatar("charlie.png").build();

        regAlice = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(aliceUser).status(RegistrationStatus.CONFIRMED).build();
        regBob = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(bobUser).status(RegistrationStatus.CONFIRMED).build();
        regCharlie = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(tournament).player(charlieUser).status(RegistrationStatus.CONFIRMED).build();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when tournament does not exist")
    void shouldThrowResourceNotFoundException_whenTournamentDoesNotExist() {
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> standingsService.calculateStandings(tournamentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tournament");
    }

    @Test
    @DisplayName("Should calculate points, game difference, and rank participants using multi-tier tie-breakers")
    void shouldCalculatePointsAndRankWithMultiTierTieBreakers() {
        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(registrationRepository.findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED))
                .thenReturn(List.of(regAlice, regBob, regCharlie));

        var match1Game1 = Game.builder().teamAScore(10).teamBScore(5).build();
        var match1Game2 = Game.builder().teamAScore(10).teamBScore(8).build();
        var match1Core = Match.builder()
                .id(UUID.randomUUID())
                .teamAAttackerId(aliceUser.getId())
                .teamBAttackerId(bobUser.getId())
                .games(List.of(match1Game1, match1Game2))
                .build();

        var tm1 = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .participant1(regAlice)
                .participant2(regBob)
                .winner(regAlice)
                .match(match1Core)
                .status(TournamentMatchStatus.COMPLETED)
                .round(1)
                .build();

        var match2Game1 = Game.builder().teamAScore(10).teamBScore(4).build();
        var match2Game2 = Game.builder().teamAScore(6).teamBScore(10).build();
        var match2Game3 = Game.builder().teamAScore(10).teamBScore(7).build();
        var match2Core = Match.builder()
                .id(UUID.randomUUID())
                .teamAAttackerId(bobUser.getId())
                .teamBAttackerId(charlieUser.getId())
                .games(List.of(match2Game1, match2Game2, match2Game3))
                .build();

        var tm2 = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .participant1(regBob)
                .participant2(regCharlie)
                .winner(regBob)
                .match(match2Core)
                .status(TournamentMatchStatus.COMPLETED)
                .round(2)
                .build();

        when(tournamentMatchRepository.findByTournamentId(tournamentId))
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
    @DisplayName("Should rank CUP knockout participants by deepest round reached, then match wins")
    void shouldRankCupParticipantsByDeepestRoundReachedAndMatchWins() {
        var cupTournament = Tournament.builder()
                .id(tournamentId)
                .name("Knockout Cup")
                .format(TournamentFormat.CUP)
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .status(TournamentStatus.COMPLETED)
                .build();

        var daveUser = User.builder().id(UUID.randomUUID()).nickname("Dave").avatar("dave.png").build();
        var regDave = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(cupTournament).player(daveUser).status(RegistrationStatus.CONFIRMED).build();

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(cupTournament));
        when(registrationRepository.findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED))
                .thenReturn(List.of(regAlice, regBob, regCharlie, regDave));

        var semi1 = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(cupTournament)
                .round(1)
                .participant1(regAlice)
                .participant2(regCharlie)
                .winner(regAlice)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        var semi2 = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(cupTournament)
                .round(1)
                .participant1(regBob)
                .participant2(regDave)
                .winner(regBob)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        var finalMatch = TournamentMatch.builder()
                .id(UUID.randomUUID())
                .tournament(cupTournament)
                .round(2)
                .participant1(regAlice)
                .participant2(regBob)
                .winner(regAlice)
                .status(TournamentMatchStatus.COMPLETED)
                .build();

        when(tournamentMatchRepository.findByTournamentId(tournamentId))
                .thenReturn(List.of(semi1, semi2, finalMatch));

        var standings = standingsService.calculateStandings(tournamentId);

        assertThat(standings).hasSize(4);

        var champion = standings.get(0);
        assertThat(champion.registrationId()).isEqualTo(regAlice.getId());
        assertThat(champion.isEliminated()).isFalse();
        assertThat(champion.rank()).isEqualTo(1);
        assertThat(champion.wins()).isEqualTo(2);

        var runnerUp = standings.get(1);
        assertThat(runnerUp.registrationId()).isEqualTo(regBob.getId());
        assertThat(runnerUp.isEliminated()).isTrue();
        assertThat(runnerUp.rank()).isEqualTo(2);
        assertThat(runnerUp.wins()).isEqualTo(1);

        var third = standings.get(2);
        assertThat(third.isEliminated()).isTrue();
        assertThat(third.rank()).isEqualTo(3);
        assertThat(third.wins()).isEqualTo(0);

        var fourth = standings.get(3);
        assertThat(fourth.isEliminated()).isTrue();
        assertThat(fourth.rank()).isEqualTo(4);
        assertThat(fourth.wins()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should exclude stub substitute match points and stats in 2v2 random pairing mode")
    void shouldExcludeStubPartnerSubstituteMatchesFromStubPlayerStandings() {
        var randomPairingTournament = Tournament.builder()
                .id(tournamentId)
                .name("Random 2v2")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS)
                .status(TournamentStatus.IN_PROGRESS)
                .build();

        var daveUser = User.builder().id(UUID.randomUUID()).nickname("Dave").avatar("dave.png").build();
        var regDave = TournamentRegistration.builder().id(UUID.randomUUID()).tournament(randomPairingTournament).player(daveUser).status(RegistrationStatus.CONFIRMED).build();

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(randomPairingTournament));
        when(registrationRepository.findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED))
                .thenReturn(List.of(regAlice, regBob, regCharlie, regDave));

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
                .round(1)
                .build();

        when(tournamentMatchRepository.findByTournamentId(tournamentId))
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
    @DisplayName("Should gracefully handle GDPR deleted users and partners by returning Anonymous fallback")
    void shouldHandleGdprDeletedUsersGracefully() {
        var regDeletedPlayer = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .player(null)
                .partner(User.builder().id(UUID.randomUUID()).nickname("").build())
                .status(RegistrationStatus.CONFIRMED)
                .build();

        when(tournamentRepository.findById(tournamentId)).thenReturn(Optional.of(tournament));
        when(registrationRepository.findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED))
                .thenReturn(List.of(regDeletedPlayer));
        when(tournamentMatchRepository.findByTournamentId(tournamentId))
                .thenReturn(List.of());

        var standings = standingsService.calculateStandings(tournamentId);

        assertThat(standings).hasSize(1);
        var standing = standings.get(0);
        assertThat(standing.nickname()).isEqualTo("Anonymous");
        assertThat(standing.partnerNickname()).isEqualTo("Anonymous");
        assertThat(standing.avatarUrl()).isNull();
    }
}
