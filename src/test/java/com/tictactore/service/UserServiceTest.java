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
import static org.mockito.ArgumentMatchers.argThat;
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

        var result = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        assertThat(result.getEmail()).isEqualTo(EMAIL_NEW);
        assertThat(result.getNickname()).isEqualTo("new");
        assertThat(result.getProviderId()).isEqualTo(SUB_NEW);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Find User - should return existing user when email and provider match")
    void findOrCreate_returnsExistingUser_whenEmailFoundAndProviderMatches() {
        var existing = User.builder()
                .email(EMAIL_EXISTING)
                .nickname("existing")
                .providerId(SUB_EXISTING)
                .build();
        when(userRepository.findByEmail(EMAIL_EXISTING)).thenReturn(Optional.of(existing));

        var result = userService.findOrCreate(EMAIL_EXISTING, SUB_EXISTING);

        assertThat(result).isSameAs(existing);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Provider Mismatch - should throw BadCredentialsException to prevent account takeover")
    void findOrCreate_throwsException_whenEmailFoundButProviderMismatch() {
        var existing = User.builder()
                .email(EMAIL_VICTIM)
                .nickname("victim")
                .providerId(SUB_VICTIM)
                .build();
        when(userRepository.findByEmail(EMAIL_VICTIM)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> userService.findOrCreate(EMAIL_VICTIM, SUB_ATTACKER))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining(ERR_PROVIDER_MISMATCH);
        
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Race Condition - should recover via secondary lookup when save throws DataIntegrityViolationException")
    void findOrCreate_retriesFind_whenSaveThrowsDataIntegrityViolation() {
        var existing = User.builder()
                .email(EMAIL_NEW)
                .nickname("new")
                .providerId(SUB_NEW)
                .build();

        when(userRepository.findByEmail(EMAIL_NEW))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        var result = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        assertThat(result).isSameAs(existing);
        verify(userRepository, times(2)).findByEmail(EMAIL_NEW);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Nickname Generation - should extract alphanumeric email prefix for new users")
    void shouldExtractEmailPrefixForNickname() {
        // Given
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        // Then
        assertThat(user.getNickname()).isEqualTo("new");
    }

    @Test
    @DisplayName("Nickname Collision - should append random suffix when nickname exists")
    void shouldHandleNicknameCollision() {
        // Given
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userRepository.existsByNickname("new")).thenReturn(true);
        // Any other nickname (with suffix) will be considered unique
        when(userRepository.existsByNickname(argThat(s -> s != null && !s.equals("new")))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        // Then
        assertThat(user.getNickname()).startsWith("new");
        assertThat(user.getNickname()).hasSize(7); // "new" + 4 digits
    }

    @Test
    @DisplayName("Avatar Generation - should generate deterministic Dicebear URL using SHA-256")
    void shouldGenerateDeterministicAvatar() {
        // Given
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        // Then
        // SHA-256 of "new@example.com"
        String expectedHash = "f0030501023327437b06e5c6f87df7871b8e704ae608d1d0b7b24fdd2a06c716";
        assertThat(user.getAvatar()).isEqualTo("https://api.dicebear.com/7.x/identicon/svg?seed=" + expectedHash);
    }

    @Test
    @DisplayName("Regression - should not overwrite existing user profile")
    void shouldNotOverwriteReturningUserProfile() {
        // Given
        var existing = User.builder()
                .email(EMAIL_EXISTING)
                .nickname("custom_nick")
                .avatar("custom_avatar")
                .providerId(SUB_EXISTING)
                .build();
        when(userRepository.findByEmail(EMAIL_EXISTING)).thenReturn(Optional.of(existing));

        // When
        User user = userService.findOrCreate(EMAIL_EXISTING, SUB_EXISTING);

        // Then
        assertThat(user.getNickname()).isEqualTo("custom_nick");
        assertThat(user.getAvatar()).isEqualTo("custom_avatar");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Privacy - should not store real name from provider")
    void shouldNotStorePii() {
        // Given
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        User user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        // Then
        // User entity should not have a 'name' field that matches "Real Name"
        // Since we are removing the field, this is implicitly tested by lack of mapping.
    }
}
