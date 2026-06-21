package com.tictactore.repository;

import com.tictactore.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

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
}
