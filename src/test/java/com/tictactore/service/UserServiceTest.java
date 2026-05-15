package com.tictactore.service;

import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    private static final String EMAIL_NEW = "new@example.com";
    private static final String NAME_NEW = "New User";
    private static final String SUB_NEW = "google-sub-123";
    private static final String EMAIL_EXISTING = "existing@example.com";
    private static final String NAME_EXISTING = "Existing User";
    private static final String SUB_EXISTING = "google-sub-456";
    private static final String EMAIL_VICTIM = "victim@example.com";
    private static final String NAME_VICTIM = "Victim";
    private static final String SUB_VICTIM = "google-sub-789";
    private static final String NAME_ATTACKER = "Attacker";
    private static final String SUB_ATTACKER = "attacker-sub-999";
    private static final String ERR_PROVIDER_MISMATCH = "Email already registered with a different identity provider";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Create User - should save and return new user when email not found")
    void findOrCreate_createsNewUser_whenEmailNotFound() {
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = userService.findOrCreate(EMAIL_NEW, NAME_NEW, SUB_NEW);

        assertThat(result.getEmail()).isEqualTo(EMAIL_NEW);
        assertThat(result.getName()).isEqualTo(NAME_NEW);
        assertThat(result.getProviderId()).isEqualTo(SUB_NEW);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Find User - should return existing user when email and provider match")
    void findOrCreate_returnsExistingUser_whenEmailFoundAndProviderMatches() {
        var existing = User.builder()
                .email(EMAIL_EXISTING)
                .name(NAME_EXISTING)
                .providerId(SUB_EXISTING)
                .build();
        when(userRepository.findByEmail(EMAIL_EXISTING)).thenReturn(Optional.of(existing));

        var result = userService.findOrCreate(EMAIL_EXISTING, NAME_EXISTING, SUB_EXISTING);

        assertThat(result).isSameAs(existing);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Provider Mismatch - should throw BadCredentialsException to prevent account takeover")
    void findOrCreate_throwsException_whenEmailFoundButProviderMismatch() {
        var existing = User.builder()
                .email(EMAIL_VICTIM)
                .name(NAME_VICTIM)
                .providerId(SUB_VICTIM)
                .build();
        when(userRepository.findByEmail(EMAIL_VICTIM)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.findOrCreate(EMAIL_VICTIM, NAME_ATTACKER, SUB_ATTACKER))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining(ERR_PROVIDER_MISMATCH);
        
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Race Condition - should recover via secondary lookup when save throws DataIntegrityViolationException")
    void findOrCreate_retriesFind_whenSaveThrowsDataIntegrityViolation() {
        var existing = User.builder()
                .email(EMAIL_NEW)
                .name(NAME_NEW)
                .providerId(SUB_NEW)
                .build();

        when(userRepository.findByEmail(EMAIL_NEW))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        var result = userService.findOrCreate(EMAIL_NEW, NAME_NEW, SUB_NEW);

        assertThat(result).isSameAs(existing);
        verify(userRepository, times(2)).findByEmail(EMAIL_NEW);
        verify(userRepository).save(any(User.class));
    }

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
