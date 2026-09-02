package com.tictactore.service.tournament;

import com.tictactore.dto.tournament.SeededParticipant;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.User;
import com.tictactore.service.tournament.impl.ChampionshipBracketGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChampionshipBracketGenerator Tests")
class ChampionshipBracketGeneratorTest {

    private ChampionshipBracketGenerator bracketGenerator;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        bracketGenerator = new ChampionshipBracketGenerator();
        tournament = Tournament.builder()
                .id(UUID.randomUUID())
                .name("Championship 2026")
                .format(TournamentFormat.CHAMPIONSHIP)
                .roundCount(3)
                .build();
    }

    @Test
    void shouldGenerateRoundRobinMatchesForFourPlayers() {
        List<SeededParticipant> participants = createParticipants(4);

        List<TournamentMatch> matches = bracketGenerator.generateBracket(tournament, participants);

        assertThat(matches).hasSize(6);

        long round1Matches = matches.stream().filter(m -> m.getRound() == 1).count();
        long round2Matches = matches.stream().filter(m -> m.getRound() == 2).count();
        long round3Matches = matches.stream().filter(m -> m.getRound() == 3).count();

        assertThat(round1Matches).isEqualTo(2);
        assertThat(round2Matches).isEqualTo(2);
        assertThat(round3Matches).isEqualTo(2);

        assertThat(matches.stream().filter(m -> m.getRound() == 1))
                .allMatch(m -> m.getStatus() == TournamentMatchStatus.READY);
        assertThat(matches.stream().filter(m -> m.getRound() > 1))
                .allMatch(m -> m.getStatus() == TournamentMatchStatus.PENDING);
    }

    @Test
    void shouldHandleOddNumberOfPlayersWithByeSlot() {
        List<SeededParticipant> participants = createParticipants(3);

        List<TournamentMatch> matches = bracketGenerator.generateBracket(tournament, participants);

        assertThat(matches).hasSize(6);
        assertThat(matches.stream().filter(m -> m.getRound() == 1)).hasSize(2);
        assertThat(matches.stream().filter(m -> m.getRound() == 2)).hasSize(2);
        assertThat(matches.stream().filter(m -> m.getRound() == 3)).hasSize(2);
        assertThat(matches.stream().filter(m -> m.getStatus() == TournamentMatchStatus.BYE)).hasSize(3);
    }

    private List<SeededParticipant> createParticipants(int count) {
        List<SeededParticipant> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = User.builder().id(UUID.randomUUID()).nickname("Player" + i).build();
            TournamentRegistration reg = TournamentRegistration.builder()
                    .id(UUID.randomUUID())
                    .tournament(tournament)
                    .player(user)
                    .seed(i)
                    .strengthScore(1.0 / i)
                    .build();
            list.add(new SeededParticipant(reg, i, 1.0 / i));
        }
        return list;
    }
}
