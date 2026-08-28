package com.tictactore.repository;

import com.tictactore.model.MatchmakingPool;
import com.tictactore.model.MatchType;
import com.tictactore.model.PoolParticipant;
import com.tictactore.model.PoolParticipantRole;
import com.tictactore.model.PoolStatus;
import com.tictactore.model.SkillLevel;
import com.tictactore.model.StartCondition;
import com.tictactore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("MatchmakingPoolRepository Data JPA Tests")
class MatchmakingPoolRepositoryTest {

    @Autowired
    private MatchmakingPoolRepository matchmakingPoolRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User creator;
    private User player2;

    @BeforeEach
    void setUp() {
        creator = userRepository.save(User.builder().email("host@example.com").nickname("HostUser").build());
        player2 = userRepository.save(User.builder().email("player2@example.com").nickname("PlayerTwo").build());
    }

    @Test
    void shouldSavePoolWithParticipantsAndCascade() {
        var pool = MatchmakingPool.builder()
                .creator(creator)
                .matchType(MatchType.ONE_VS_ONE)
                .startCondition(StartCondition.FILL_BASED)
                .skillLevel(SkillLevel.OPEN_FOR_ALL)
                .status(PoolStatus.OPEN)
                .build();

        var host = PoolParticipant.builder()
                .pool(pool)
                .user(creator)
                .role(PoolParticipantRole.HOST)
                .joinedAt(Instant.now())
                .build();
        pool.addParticipant(host);

        var saved = matchmakingPoolRepository.save(pool);
        entityManager.flush();
        entityManager.clear();

        var found = matchmakingPoolRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCreator().getId()).isEqualTo(creator.getId());
        assertThat(found.get().getParticipants()).hasSize(1);
        assertThat(found.get().getParticipants().get(0).getRole()).isEqualTo(PoolParticipantRole.HOST);
        assertThat(found.get().getParticipants().get(0).getUser().getId()).isEqualTo(creator.getId());
    }

    @Test
    void shouldCountActivePoolsByCreatorIdAndStatus() {
        var openPool1 = MatchmakingPool.builder()
                .creator(creator)
                .matchType(MatchType.ONE_VS_ONE)
                .startCondition(StartCondition.FILL_BASED)
                .skillLevel(SkillLevel.OPEN_FOR_ALL)
                .status(PoolStatus.OPEN)
                .build();
        var openPool2 = MatchmakingPool.builder()
                .creator(creator)
                .matchType(MatchType.TWO_VS_TWO)
                .startCondition(StartCondition.SCHEDULED_TIME)
                .scheduledTime(Instant.now().plus(2, ChronoUnit.DAYS))
                .skillLevel(SkillLevel.ADVANCED)
                .status(PoolStatus.OPEN)
                .build();
        var filledPool = MatchmakingPool.builder()
                .creator(creator)
                .matchType(MatchType.ONE_VS_ONE)
                .startCondition(StartCondition.FILL_BASED)
                .skillLevel(SkillLevel.OPEN_FOR_ALL)
                .status(PoolStatus.FILLED)
                .build();

        matchmakingPoolRepository.save(openPool1);
        matchmakingPoolRepository.save(openPool2);
        matchmakingPoolRepository.save(filledPool);
        entityManager.flush();

        var activeCount = matchmakingPoolRepository.countByCreatorIdAndStatus(creator.getId(), PoolStatus.OPEN);
        var filledCount = matchmakingPoolRepository.countByCreatorIdAndStatus(creator.getId(), PoolStatus.FILLED);
        var otherUserCount = matchmakingPoolRepository.countByCreatorIdAndStatus(UUID.randomUUID(), PoolStatus.OPEN);

        assertThat(activeCount).isEqualTo(2L);
        assertThat(filledCount).isEqualTo(1L);
        assertThat(otherUserCount).isEqualTo(0L);
    }

    @Test
    void shouldFindByIdAndStatus() {
        var openPool = MatchmakingPool.builder()
                .creator(creator)
                .matchType(MatchType.ONE_VS_ONE)
                .startCondition(StartCondition.FILL_BASED)
                .skillLevel(SkillLevel.OPEN_FOR_ALL)
                .status(PoolStatus.OPEN)
                .build();
        var saved = matchmakingPoolRepository.save(openPool);
        entityManager.flush();
        entityManager.clear();

        var foundOpen = matchmakingPoolRepository.findByIdAndStatus(saved.getId(), PoolStatus.OPEN);
        var foundCancelled = matchmakingPoolRepository.findByIdAndStatus(saved.getId(), PoolStatus.CANCELLED);

        assertThat(foundOpen).isPresent();
        assertThat(foundOpen.get().getId()).isEqualTo(saved.getId());
        assertThat(foundCancelled).isEmpty();
    }

    @Test
    void shouldEnforceUniqueParticipantPerPoolConstraint() {
        var pool = MatchmakingPool.builder()
                .creator(creator)
                .matchType(MatchType.TWO_VS_TWO)
                .startCondition(StartCondition.FILL_BASED)
                .skillLevel(SkillLevel.OPEN_FOR_ALL)
                .status(PoolStatus.OPEN)
                .build();

        var part1 = PoolParticipant.builder()
                .pool(pool)
                .user(creator)
                .role(PoolParticipantRole.HOST)
                .joinedAt(Instant.now())
                .build();
        var part2 = PoolParticipant.builder()
                .pool(pool)
                .user(creator)
                .role(PoolParticipantRole.PLAYER)
                .joinedAt(Instant.now())
                .build();
        pool.getParticipants().add(part1);
        pool.getParticipants().add(part2);

        matchmakingPoolRepository.save(pool);

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(jakarta.persistence.PersistenceException.class);
    }
}
