package com.tictactore.repository;

import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.MatchStatus;
import com.tictactore.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для проверки персистентности (сохранения в БД) матчей и игр.
 * Мы используем @DataJpaTest для тестирования только слоя работы с данными.
 */
@DataJpaTest
@DisplayName("MatchRepository Tests")
public class MatchRepositoryTest {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("should save and retrieve a match with games and participants")
    public void shouldSaveAndRetrieveMatch() {
        // Given: Создаем участников. В реальном приложении они уже были бы в базе.
        User creator = createUser("creator@test.com", "Creator");
        User teammate = createUser("teammate@test.com", "Teammate");
        User opponent1 = createUser("opponent1@test.com", "Opponent 1");
        User opponent2 = createUser("opponent2@test.com", "Opponent 2");

        // Given: Настраиваем объект матча
        Match match = new Match();
        match.setCreator(creator);
        match.setTeamAAttacker(creator);
        match.setTeamADefender(teammate);
        match.setTeamBAttacker(opponent1);
        match.setTeamBDefender(opponent2);
        match.setStatus(MatchStatus.PENDING_APPROVAL);

        // Given: Добавляем игры в матч (Best of 3)
        Game game1 = new Game();
        game1.setGameNumber(1);
        game1.setTeamAScore(10);
        game1.setTeamBScore(8);
        match.addGame(game1);

        Game game2 = new Game();
        game2.setGameNumber(2);
        game2.setTeamAScore(10);
        game2.setTeamBScore(5);
        match.addGame(game2);

        // When: Сохраняем в базу через репозиторий
        Match savedMatch = matchRepository.save(match);
        
        // Очищаем контекст, чтобы при чтении Hibernate реально пошел в БД, а не взял объект из кеша
        entityManager.flush();
        entityManager.clear();

        // Then: Проверяем, что всё сохранилось корректно
        Optional<Match> result = matchRepository.findById(savedMatch.getId());
        assertThat(result).isPresent();
        Match retrieved = result.get();

        assertThat(retrieved.getStatus()).isEqualTo(MatchStatus.PENDING_APPROVAL);
        assertThat(retrieved.getGames()).hasSize(2);
        assertThat(retrieved.getTeamAAttacker().getEmail()).isEqualTo("creator@test.com");
        
        // Проверяем каскадное сохранение: игры должны были сохраниться вместе с матчем
        assertThat(retrieved.getGames())
                .extracting(Game::getGameNumber)
                .containsExactlyInAnyOrder(1, 2);
    }

    private User createUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setProviderId(UUID.randomUUID().toString());
        return entityManager.persist(user);
    }
}
