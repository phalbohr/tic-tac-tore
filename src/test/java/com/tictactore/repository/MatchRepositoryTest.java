package com.tictactore.repository;

import com.tictactore.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("MatchRepository Tests")
public class MatchRepositoryTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("should save and retrieve a match with games and participants")
    void shouldSaveAndRetrieveMatch() {
        var creator = createUser("creator@test.com", "Creator");
        var teammate = createUser("teammate@test.com", "Teammate");
        var opponent1 = createUser("opponent1@test.com", "Opponent 1");
        var opponent2 = createUser("opponent2@test.com", "Opponent 2");

        var match = new Match();
        match.setCreator(creator);
        match.setTeamAAttacker(creator);
        match.setTeamADefender(teammate);
        match.setTeamBAttacker(opponent1);
        match.setTeamBDefender(opponent2);
        match.setStatus(MatchStatus.PENDING_APPROVAL);

        var game1 = new Game();
        game1.setGameNumber(1);
        game1.setTeamAScore(10);
        game1.setTeamBScore(8);
        match.addGame(game1);

        var game2 = new Game();
        game2.setGameNumber(2);
        game2.setTeamAScore(10);
        game2.setTeamBScore(5);
        match.addGame(game2);

        var savedMatch = matchRepository.save(match);
        
        entityManager.flush();
        entityManager.clear();

        var result = matchRepository.findById(savedMatch.getId());
        assertThat(result).isPresent();
        var retrieved = result.get();

        assertThat(retrieved.getStatus()).isEqualTo(MatchStatus.PENDING_APPROVAL);
        assertThat(retrieved.getGames()).hasSize(2);
        assertThat(retrieved.getTeamAAttacker().getEmail()).isEqualTo("creator@test.com");
        
        assertThat(retrieved.getGames())
                .extracting(Game::getGameNumber)
                .containsExactlyInAnyOrder(1, 2);
    }

    @Test
    @DisplayName("findLeaderboard - should return aggregate rankings")
    void findLeaderboard_shouldReturnRankings() {
        var p1 = createUser("p1@test.com", "Player 1");
        var p2 = createUser("p2@test.com", "Player 2");
        var p3 = createUser("p3@test.com", "Player 3");
        var p4 = createUser("p4@test.com", "Player 4");

        createConfirmedMatch(p1, p2, p3, p4, WinnerTeam.TEAM_A);
        createConfirmedMatch(p1, p2, p3, p4, WinnerTeam.TEAM_A);
        createConfirmedMatch(p3, p4, p1, p2, WinnerTeam.TEAM_A);

        entityManager.flush();
        entityManager.clear();

        var since = LocalDateTime.now().minusDays(1);
        var pageable = PageRequest.of(0, 10);
        
        var leaderboard = matchRepository.findLeaderboard("OVERALL", since, 0, pageable);

        assertThat(leaderboard.getContent()).isNotEmpty();
        var p1Stats = leaderboard.getContent().stream()
                .filter(p -> p.getPlayerName().equals("Player 1"))
                .findFirst()
                .orElseThrow();
        
        assertThat(p1Stats.getTotalMatches()).isEqualTo(3);
        assertThat(p1Stats.getWins()).isEqualTo(2);
        assertThat(p1Stats.getLosses()).isEqualTo(1);
        assertThat(p1Stats.getWinRate()).isCloseTo(66.67, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("findH2HStats - should return head-to-head records")
    void findH2HStats_shouldReturnResults() {
        var p1 = createUser("p1@test.com", "Player 1");
        var p2 = createUser("p2@test.com", "Player 2");
        var p3 = createUser("p3@test.com", "Player 3");
        var p4 = createUser("p4@test.com", "Player 4");

        createConfirmedMatch(p1, p2, p3, p4, WinnerTeam.TEAM_A);
        createConfirmedMatch(p1, p2, p3, p4, WinnerTeam.TEAM_B);

        entityManager.flush();
        entityManager.clear();

        var since = LocalDateTime.now().minusDays(1);
        var pageable = PageRequest.of(0, 10);
        
        var h2h = matchRepository.findH2HStats(p1.getId(), since, "OVERALL", "OVERALL", pageable);

        assertThat(h2h.getContent()).hasSize(2); // Against p3 and p4
        var p3Stats = h2h.getContent().stream()
                .filter(p -> p.getOpponentName().equals("Player 3"))
                .findFirst()
                .orElseThrow();
        
        assertThat(p3Stats.getTotalMatches()).isEqualTo(2);
        assertThat(p3Stats.getWins()).isEqualTo(1);
        assertThat(p3Stats.getLosses()).isEqualTo(1);
    }

    @Test
    @DisplayName("findPlayerStats - should return personal breakdown")
    void findPlayerStats_shouldReturnBreakdown() {
        var p1 = createUser("p1@test.com", "Player 1");
        var p2 = createUser("p2@test.com", "Player 2");
        var p3 = createUser("p3@test.com", "Player 3");
        var p4 = createUser("p4@test.com", "Player 4");

        // p1 as Attacker (win)
        createConfirmedMatch(p1, p2, p3, p4, WinnerTeam.TEAM_A);
        // p1 as Defender (loss)
        createConfirmedMatch(p2, p1, p3, p4, WinnerTeam.TEAM_B);

        entityManager.flush();
        entityManager.clear();

        var since = LocalDateTime.now().minusDays(1);
        var stats = matchRepository.findPlayerStats(p1.getId(), since);

        assertThat(stats.getOverallMatches()).isEqualTo(2);
        assertThat(stats.getOverallWins()).isEqualTo(1);
        assertThat(stats.getAttackerMatches()).isEqualTo(1);
        assertThat(stats.getAttackerWins()).isEqualTo(1);
        assertThat(stats.getDefenderMatches()).isEqualTo(1);
        assertThat(stats.getDefenderWins()).isEqualTo(0);
    }

    private User createUser(String email, String name) {
        var user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setProviderId(UUID.randomUUID().toString());
        return entityManager.persist(user);
    }

    private void createConfirmedMatch(User aAtt, User aDef, User bAtt, User bDef, WinnerTeam winner) {
        var match = new Match();
        match.setCreator(aAtt);
        match.setTeamAAttacker(aAtt);
        match.setTeamADefender(aDef);
        match.setTeamBAttacker(bAtt);
        match.setTeamBDefender(bDef);
        match.setStatus(MatchStatus.CONFIRMED);
        match.setWinner(winner);
        
        var game = new Game();
        game.setGameNumber(1);
        game.setTeamAScore(winner == WinnerTeam.TEAM_A ? 10 : 0);
        game.setTeamBScore(winner == WinnerTeam.TEAM_B ? 10 : 0);
        match.addGame(game);
        
        matchRepository.save(match);
    }
}
