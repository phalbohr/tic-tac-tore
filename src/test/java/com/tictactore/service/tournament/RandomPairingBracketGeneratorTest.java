package com.tictactore.service.tournament;

import com.tictactore.dto.tournament.SeededParticipant;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentMode;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.User;
import com.tictactore.service.tournament.impl.RandomPairingBracketGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("RandomPairingBracketGenerator Tests")
class RandomPairingBracketGeneratorTest {

    private RandomPairingBracketGenerator generator;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        generator = new RandomPairingBracketGenerator();
        tournament = Tournament.builder()
                .id(UUID.fromString("11111111-2222-3333-4444-555555555555"))
                .name("2v2 Random Pairing Cup 2026")
                .format(TournamentFormat.CHAMPIONSHIP)
                .mode(TournamentMode.TWO_VS_TWO_RANDOM_PAIRINGS)
                .roundCount(3)
                .build();
    }

    @Test
    void shouldThrowExceptionWhenFewerThanFourParticipants() {
        List<SeededParticipant> participants = createParticipants(3);

        assertThatThrownBy(() -> generator.generateBracket(tournament, participants))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least 4 participants");
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 6, 8, 12, 16})
    void shouldGuaranteeEqualMatchDistributionForParticipants(int participantCount) {
        List<SeededParticipant> participants = createParticipants(participantCount);

        List<TournamentMatch> matches = generator.generateBracket(tournament, participants);

        Map<UUID, Integer> matchCounts = new HashMap<>();
        for (SeededParticipant p : participants) {
            matchCounts.put(p.registration().getId(), 0);
        }

        for (TournamentMatch match : matches) {
            assertThat(match.getParticipant1()).isNotNull();
            assertThat(match.getParticipant1Partner()).isNotNull();
            assertThat(match.getParticipant2()).isNotNull();
            assertThat(match.getParticipant2Partner()).isNotNull();

            Set<UUID> distinctInMatch = Set.of(
                    match.getParticipant1().getId(),
                    match.getParticipant1Partner().getId(),
                    match.getParticipant2().getId(),
                    match.getParticipant2Partner().getId()
            );
            assertThat(distinctInMatch).hasSize(4);

            distinctInMatch.forEach(id -> matchCounts.merge(id, 1, Integer::sum));
        }

        int expectedMatchesPerPlayer = matchCounts.values().iterator().next();
        assertThat(expectedMatchesPerPlayer).isPositive();
        assertThat(matchCounts.values()).allMatch(count -> count == expectedMatchesPerPlayer);
    }

    @Test
    void shouldMinimizePartnerRepetitionAcrossRoundsForEightPlayers() {
        List<SeededParticipant> participants = createParticipants(8);

        List<TournamentMatch> matches = generator.generateBracket(tournament, participants);

        Map<String, Integer> partnerPairs = new HashMap<>();
        for (TournamentMatch m : matches) {
            recordPair(partnerPairs, m.getParticipant1().getId(), m.getParticipant1Partner().getId());
            recordPair(partnerPairs, m.getParticipant2().getId(), m.getParticipant2Partner().getId());
        }

        long duplicatePartnerships = partnerPairs.values().stream().filter(count -> count > 1).count();
        assertThat(duplicatePartnerships).isZero();
    }

    @Test
    void shouldBeDeterministicAndReproducibleForSameTournamentSeed() {
        List<SeededParticipant> participants = createParticipants(8);

        List<TournamentMatch> matches1 = generator.generateBracket(tournament, participants);
        List<TournamentMatch> matches2 = generator.generateBracket(tournament, participants);

        assertThat(matches1).hasSameSizeAs(matches2);
        for (int i = 0; i < matches1.size(); i++) {
            TournamentMatch m1 = matches1.get(i);
            TournamentMatch m2 = matches2.get(i);

            assertThat(m1.getRound()).isEqualTo(m2.getRound());
            assertThat(m1.getParticipant1().getId()).isEqualTo(m2.getParticipant1().getId());
            assertThat(m1.getParticipant1Partner().getId()).isEqualTo(m2.getParticipant1Partner().getId());
            assertThat(m1.getParticipant2().getId()).isEqualTo(m2.getParticipant2().getId());
            assertThat(m1.getParticipant2Partner().getId()).isEqualTo(m2.getParticipant2Partner().getId());
        }
    }

    @Test
    void shouldSetRoundOneToReadyAndSubsequentRoundsToPending() {
        List<SeededParticipant> participants = createParticipants(4);

        List<TournamentMatch> matches = generator.generateBracket(tournament, participants);

        assertThat(matches.stream().filter(m -> m.getRound() == 1))
                .isNotEmpty()
                .allMatch(m -> m.getStatus() == TournamentMatchStatus.READY);

        assertThat(matches.stream().filter(m -> m.getRound() > 1))
                .isNotEmpty()
                .allMatch(m -> m.getStatus() == TournamentMatchStatus.PENDING);
    }

    private void recordPair(Map<String, Integer> pairCounts, UUID p1, UUID p2) {
        String key = p1.compareTo(p2) < 0 ? p1 + ":" + p2 : p2 + ":" + p1;
        pairCounts.merge(key, 1, Integer::sum);
    }

    private List<SeededParticipant> createParticipants(int count) {
        List<SeededParticipant> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            User user = User.builder()
                    .id(UUID.nameUUIDFromBytes(("user-" + i).getBytes()))
                    .nickname("Player " + i)
                    .build();
            TournamentRegistration reg = TournamentRegistration.builder()
                    .id(UUID.nameUUIDFromBytes(("reg-" + i).getBytes()))
                    .tournament(tournament)
                    .player(user)
                    .seed(i)
                    .strengthScore((double) (1000 + i * 50))
                    .build();
            list.add(new SeededParticipant(reg, i, 1000.0 + i * 50));
        }
        return list;
    }
}
