package com.tictactore.repository;

import com.tictactore.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("UserRepository Tests")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("findByEmail - should return user when user exists")
    public void findByEmail_shouldReturnUser_whenUserExists() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setProviderId("google-id");
        userRepository.save(user);

        // When
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        assertThat(result.get().getName()).isEqualTo("Test User");
        assertThat(result.get().getId()).isNotNull();
    }
}