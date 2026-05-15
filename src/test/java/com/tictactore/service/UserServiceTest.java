package com.tictactore.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {

    @Test
    @Disabled("[P0] ATDD Red Phase: should extract email prefix for nickname")
    void shouldExtractEmailPrefixForNickname() {
        // Given
        String email = "john.doe@gmail.com";
        // When
        // User user = userService.findOrCreate(..., email, ...);
        // Then
        // assertThat(user.getNickname()).isEqualTo("johndoe");
    }

    @Test
    @Disabled("[P0] ATDD Red Phase: should handle nickname collision with random suffix")
    void shouldHandleNicknameCollision() {
        // Mocks exist returning true up to 10 times
        // Asserts fallback works.
    }

    @Test
    @Disabled("[P0] ATDD Red Phase: should generate deterministic avatar using SHA-256")
    void shouldGenerateDeterministicAvatar() {
        // Assert Dicebear URL matches hash
    }

    @Test
    @Disabled("[P0] ATDD Red Phase: returning users do not have custom profile overwritten")
    void shouldNotOverwriteReturningUserProfile() {
        // Assert returning user keeps nickname
    }

    @Test
    @Disabled("[P1] ATDD Red Phase: should not store PII such as real name")
    void shouldNotStorePii() {
        // Assert name is not saved
    }
}
