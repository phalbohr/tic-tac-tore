package com.tictactore.service.tournament;

import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.service.tournament.impl.StrengthBasedSeedingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("StrengthBasedSeedingStrategy Tests")
class StrengthBasedSeedingStrategyTest {

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private StrengthBasedSeedingStrategy seedingStrategy;

    private User player1;
    private User player2;
    private User player3;
    private Tournament tournament1v1;

    @BeforeEach
    void setUp() {
        player1 = User.builder().id(UUID.randomUUID()).nickname("Alpha").build();
        player2 = User.builder().id(UUID.randomUUID()).nickname("Beta").build();
        player3 = User.builder().id(UUID.randomUUID()).nickname("Gamma").build();

        tournament1v1 = Tournament.builder()
                .id(UUID.randomUUID())
                .mode(TournamentMode.ONE_VS_ONE_PERSONAL)
                .build();
    }

    @Test
    void shouldSeedParticipantsByWinRateDescending() {
        var reg1 = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(player1)
                .createdAt(Instant.parse("2026-09-01T10:00:00Z"))
                .build();
        var reg2 = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(player2)
                .createdAt(Instant.parse("2026-09-01T10:05:00Z"))
                .build();
        var reg3 = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(player3)
                .createdAt(Instant.parse("2026-09-01T10:10:00Z"))
                .build();

        Match winMatch1 = createMatch(player1.getId(), UUID.randomUUID(), 5, 2);
        Match winMatch2 = createMatch(player1.getId(), UUID.randomUUID(), 5, 3);
        when(matchRepository.findConfirmedMatchesByPlayerId(player1.getId())).thenReturn(List.of(winMatch1, winMatch2));

        Match winMatch3 = createMatch(player2.getId(), UUID.randomUUID(), 5, 1);
        Match lossMatch1 = createMatch(UUID.randomUUID(), player2.getId(), 5, 2);
        when(matchRepository.findConfirmedMatchesByPlayerId(player2.getId())).thenReturn(List.of(winMatch3, lossMatch1));

        when(matchRepository.findConfirmedMatchesByPlayerId(player3.getId())).thenReturn(List.of());

        var result = seedingStrategy.seed(tournament1v1, List.of(reg2, reg3, reg1));

        assertThat(result).hasSize(3);
        assertThat(result.get(0).registration().getPlayer().getId()).isEqualTo(player1.getId());
        assertThat(result.get(0).seed()).isEqualTo(1);
        assertThat(result.get(0).strengthScore()).isEqualTo(1.0);

        assertThat(result.get(1).registration().getPlayer().getId()).isEqualTo(player2.getId());
        assertThat(result.get(1).seed()).isEqualTo(2);
        assertThat(result.get(1).strengthScore()).isEqualTo(0.5);

        assertThat(result.get(2).registration().getPlayer().getId()).isEqualTo(player3.getId());
        assertThat(result.get(2).seed()).isEqualTo(3);
        assertThat(result.get(2).strengthScore()).isEqualTo(0.0);
    }

