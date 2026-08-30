package com.tictactore.repository;

import com.tictactore.model.ChallengeStatus;
import com.tictactore.model.MatchChallenge;
import com.tictactore.model.MatchType;
import com.tictactore.model.PlayerGroup;
import com.tictactore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@DisplayName("MatchChallengeRepository Data JPA Tests")
class MatchChallengeRepositoryTest {

    @Autowired
    private MatchChallengeRepository matchChallengeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerGroupRepository playerGroupRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User challenger;
    private User targetPlayer;
    private User groupMember;
    private PlayerGroup targetGroup;

    @BeforeEach
    void setUp() {
        challenger = userRepository.save(User.builder().email("challenger@example.com").nickname("Challenger").build());
        targetPlayer = userRepository.save(User.builder().email("target@example.com").nickname("Target").build());
        groupMember = userRepository.save(User.builder().email("member@example.com").nickname("Member").build());

        var group = PlayerGroup.builder()
                .name("Alpha Squad")
                .creatorId(targetPlayer.getId())
                .members(Set.of(targetPlayer, groupMember))
                .build();
        targetGroup = playerGroupRepository.save(group);
    }

    @Test
    void shouldSaveChallengeAndRetrieveByIdWithDetails() {
        var challenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetPlayer(targetPlayer)
                .matchType(MatchType.ONE_VS_ONE)
                .message("Ready for a rematch?")
                .status(ChallengeStatus.PENDING)
                .build();

        var saved = matchChallengeRepository.save(challenge);
        entityManager.flush();
        entityManager.clear();

        var found = matchChallengeRepository.findByIdWithDetails(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getChallenger().getId()).isEqualTo(challenger.getId());
        assertThat(found.get().getTargetPlayer().getId()).isEqualTo(targetPlayer.getId());
        assertThat(found.get().getMessage()).isEqualTo("Ready for a rematch?");
        assertThat(found.get().getStatus()).isEqualTo(ChallengeStatus.PENDING);
    }

    @Test
    void shouldFindIncomingChallengesForDirectPlayerAndGroupMember() {
        var directChallenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetPlayer(targetPlayer)
                .matchType(MatchType.ONE_VS_ONE)
                .status(ChallengeStatus.PENDING)
                .build();

        var groupChallenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetGroup(targetGroup)
                .matchType(MatchType.TWO_VS_TWO)
                .status(ChallengeStatus.PENDING)
                .build();

        var otherChallenge = MatchChallenge.builder()
                .challenger(targetPlayer)
                .targetPlayer(challenger)
                .matchType(MatchType.ONE_VS_ONE)
                .status(ChallengeStatus.PENDING)
                .build();

        matchChallengeRepository.save(directChallenge);
        matchChallengeRepository.save(groupChallenge);
        matchChallengeRepository.save(otherChallenge);
        entityManager.flush();
        entityManager.clear();

        var memberGroupIds = playerGroupRepository.findGroupIdsByMemberId(groupMember.getId());
        var incomingForMember = matchChallengeRepository.findIncomingChallenges(groupMember.getId(), memberGroupIds, ChallengeStatus.PENDING);

        var targetGroupIds = playerGroupRepository.findGroupIdsByMemberId(targetPlayer.getId());
        var incomingForTarget = matchChallengeRepository.findIncomingChallenges(targetPlayer.getId(), targetGroupIds, ChallengeStatus.PENDING);

        var noGroupUserIncoming = matchChallengeRepository.findIncomingChallenges(UUID.randomUUID(), List.of(), ChallengeStatus.PENDING);

        assertThat(incomingForMember).hasSize(1);
        assertThat(incomingForMember.get(0).getTargetGroup().getId()).isEqualTo(targetGroup.getId());

        assertThat(incomingForTarget).hasSize(2);
        assertThat(noGroupUserIncoming).isEmpty();
    }

    @Test
    void shouldFindByChallengerIdAndStatus() {
        var challenge1 = MatchChallenge.builder()
                .challenger(challenger)
                .targetPlayer(targetPlayer)
                .matchType(MatchType.ONE_VS_ONE)
                .status(ChallengeStatus.PENDING)
                .build();

        var challenge2 = MatchChallenge.builder()
                .challenger(challenger)
                .targetGroup(targetGroup)
                .matchType(MatchType.TWO_VS_TWO)
                .status(ChallengeStatus.ACCEPTED)
                .build();

        matchChallengeRepository.save(challenge1);
        matchChallengeRepository.save(challenge2);
        entityManager.flush();
        entityManager.clear();

        var pendingChallenges = matchChallengeRepository.findByChallengerIdAndStatus(challenger.getId(), ChallengeStatus.PENDING);
        var acceptedChallenges = matchChallengeRepository.findByChallengerIdAndStatus(challenger.getId(), ChallengeStatus.ACCEPTED);

        assertThat(pendingChallenges).hasSize(1);
        assertThat(pendingChallenges.get(0).getStatus()).isEqualTo(ChallengeStatus.PENDING);
        assertThat(acceptedChallenges).hasSize(1);
        assertThat(acceptedChallenges.get(0).getStatus()).isEqualTo(ChallengeStatus.ACCEPTED);
    }

    @Test
    void shouldCheckExistenceByPlayerAndGroup() {
        var playerChallenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetPlayer(targetPlayer)
                .matchType(MatchType.ONE_VS_ONE)
                .status(ChallengeStatus.PENDING)
                .build();

        var groupChallenge = MatchChallenge.builder()
                .challenger(challenger)
                .targetGroup(targetGroup)
                .matchType(MatchType.TWO_VS_TWO)
                .status(ChallengeStatus.PENDING)
                .build();

        matchChallengeRepository.save(playerChallenge);
        matchChallengeRepository.save(groupChallenge);
        entityManager.flush();

        var playerExistsDirect = matchChallengeRepository.existsPendingBetweenPlayers(
                challenger.getId(), targetPlayer.getId(), ChallengeStatus.PENDING);
        var playerExistsReverse = matchChallengeRepository.existsPendingBetweenPlayers(
                targetPlayer.getId(), challenger.getId(), ChallengeStatus.PENDING);
        var groupExists = matchChallengeRepository.existsByChallengerIdAndTargetGroupIdAndStatus(
                challenger.getId(), targetGroup.getId(), ChallengeStatus.PENDING);
        var playerNotExists = matchChallengeRepository.existsPendingBetweenPlayers(
                challenger.getId(), groupMember.getId(), ChallengeStatus.PENDING);

        assertThat(playerExistsDirect).isTrue();
        assertThat(playerExistsReverse).isTrue();
        assertThat(groupExists).isTrue();
        assertThat(playerNotExists).isFalse();
    }

    @Test
    void shouldEnforceCheckConstraintOnTarget() {
        var invalidChallenge = MatchChallenge.builder()
                .challenger(challenger)
                .matchType(MatchType.ONE_VS_ONE)
                .status(ChallengeStatus.PENDING)
                .build();

        matchChallengeRepository.save(invalidChallenge);

        assertThatThrownBy(() -> entityManager.flush())
                .isInstanceOf(jakarta.persistence.PersistenceException.class);
    }
}
