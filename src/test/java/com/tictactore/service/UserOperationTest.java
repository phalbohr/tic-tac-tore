package com.tictactore.service;

import com.tictactore.exception.UserNotFoundException;
import com.tictactore.exception.ValidationException;
import com.tictactore.model.PlayerGroup;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.model.User;
import com.tictactore.repository.PlayerGroupRepository;
import com.tictactore.repository.RuleConfigurationRepository;
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
import java.time.temporal.ChronoUnit;
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
    private PlayerGroupRepository playerGroupRepository;

    @Mock
    private RuleConfigurationRepository ruleConfigurationRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private UserOperation userOperation;

    @Test
    @DisplayName("Update Profile - should update nickname when cooldown passed")
    void updateProfile_shouldUpdateNickname_whenCooldownPassed() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setNickname("oldNickname");
        user.setLastNicknameUpdate(Instant.parse("2026-05-25T12:00:00Z").minus(31, ChronoUnit.DAYS));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("newNickname")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clock.instant()).thenReturn(Instant.parse("2026-05-25T12:00:00Z"));

        var updatedUser = userOperation.updateProfile(userId, "newNickname", null, null, null);

        assertThat(updatedUser.getNickname()).isEqualTo("newNickname");
        assertThat(updatedUser.getLastNicknameUpdate()).isEqualTo(Instant.parse("2026-05-25T12:00:00Z"));
    }

    @Test
    @DisplayName("Update Profile - should throw exception when cooldown not passed")
    void updateProfile_shouldThrowException_whenCooldownNotPassed() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setNickname("oldNickname");
        user.setLastNicknameUpdate(Instant.parse("2026-05-25T12:00:00Z").minus(15, ChronoUnit.DAYS));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(clock.instant()).thenReturn(Instant.parse("2026-05-25T12:00:00Z"));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, "newNickname", null, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Nickname can only be changed once every 30 days");
    }

    @Test
    @DisplayName("Update Profile - should sanitize nickname")
    void updateProfile_shouldSanitizeNickname() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setNickname("oldNickname");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("newNick123")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clock.instant()).thenReturn(Instant.parse("2026-05-25T12:00:00Z"));

        var updatedUser = userOperation.updateProfile(userId, "new_Nick-123!", null, null, null);

        assertThat(updatedUser.getNickname()).isEqualTo("newNick123");
    }

    @Test
    @DisplayName("Update Profile - should throw exception when nickname not unique")
    void updateProfile_shouldThrowException_whenNicknameNotUnique() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setNickname("oldNickname");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByNickname("takenNickname")).thenReturn(true);

        assertThatThrownBy(() -> userOperation.updateProfile(userId, "takenNickname", null, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Nickname already taken");
    }

    @Test
    @DisplayName("Update Profile - should throw exception on empty sanitized nickname")
    void updateProfile_shouldThrowException_onEmptySanitizedNickname() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setNickname("oldNickname");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, "!@#", null, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Nickname cannot be empty");
    }

    @Test
    @DisplayName("Update Profile - should throw exception on invalid language")
    void updateProfile_shouldThrowException_onInvalidLanguage() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, null, "FR", null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Language must be EN or DE");
    }

    @Test
    @DisplayName("Update Profile - should update language")
    void updateProfile_shouldUpdateLanguage() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updatedUser = userOperation.updateProfile(userId, null, "DE", null, null);

        assertThat(updatedUser.getLanguage()).isEqualTo("DE");
    }

    @Test
    @DisplayName("Update Profile - should catch DataIntegrityViolationException and throw ValidationException")
    void updateProfile_shouldCatchDataIntegrityViolationException() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, null, null, null, true))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Nickname already taken");
    }

    @Test
    @DisplayName("Update Profile - should throw UserNotFoundException when user not found")
    void updateProfile_shouldThrowUserNotFoundException() {
        var userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userOperation.updateProfile(userId, "newNick", null, null, null))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }


    @Test
    @DisplayName("Update Profile - should update avatar when avatar whitelisted")
    void updateProfile_shouldUpdateAvatar_whenAvatarWhitelisted() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setNickname("nickname");
        user.setAvatar("old-avatar");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updatedUser = userOperation.updateProfile(userId, null, null, "ball-classic", null);

        assertThat(updatedUser.getAvatar()).isEqualTo("ball-classic");
    }

    @Test
    @DisplayName("Update Profile - should throw exception when avatar is anonymous")
    void updateProfile_shouldThrowException_whenAvatarIsAnonymous() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setNickname("nickname");
        user.setAvatar("old-avatar");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, null, null, "anonymous", null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid avatar selection");
    }

    @Test
    @DisplayName("Update Profile - should throw exception when avatar is empty")
    void updateProfile_shouldThrowException_whenAvatarIsEmpty() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setNickname("nickname");
        user.setAvatar("old-avatar");
        user.setEmail("test@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, null, null, "", null))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid avatar selection");
    }

    @Test
    @DisplayName("Update Profile - should update tutorialCompleted when provided")
    void updateProfile_shouldUpdateTutorialCompleted_whenProvided() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setTutorialCompleted(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updatedUser = userOperation.updateProfile(userId, null, null, null, true);

        assertThat(updatedUser.isTutorialCompleted()).isTrue();
    }

    @Test
    @DisplayName("Update Profile - should update tutorialCompleted to false when explicitly provided")
    void updateProfile_shouldUpdateTutorialCompletedToFalse_whenProvided() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setTutorialCompleted(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updatedUser = userOperation.updateProfile(userId, null, null, null, false);

        assertThat(updatedUser.isTutorialCompleted()).isFalse();
    }

    @Test
    @DisplayName("Update Profile - should not modify tutorialCompleted when omitted from request")
    void updateProfile_shouldNotModifyTutorialCompleted_whenOmitted() {
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setTutorialCompleted(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updatedUser = userOperation.updateProfile(userId, null, null, null, null);

        assertThat(updatedUser.isTutorialCompleted()).isTrue();
    }

    @Test
    @DisplayName("Delete Account - should anonymize user data")
    void deleteAccount_shouldAnonymizeUserData() {
        var userId = UUID.randomUUID();
        var user = User.builder()
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
        var userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userOperation.deleteAccount(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Delete Account - should return early if already anonymous")
    void deleteAccount_shouldReturnEarlyIfAlreadyAnonymous() {
        var userId = UUID.randomUUID();
        var user = User.builder()
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
        var userId = UUID.randomUUID();
        var user = User.builder()
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
        var userId = UUID.randomUUID();
        var user = User.builder()
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

    @Test
    @DisplayName("Update Profile - should persist valid owned defaultGroupId and custom defaultRuleConfigurationId")
    void updateProfile_shouldPersistValidOwnedDefaults() {
        var userId = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        var ruleId = UUID.randomUUID();
        var user = User.builder().id(userId).nickname("nick").build();
        var group = PlayerGroup.builder().id(groupId).creatorId(userId).name("My Squad").build();
        var rule = RuleConfiguration.builder().id(ruleId).createdBy(userId).type(RuleConfigurationType.CUSTOM).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerGroupRepository.findByIdAndCreatorId(groupId, userId)).thenReturn(Optional.of(group));
        when(ruleConfigurationRepository.findById(ruleId)).thenReturn(Optional.of(rule));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = userOperation.updateProfile(userId, null, null, null, null, groupId, ruleId, false, false);

        assertThat(updated.getDefaultGroupId()).isEqualTo(groupId);
        assertThat(updated.getDefaultRuleConfigurationId()).isEqualTo(ruleId);
    }

    @Test
    @DisplayName("Update Profile - should persist PRESET defaultRuleConfigurationId regardless of creator")
    void updateProfile_shouldPersistPresetRuleConfiguration() {
        var userId = UUID.randomUUID();
        var presetId = UUID.randomUUID();
        var user = User.builder().id(userId).nickname("nick").build();
        var presetRule = RuleConfiguration.builder().id(presetId).createdBy(UUID.randomUUID()).type(RuleConfigurationType.PRESET).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ruleConfigurationRepository.findById(presetId)).thenReturn(Optional.of(presetRule));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = userOperation.updateProfile(userId, null, null, null, null, null, presetId, false, false);

        assertThat(updated.getDefaultRuleConfigurationId()).isEqualTo(presetId);
    }

    @Test
    @DisplayName("Update Profile - should throw IllegalArgumentException when defaultGroupId not owned by user")
    void updateProfile_shouldThrowWhenGroupNotOwned() {
        var userId = UUID.randomUUID();
        var foreignGroupId = UUID.randomUUID();
        var user = User.builder().id(userId).nickname("nick").build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(playerGroupRepository.findByIdAndCreatorId(foreignGroupId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userOperation.updateProfile(userId, null, null, null, null, foreignGroupId, null, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Selected player group does not exist or does not belong to the user");
    }

    @Test
    @DisplayName("Update Profile - should throw IllegalArgumentException when defaultRuleConfigurationId not owned and not preset")
    void updateProfile_shouldThrowWhenRuleNotOwnedAndNotPreset() {
        var userId = UUID.randomUUID();
        var foreignRuleId = UUID.randomUUID();
        var user = User.builder().id(userId).nickname("nick").build();
        var foreignCustomRule = RuleConfiguration.builder().id(foreignRuleId).createdBy(UUID.randomUUID()).type(RuleConfigurationType.CUSTOM).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ruleConfigurationRepository.findById(foreignRuleId)).thenReturn(Optional.of(foreignCustomRule));

        assertThatThrownBy(() -> userOperation.updateProfile(userId, null, null, null, null, null, foreignRuleId, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Selected rule configuration does not exist or is not accessible");
    }

    @Test
    @DisplayName("Update Profile - should clear defaultGroupId and defaultRuleConfigurationId when clear flags are true")
    void updateProfile_shouldClearDefaultsWhenFlagsTrue() {
        var userId = UUID.randomUUID();
        var user = User.builder()
                .id(userId)
                .nickname("nick")
                .defaultGroupId(UUID.randomUUID())
                .defaultRuleConfigurationId(UUID.randomUUID())
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = userOperation.updateProfile(userId, null, null, null, null, null, null, true, true);

        assertThat(updated.getDefaultGroupId()).isNull();
        assertThat(updated.getDefaultRuleConfigurationId()).isNull();
    }
}