    @Test
    void shouldSeedFixedTeamsByCombinedAverageWinRate() {
        User partner1 = User.builder().id(UUID.randomUUID()).nickname("Partner1").build();
        User partner2 = User.builder().id(UUID.randomUUID()).nickname("Partner2").build();

        Tournament tournament2v2 = Tournament.builder()
                .id(UUID.randomUUID())
                .mode(TournamentMode.TWO_VS_TWO_FIXED_TEAMS)
                .build();

        var team1 = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(player1)
                .partner(partner1)
                .createdAt(Instant.parse("2026-09-01T10:00:00Z"))
                .build();
        var team2 = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(player2)
                .partner(partner2)
                .createdAt(Instant.parse("2026-09-01T10:05:00Z"))
                .build();

        when(matchRepository.findConfirmedMatchesByPlayerId(player1.getId()))
                .thenReturn(List.of(createMatch(player1.getId(), UUID.randomUUID(), 5, 0)));
        when(matchRepository.findConfirmedMatchesByPlayerId(partner1.getId()))
                .thenReturn(List.of(createMatch(partner1.getId(), UUID.randomUUID(), 5, 0)));

        when(matchRepository.findConfirmedMatchesByPlayerId(player2.getId()))
                .thenReturn(List.of(createMatch(player2.getId(), UUID.randomUUID(), 5, 0)));
        when(matchRepository.findConfirmedMatchesByPlayerId(partner2.getId()))
                .thenReturn(List.of(createMatch(UUID.randomUUID(), partner2.getId(), 5, 0)));

        var result = seedingStrategy.seed(tournament2v2, List.of(team2, team1));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).registration().getId()).isEqualTo(team1.getId());
        assertThat(result.get(0).seed()).isEqualTo(1);
        assertThat(result.get(0).strengthScore()).isEqualTo(1.0);

        assertThat(result.get(1).registration().getId()).isEqualTo(team2.getId());
        assertThat(result.get(1).seed()).isEqualTo(2);
        assertThat(result.get(1).strengthScore()).isEqualTo(0.5);
    }

    @Test
    void shouldSeedZeroMatchParticipantsBelowParticipantsWithMatchHistoryEvenWithZeroWins() {
        var zeroMatchRegisteredFirst = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(player1)
                .createdAt(Instant.parse("2026-09-01T08:00:00Z"))
                .build();
        var zeroWinsRegisteredSecond = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(player2)
                .createdAt(Instant.parse("2026-09-01T09:00:00Z"))
                .build();

        when(matchRepository.findConfirmedMatchesByPlayerId(player1.getId())).thenReturn(List.of());
        when(matchRepository.findConfirmedMatchesByPlayerId(player2.getId()))
                .thenReturn(List.of(createMatch(UUID.randomUUID(), player2.getId(), 5, 2)));

        var result = seedingStrategy.seed(tournament1v1, List.of(zeroMatchRegisteredFirst, zeroWinsRegisteredSecond));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).registration().getId()).isEqualTo(zeroWinsRegisteredSecond.getId());
        assertThat(result.get(0).seed()).isEqualTo(1);
        assertThat(result.get(1).registration().getId()).isEqualTo(zeroMatchRegisteredFirst.getId());
        assertThat(result.get(1).seed()).isEqualTo(2);
    }

    @Test
    void shouldNotPenalize2v2TeamWhenOnePartnerHasNoMatchHistory() {
        User partner1 = User.builder().id(UUID.randomUUID()).nickname("NewbiePartner").build();
        Tournament tournament2v2 = Tournament.builder()
                .id(UUID.randomUUID())
                .mode(TournamentMode.TWO_VS_TWO_FIXED_TEAMS)
                .build();
        var team = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(player1)
                .partner(partner1)
                .createdAt(Instant.parse("2026-09-01T10:00:00Z"))
                .build();

        when(matchRepository.findConfirmedMatchesByPlayerId(player1.getId()))
                .thenReturn(List.of(createMatch(player1.getId(), UUID.randomUUID(), 5, 0)));
        when(matchRepository.findConfirmedMatchesByPlayerId(partner1.getId()))
                .thenReturn(List.of());

        var result = seedingStrategy.seed(tournament2v2, List.of(team));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).seed()).isEqualTo(1);
        assertThat(result.get(0).strengthScore()).isEqualTo(1.0);
    }

    @Test
    void shouldPreferPlayerMatchStatsProjectionWhenAvailable() {
        var reg1 = TournamentRegistration.builder()
                .id(UUID.randomUUID())
                .player(player1)
                .createdAt(Instant.parse("2026-09-01T10:00:00Z"))
                .build();

        com.tictactore.repository.projection.PlayerMatchStatsProjection mockProjection =
                new com.tictactore.repository.projection.PlayerMatchStatsProjection() {
                    @Override
                    public long getTotalMatches() {
                        return 10;
                    }

                    @Override
                    public long getWins() {
                        return 8;
                    }
                };
        when(matchRepository.getPlayerMatchStats(player1.getId())).thenReturn(mockProjection);

        var result = seedingStrategy.seed(tournament1v1, List.of(reg1));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).seed()).isEqualTo(1);
        assertThat(result.get(0).strengthScore()).isEqualTo(0.8);
    }

    private Match createMatch(UUID winnerId, UUID loserId, int winScore, int loseScore) {
        Match match = Match.builder()
                .id(UUID.randomUUID())
                .teamAAttackerId(winnerId)
                .teamBAttackerId(loserId)
                .status("CONFIRMED")
                .build();

        Game game = Game.builder()
                .id(UUID.randomUUID())
                .match(match)
                .gameOrder(1)
                .teamAScore(winScore)
                .teamBScore(loseScore)
                .build();

        match.setGames(List.of(game));
        return match;
    }
}
