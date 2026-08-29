package com.tictactore.repository;

import com.tictactore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NICKNAME = "testnick";
    private static final String NON_EXISTENT_EMAIL = "none@example.com";
    private static final String NON_EXISTENT_NICKNAME = "nonenick";

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("findByEmail - should return user when email exists")
    void findByEmail_ReturnsUser() {
        var user = User.builder()
                .email(TEST_EMAIL)
                .nickname(TEST_NICKNAME)
                .build();
        userRepository.save(user);

        var result = userRepository.findByEmail(TEST_EMAIL);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @DisplayName("findByEmail - should return empty when email does not exist")
    void findByEmail_ReturnsEmpty() {
        var result = userRepository.findByEmail(NON_EXISTENT_EMAIL);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByNickname - should return true when nickname exists")
    void existsByNickname_ReturnsTrue() {
        var user = User.builder()
                .email(TEST_EMAIL)
                .nickname(TEST_NICKNAME)
                .build();
        userRepository.save(user);

        var result = userRepository.existsByNickname(TEST_NICKNAME);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("existsByNickname - should return false when nickname does not exist")
    void existsByNickname_ReturnsFalse() {
        var result = userRepository.existsByNickname(NON_EXISTENT_NICKNAME);

        assertThat(result).isFalse();
    }



    @Test
    @DisplayName("Save User - should persist tutorialCompleted flag")
    void saveUser_PersistsTutorialCompletedFlag() {
        var user = User.builder()
                .email(TEST_EMAIL)
                .nickname(TEST_NICKNAME)
                .tutorialCompleted(true)
                .build();

        userRepository.save(user);

        var result = userRepository.findByEmail(TEST_EMAIL);

        assertThat(result).isPresent();
        assertThat(result.get().isTutorialCompleted()).isTrue();
    }

    @Test
    @DisplayName("Save User - should persist and retrieve defaultGroupId and defaultRuleConfigurationId")
    void saveUser_PersistsAndRetrievesDefaults() {
        var groupId = UUID.randomUUID();
        var ruleId = UUID.randomUUID();
        var user = User.builder()
                .email(TEST_EMAIL)
                .nickname(TEST_NICKNAME)
                .defaultGroupId(groupId)
                .defaultRuleConfigurationId(ruleId)
                .build();

        var savedUser = userRepository.save(user);
        var result = userRepository.findById(savedUser.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getDefaultGroupId()).isEqualTo(groupId);
        assertThat(result.get().getDefaultRuleConfigurationId()).isEqualTo(ruleId);
    }

    @Test
    @DisplayName("Update User - should allow clearing defaultGroupId and defaultRuleConfigurationId to null")
    void updateUser_ClearsDefaultsToNull() {
        var groupId = UUID.randomUUID();
        var ruleId = UUID.randomUUID();
        var user = userRepository.save(User.builder()
                .email(TEST_EMAIL)
                .nickname(TEST_NICKNAME)
                .defaultGroupId(groupId)
                .defaultRuleConfigurationId(ruleId)
                .build());

        user.setDefaultGroupId(null);
        user.setDefaultRuleConfigurationId(null);
        userRepository.save(user);
        var reloaded = userRepository.findById(user.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().getDefaultGroupId()).isNull();
        assertThat(reloaded.get().getDefaultRuleConfigurationId()).isNull();
    }

    @Test
    @DisplayName("Save User - should persist poolNotificationsEnabled flag")
    void saveUser_PersistsPoolNotificationsEnabledFlag() {
        var user = User.builder()
                .email(TEST_EMAIL)
                .nickname(TEST_NICKNAME)
                .poolNotificationsEnabled(false)
                .build();

        var savedUser = userRepository.save(user);
        var reloaded = userRepository.findById(savedUser.getId());

        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().isPoolNotificationsEnabled()).isFalse();
    }

    @Test
    @DisplayName("findByPoolNotificationsEnabledTrueAndIdNot - should return only users with notifications enabled excluding specified user")
    void findByPoolNotificationsEnabledTrueAndIdNot_ReturnsEligibleUsersOnly() {
        var creator = userRepository.save(User.builder()
                .email("creator@example.com")
                .nickname("creator")
                .poolNotificationsEnabled(true)
                .build());
        var eligibleUser = userRepository.save(User.builder()
                .email("eligible@example.com")
                .nickname("eligible")
                .poolNotificationsEnabled(true)
                .build());
        var disabledUser = userRepository.save(User.builder()
                .email("disabled@example.com")
                .nickname("disabled")
                .poolNotificationsEnabled(false)
                .build());

        var result = userRepository.findByPoolNotificationsEnabledTrueAndIdNot(creator.getId());

        assertThat(result)
                .extracting(User::getId)
                .containsExactly(eligibleUser.getId());
    }
}
