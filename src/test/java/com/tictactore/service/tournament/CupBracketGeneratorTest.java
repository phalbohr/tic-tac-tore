package com.tictactore.service.tournament;

import com.tictactore.dto.tournament.SeededParticipant;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.User;
import com.tictactore.service.tournament.impl.CupBracketGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CupBracketGenerator Tests")
class CupBracketGeneratorTest {

    private CupBracketGenerator bracketGenerator;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        bracketGenerator = new CupBracketGenerator();
        tournament = Tournament.builder()
                .id(UUID.randomUUID())
                .name("Summer Cup")
                .format(TournamentFormat.CUP)
                .build();
    }

    @Test
    void shouldGenerateFourPlayerBracketWithoutByes() {
        List<SeededParticipant> participants = createParticipants(4);

        List<TournamentMatch> matches = bracketGenerator.generateBracket(tournament, participants);

        assertThat(matches).hasSize(3);

        TournamentMatch m1 = matches.get(0);
        assertThat(m1.getRound()).isEqualTo(1);
        assertThat(m1.getMatchOrder()).isEqualTo(1);
        assertThat(m1.getSeed1()).isEqualTo(1);
        assertThat(m1.getSeed2()).isEqualTo(4);
        assertThat(m1.getStatus()).isEqualTo(TournamentMatchStatus.READY);

        TournamentMatch m2 = matches.get(1);
        assertThat(m2.getRound()).isEqualTo(1);
        assertThat(m2.getMatchOrder()).isEqualTo(2);
        assertThat(m2.getSeed1()).isEqualTo(2);
        assertThat(m2.getSeed2()).isEqualTo(3);
        assertThat(m2.getStatus()).isEqualTo(TournamentMatchStatus.READY);

        TournamentMatch finalMatch = matches.get(2);
        assertThat(finalMatch.getRound()).isEqualTo(2);
        assertThat(finalMatch.getMatchOrder()).isEqualTo(1);
        assertThat(finalMatch.getStatus()).isEqualTo(TournamentMatchStatus.PENDING);
        assertThat(m1.getNextMatch()).isEqualTo(finalMatch);
        assertThat(m2.getNextMatch()).isEqualTo(finalMatch);
    }

    @Test
    void shouldGenerateSixPlayerBracketWithTwoByesAndAdvanceWinners() {
        List<SeededParticipant> participants = createParticipants(6);

        List<TournamentMatch> matches = bracketGenerator.generateBracket(tournament, participants);

        assertThat(matches).hasSize(7);

        TournamentMatch m1 = matches.get(0);
        assertThat(m1.getSeed1()).isEqualTo(1);
        assertThat(m1.getSeed2()).isNull();
        assertThat(m1.getParticipant2()).isNull();
        assertThat(m1.getStatus()).isEqualTo(TournamentMatchStatus.BYE);
        assertThat(m1.getWinner()).isEqualTo(m1.getParticipant1());

        TournamentMatch m2 = matches.get(1);
        assertThat(m2.getSeed1()).isEqualTo(4);
        assertThat(m2.getSeed2()).isEqualTo(5);
        assertThat(m2.getStatus()).isEqualTo(TournamentMatchStatus.READY);

        TournamentMatch m3 = matches.get(2);
        assertThat(m3.getSeed1()).isEqualTo(2);
        assertThat(m3.getSeed2()).isNull();
        assertThat(m3.getParticipant2()).isNull();
        assertThat(m3.getStatus()).isEqualTo(TournamentMatchStatus.BYE);
        assertThat(m3.getWinner()).isEqualTo(m3.getParticipant1());

        TournamentMatch m4 = matches.get(3);
        assertThat(m4.getSeed1()).isEqualTo(3);
        assertThat(m4.getSeed2()).isEqualTo(6);
        assertThat(m4.getStatus()).isEqualTo(TournamentMatchStatus.READY);

        TournamentMatch semi1 = matches.get(4);
        assertThat(semi1.getRound()).isEqualTo(2);
        assertThat(semi1.getMatchOrder()).isEqualTo(1);
        assertThat(semi1.getParticipant1()).isEqualTo(m1.getParticipant1());
        assertThat(semi1.getSeed1()).isEqualTo(1);
        assertThat(semi1.getParticipant2()).isNull();
        assertThat(semi1.getStatus()).isEqualTo(TournamentMatchStatus.PENDING);

        TournamentMatch semi2 = matches.get(5);
        assertThat(semi2.getRound()).isEqualTo(2);
        assertThat(semi2.getMatchOrder()).isEqualTo(2);
        assertThat(semi2.getParticipant1()).isEqualTo(m3.getParticipant1());
        assertThat(semi2.getSeed1()).isEqualTo(2);
        assertThat(semi2.getParticipant2()).isNull();
        assertThat(semi2.getStatus()).isEqualTo(TournamentMatchStatus.PENDING);
    }

    @Test
    void shouldHandleBoundaryParticipantCountsWithoutOverflow() {
        var twoParticipants = createParticipants(2);

        var matches = bracketGenerator.generateBracket(tournament, twoParticipants);

        assertThat(matches).hasSize(1);
        assertThat(matches.get(0).getRound()).isEqualTo(1);
        assertThat(matches.get(0).getStatus()).isEqualTo(TournamentMatchStatus.READY);
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
