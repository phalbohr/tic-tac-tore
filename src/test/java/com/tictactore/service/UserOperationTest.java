package com.tictactore.service;

import com.tictactore.dto.UpdateProfileRequest;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserOperation Tests")
class UserOperationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private UserOperation userOperation;

    @Test
    @DisplayName("Update Profile - should update nickname when cooldown passed")
    void updateProfile_shouldUpdateNickname_whenCooldownPassed() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setNickname("oldNickname");
        user.setLastNicknameUpdate(Instant.parse("2026-05-25T12:00:00Z").minus(31, java.time.temporal.ChronoUnit.DAYS));
        UpdateProfileRequest request = UpdateProfileRequest.builder().nickname("newNickname").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("newNickname")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clock.instant()).thenReturn(Instant.parse("2026-05-25T12:00:00Z"));

        User updatedUser = userOperation.updateProfile(userId, request);

        assertThat(updatedUser.getNickname()).isEqualTo("newNickname");
        assertThat(updatedUser.getLastNicknameUpdate()).isEqualTo(Instant.parse("2026-05-25T12:00:00Z"));
    }

    @Test
    @DisplayName("Update Profile - should throw exception when cooldown not passed")
    void updateProfile_shouldThrowException_whenCooldownNotPassed() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setNickname("oldNickname");
        user.setLastNicknameUpdate(Instant.parse("2026-05-25T12:00:00Z").minus(15, java.time.temporal.ChronoUnit.DAYS));
        UpdateProfileRequest request = UpdateProfileRequest.builder().nickname("newNickname").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(clock.instant()).thenReturn(Instant.parse("2026-05-25T12:00:00Z"));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, request))
                .isInstanceOf(com.tictactore.exception.ValidationException.class)
                .hasMessage("Nickname can only be changed once every 30 days");
    }

    @Test
    @DisplayName("Update Profile - should sanitize nickname")
    void updateProfile_shouldSanitizeNickname() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setNickname("oldNickname");
        UpdateProfileRequest request = UpdateProfileRequest.builder().nickname("new_Nick-123!").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("newNick123")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clock.instant()).thenReturn(Instant.parse("2026-05-25T12:00:00Z"));

        User updatedUser = userOperation.updateProfile(userId, request);

        assertThat(updatedUser.getNickname()).isEqualTo("newNick123");
    }

    @Test
    @DisplayName("Update Profile - should throw exception when nickname not unique")
    void updateProfile_shouldThrowException_whenNicknameNotUnique() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setNickname("oldNickname");
        UpdateProfileRequest request = UpdateProfileRequest.builder().nickname("takenNickname").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("takenNickname")).thenReturn(true);

        assertThatThrownBy(() -> userOperation.updateProfile(userId, request))
                .isInstanceOf(com.tictactore.exception.ValidationException.class)
                .hasMessage("Nickname already taken");
    }

    @Test
    @DisplayName("Update Profile - should throw exception on empty sanitized nickname")
    void updateProfile_shouldThrowException_onEmptySanitizedNickname() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setNickname("oldNickname");
        UpdateProfileRequest request = UpdateProfileRequest.builder().nickname("!@#").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, request))
                .isInstanceOf(com.tictactore.exception.ValidationException.class)
                .hasMessage("Nickname cannot be empty");
    }

    @Test
    @DisplayName("Update Profile - should throw exception on invalid language")
    void updateProfile_shouldThrowException_onInvalidLanguage() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        UpdateProfileRequest request = UpdateProfileRequest.builder().language("FR").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, request))
                .isInstanceOf(com.tictactore.exception.ValidationException.class)
                .hasMessage("Language must be EN or DE");
    }

    @Test
    @DisplayName("Update Profile - should update language")
    void updateProfile_shouldUpdateLanguage() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        UpdateProfileRequest request = UpdateProfileRequest.builder().language("DE").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userOperation.updateProfile(userId, request);

        assertThat(updatedUser.getLanguage()).isEqualTo("DE");
    }

    @Test
    @DisplayName("Update Profile - should catch DataIntegrityViolationException and throw ValidationException")
    void updateProfile_shouldCatchDataIntegrityViolationException() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        UpdateProfileRequest request = UpdateProfileRequest.builder().tutorialCompleted(true).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, request))
                .isInstanceOf(com.tictactore.exception.ValidationException.class)
                .hasMessage("Nickname already taken");
    }

    @Test
    @DisplayName("Update Profile - should throw ResourceNotFoundException when user not found")
    void updateProfile_shouldThrowResourceNotFoundException() {
        UUID userId = UUID.randomUUID();
        UpdateProfileRequest request = UpdateProfileRequest.builder().nickname("newNick").build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userOperation.updateProfile(userId, request))
                .isInstanceOf(com.tictactore.exception.ResourceNotFoundException.class)
                .hasMessage("User not found");
    }


    @Test
    @DisplayName("Update Profile - should update avatar when avatar whitelisted")
    void updateProfile_shouldUpdateAvatar_whenAvatarWhitelisted() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setNickname("nickname");
        user.setAvatar("old-avatar");
        UpdateProfileRequest request = UpdateProfileRequest.builder().avatar("ball-classic").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userOperation.updateProfile(userId, request);

        assertThat(updatedUser.getAvatar()).isEqualTo("ball-classic");
    }

    @Test
    @DisplayName("Update Profile - should throw exception when avatar is anonymous")
    void updateProfile_shouldThrowException_whenAvatarIsAnonymous() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setNickname("nickname");
        user.setAvatar("old-avatar");
        UpdateProfileRequest request = UpdateProfileRequest.builder().avatar("anonymous").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, request))
                .isInstanceOf(com.tictactore.exception.ValidationException.class)
                .hasMessage("Invalid avatar selection");
    }

    @Test
    @DisplayName("Update Profile - should throw exception when avatar is empty")
    void updateProfile_shouldThrowException_whenAvatarIsEmpty() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setNickname("nickname");
        user.setAvatar("old-avatar");
        user.setEmail("test@example.com");
        UpdateProfileRequest request = UpdateProfileRequest.builder().avatar("").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, request))
                .isInstanceOf(com.tictactore.exception.ValidationException.class)
                .hasMessage("Invalid avatar selection");
    }

    @Test
    @DisplayName("Update Profile - should update tutorialCompleted when provided")
    void updateProfile_shouldUpdateTutorialCompleted_whenProvided() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setTutorialCompleted(false);
        UpdateProfileRequest request = UpdateProfileRequest.builder().tutorialCompleted(true).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userOperation.updateProfile(userId, request);

        assertThat(updatedUser.isTutorialCompleted()).isTrue();
    }

    @Test
    @DisplayName("Update Profile - should update tutorialCompleted to false when explicitly provided")
    void updateProfile_shouldUpdateTutorialCompletedToFalse_whenProvided() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setTutorialCompleted(true);
        UpdateProfileRequest request = UpdateProfileRequest.builder().tutorialCompleted(false).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userOperation.updateProfile(userId, request);

        assertThat(updatedUser.isTutorialCompleted()).isFalse();
    }

    @Test
    @DisplayName("Update Profile - should not modify tutorialCompleted when omitted from request")
    void updateProfile_shouldNotModifyTutorialCompleted_whenOmitted() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        user.setTutorialCompleted(true);
        UpdateProfileRequest request = UpdateProfileRequest.builder().build(); // tutorialCompleted is null
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userOperation.updateProfile(userId, request);

        assertThat(updatedUser.isTutorialCompleted()).isTrue();
    }

    @Test
    @DisplayName("Delete Account - should anonymize user data")
    void deleteAccount_shouldAnonymizeUserData() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("player1")
                .avatar("https://avatar.url")
                .providerId("google-123")
                .language("RU")
                .lastNicknameUpdate(Instant.now())
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userOperation.deleteAccount(userId);

        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getEmail()).startsWith("deleted-").endsWith("@tic-tac-tore.invalid");
        assertThat(user.getNickname()).startsWith("ex-player-");
        assertThat(user.getAvatar()).isEqualTo("anonymous");
        assertThat(user.getProviderId()).isNull();
        assertThat(user.getLanguage()).isNull();
        assertThat(user.getLastNicknameUpdate()).isNull();

        verify(userRepository).flush();
    }

    @Test
    @DisplayName("Delete Account - should throw exception when user not found")
    void deleteAccount_shouldThrowException_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userOperation.deleteAccount(userId))
                .isInstanceOf(com.tictactore.exception.ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Delete Account - should return early if already anonymous")
    void deleteAccount_shouldReturnEarlyIfAlreadyAnonymous() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("deleted-abc@tic-tac-tore.invalid")
                .nickname("ex-player-abc")
                .avatar("anonymous")
                .providerId(null)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userOperation.deleteAccount(userId);

        verify(userRepository, never()).flush();
    }

    @Test
    @DisplayName("Delete Account - should throw IllegalArgumentException when userId is null")
    void deleteAccount_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> userOperation.deleteAccount(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User ID cannot be null");
    }

    @Test
    @DisplayName("Delete Account - should keep user ID intact and never call delete on repository to preserve references")
    void deleteAccount_shouldKeepUserIdIntactAndNeverCallDelete() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("player1")
                .avatar("https://avatar.url")
                .providerId("google-123")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userOperation.deleteAccount(userId);

        assertThat(user.getId()).isEqualTo(userId);
        verify(userRepository, never()).delete(any());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Delete Account - should propagate ObjectOptimisticLockingFailureException")
    void deleteAccount_shouldPropagateObjectOptimisticLockingFailureException() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("player1")
                .avatar("https://avatar.url")
                .providerId("google-123")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doThrow(new ObjectOptimisticLockingFailureException(User.class, userId))
                .when(userRepository).flush();

        assertThatThrownBy(() -> userOperation.deleteAccount(userId))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
