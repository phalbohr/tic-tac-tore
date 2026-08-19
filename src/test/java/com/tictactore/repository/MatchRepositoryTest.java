package com.tictactore.repository;

import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("MatchRepository Tests")
class MatchRepositoryTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;
    private User userC;

    @BeforeEach
    void setUp() {
        matchRepository.deleteAll();
        userRepository.deleteAll();

        userA = userRepository.save(User.builder().email("a@test.com").nickname("UserA").build());
        userB = userRepository.save(User.builder().email("b@test.com").nickname("UserB").build());
        userC = userRepository.save(User.builder().email("c@test.com").nickname("UserC").build());
    }

    @Test
    void findMatchHistory_shouldReturnConfirmedMatchesForParticipant() {
        var match = Match.builder()
                .creatorId(userA.getId())
                .teamAAttackerId(userA.getId())
                .teamBAttackerId(userB.getId())
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build();
        match.addGame(Game.builder().gameOrder(1).teamAScore(10).teamBScore(8).build());
        matchRepository.save(match);

        var result = matchRepository.findMatchHistory(
                userA.getId(), "CONFIRMED", null, null, null, PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getGames()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1L);
    }

    @Test
    void findMatchHistory_shouldFilterByOpponentOrTeammate() {
        var match1 = Match.builder()
                .creatorId(userA.getId())
                .teamAAttackerId(userA.getId())
                .teamBAttackerId(userB.getId())
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now().minusSeconds(100))
                .build();
        matchRepository.save(match1);

        var match2 = Match.builder()
                .creatorId(userA.getId())
                .teamAAttackerId(userA.getId())
                .teamBAttackerId(userC.getId())
                .status(Match.STATUS_CONFIRMED)
                .createdAt(Instant.now())
                .build();
        matchRepository.save(match2);

        var result = matchRepository.findMatchHistory(
                userA.getId(), "CONFIRMED", userB.getId(), null, null, PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTeamBAttackerId()).isEqualTo(userB.getId());
    }
}
