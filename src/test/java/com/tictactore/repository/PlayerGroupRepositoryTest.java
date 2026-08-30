package com.tictactore.repository;

import com.tictactore.model.PlayerGroup;
import com.tictactore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("PlayerGroupRepository Tests")
class PlayerGroupRepositoryTest {

    @Autowired
    private PlayerGroupRepository playerGroupRepository;

    @Autowired
    private UserRepository userRepository;

    private User creator;
    private User member1;
    private User member2;

    @BeforeEach
    void setUp() {
        playerGroupRepository.deleteAll();
        userRepository.deleteAll();

        creator = userRepository.save(User.builder().email("creator@example.com").nickname("creator").build());
        member1 = userRepository.save(User.builder().email("m1@example.com").nickname("member1").build());
        member2 = userRepository.save(User.builder().email("m2@example.com").nickname("member2").build());
    }

    @Test
    void shouldFindGroupsByCreatorIdOrderedByCreatedAtAsc() {
        var group1 = playerGroupRepository.save(PlayerGroup.builder()
                .name("Alpha Squad")
                .creatorId(creator.getId())
                .isFavorite(true)
                .members(Set.of(member1))
                .build());
        var group2 = playerGroupRepository.save(PlayerGroup.builder()
                .name("Beta Squad")
                .creatorId(creator.getId())
                .isFavorite(false)
                .members(Set.of(member2))
                .build());

        var result = playerGroupRepository.findByCreatorIdOrderByCreatedAtAsc(creator.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Alpha Squad");
        assertThat(result.get(0).getMembers()).hasSize(1);
        assertThat(result.get(1).getName()).isEqualTo("Beta Squad");
    }

    @Test
    void shouldFindGroupByIdAndCreatorId() {
        var group = playerGroupRepository.save(PlayerGroup.builder()
                .name("Favorites")
                .creatorId(creator.getId())
                .isFavorite(true)
                .members(Set.of(member1, member2))
                .build());

        var found = playerGroupRepository.findByIdAndCreatorId(group.getId(), creator.getId());
        var notFound = playerGroupRepository.findByIdAndCreatorId(group.getId(), UUID.randomUUID());

        assertThat(found).isPresent();
        assertThat(found.get().getMembers()).hasSize(2);
        assertThat(notFound).isEmpty();
    }

    @Test
    void shouldFindGroupByIdWithMembers() {
        var group = playerGroupRepository.save(PlayerGroup.builder()
                .name("Alpha Squad")
                .creatorId(creator.getId())
                .isFavorite(true)
                .members(Set.of(member1, member2))
                .build());

        var found = playerGroupRepository.findByIdWithMembers(group.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getMembers()).hasSize(2);
    }

    @Test
    void shouldCheckExistenceByCreatorIdAndNameIgnoreCase() {
        playerGroupRepository.save(PlayerGroup.builder()
                .name("Friday Crew")
                .creatorId(creator.getId())
                .isFavorite(false)
                .build());

        var existsSameCase = playerGroupRepository.existsByCreatorIdAndNameIgnoreCase(creator.getId(), "Friday Crew");
        var existsDiffCase = playerGroupRepository.existsByCreatorIdAndNameIgnoreCase(creator.getId(), "FRIDAY CREW");
        var existsOtherUser = playerGroupRepository.existsByCreatorIdAndNameIgnoreCase(UUID.randomUUID(), "Friday Crew");

        assertThat(existsSameCase).isTrue();
        assertThat(existsDiffCase).isTrue();
        assertThat(existsOtherUser).isFalse();
    }

    @Test
    void shouldCheckExistenceByCreatorIdAndNameIgnoreCaseAndIdNot() {
        var group = playerGroupRepository.save(PlayerGroup.builder()
                .name("Weekend League")
                .creatorId(creator.getId())
                .isFavorite(false)
                .build());

        var duplicateSameId = playerGroupRepository.existsByCreatorIdAndNameIgnoreCaseAndIdNot(creator.getId(), "Weekend League", group.getId());
        var duplicateDiffId = playerGroupRepository.existsByCreatorIdAndNameIgnoreCaseAndIdNot(creator.getId(), "WEEKEND LEAGUE", UUID.randomUUID());

        assertThat(duplicateSameId).isFalse();
        assertThat(duplicateDiffId).isTrue();
    }
}
