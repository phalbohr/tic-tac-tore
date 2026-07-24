package com.tictactore.service;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;

import com.tictactore.dto.UpdateProfileRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    private static final String EMAIL_NEW = "new@example.com";
    private static final String SUB_NEW = "google-sub-123";
    private static final String EMAIL_EXISTING = "existing@example.com";
    private static final String SUB_EXISTING = "google-sub-456";
    private static final String EMAIL_VICTIM = "victim@example.com";
    private static final String SUB_VICTIM = "google-sub-789";
    private static final String SUB_ATTACKER = "attacker-sub-999";
    private static final String ERR_PROVIDER_MISMATCH = "Email already registered with a different identity provider";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCreator userCreator;

    @Mock
    private ApplicationProperties properties;

    @Mock
    private ApplicationProperties.Avatar avatarConfig;

    @Mock
    private Clock clock;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getAvatar()).thenReturn(avatarConfig);
        lenient().when(avatarConfig.getApiUrl()).thenReturn("https://api.dicebear.com/7.x/identicon/svg?seed=");
        lenient().when(avatarConfig.getSalt()).thenReturn("default-avatar-salt-for-privacy");
        lenient().when(clock.instant()).thenReturn(Instant.parse("2026-05-25T12:00:00Z"));
        lenient().when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Test
    @DisplayName("Find or Create - should truncate long email prefix for nickname")
    void findOrCreate_shouldTruncateLongEmailPrefix() {
        String longEmail = "thisisaverylongemailprefixthatgoeswaybeyondsixtyfourcharacters@example.com";
        when(userRepository.findByEmail(longEmail)).thenReturn(Optional.empty());
        when(userCreator.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = userService.findOrCreate(longEmail, SUB_NEW);

        assertThat(user.getNickname().length()).isLessThanOrEqualTo(48);
        assertThat(user.getNickname()).startsWith("thisisaverylongemailprefixthatgoeswaybey");
    }

    @Test
    @DisplayName("Create User - should save and return new user when email not found")
    void findOrCreate_createsNewUser_whenEmailNotFound() {
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userCreator.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        assertThat(result.getEmail()).isEqualTo(EMAIL_NEW);
        assertThat(result.getNickname()).isEqualTo("new");
        assertThat(result.getProviderId()).isEqualTo(SUB_NEW);
        verify(userCreator).createUser(any(User.class));
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
        verify(userCreator, never()).createUser(any());
    }

    @Test
    @DisplayName("findOrCreate - should throw exception when email is null")
    void findOrCreate_shouldThrowException_whenEmailIsNull() {
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findOrCreate(null, "provider123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email cannot be null");
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
        
        verify(userCreator, never()).createUser(any());
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
        when(userCreator.createUser(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        var result = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        assertThat(result).isSameAs(existing);
        verify(userRepository, times(2)).findByEmail(EMAIL_NEW);
        verify(userCreator).createUser(any(User.class));
    }

    @Test
    @DisplayName("Nickname Generation - should extract alphanumeric email prefix for new users")
    void shouldExtractEmailPrefixForNickname() {
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userCreator.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        assertThat(user.getNickname()).isEqualTo("new");
    }

    @Test
    @DisplayName("Nickname Collision - should append random suffix when nickname exists")
    void shouldHandleNicknameCollision() {
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userRepository.existsByNickname("new")).thenReturn(true);
        when(userRepository.findExistingNicknames(anyList())).thenReturn(java.util.Collections.emptyList());
        when(userCreator.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        assertThat(user.getNickname()).startsWith("new");
        assertThat(user.getNickname()).hasSize(7);
    }

    @Test
    @DisplayName("Avatar Generation - should generate deterministic Dicebear URL using SHA-256")
    void shouldGenerateDeterministicAvatar() {
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userCreator.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        String expectedHash = "19d7f5455ecc3199ffa0f29a6755a288fceb6b88ec694e053c5aa24b4317771c";
        assertThat(user.getAvatar()).isEqualTo("https://api.dicebear.com/7.x/identicon/svg?seed=" + expectedHash);
    }

    @Test
    @DisplayName("Regression - should not overwrite existing user profile")
    void shouldNotOverwriteReturningUserProfile() {
        var existing = User.builder()
                .email(EMAIL_EXISTING)
                .nickname("custom_nick")
                .avatar("custom_avatar")
                .providerId(SUB_EXISTING)
                .build();
        when(userRepository.findByEmail(EMAIL_EXISTING)).thenReturn(Optional.of(existing));

        User user = userService.findOrCreate(EMAIL_EXISTING, SUB_EXISTING);

        assertThat(user.getNickname()).isEqualTo("custom_nick");
        assertThat(user.getAvatar()).isEqualTo("custom_avatar");
        verify(userCreator, never()).createUser(any());
    }



    @Test
    @DisplayName("Get Profile - should return user when found")
    void getProfile_returnsUser_whenFound() {
        var user = User.builder()
                .id(UUID.randomUUID())
                .email(EMAIL_EXISTING)
                .nickname("existing")
                .build();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        var result = userService.getProfile(user.getId());

        assertThat(result).isSameAs(user);
    }

    @Test
    @DisplayName("Get Profile - should throw Exception when not found")
    void getProfile_throwsException_whenNotFound() {
        var id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(id))
                .isInstanceOf(com.tictactore.exception.UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Nickname Generation - should strip non-alphanumeric characters")
    void shouldStripNonAlphanumericCharacters() {
        when(userRepository.findByEmail("test.user+123@example.com")).thenReturn(Optional.empty());
        when(userCreator.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = userService.findOrCreate("test.user+123@example.com", SUB_NEW);

        assertThat(user.getNickname()).isEqualTo("testuser123");
    }

    @Test
    @DisplayName("Nickname Collision - should use UUID fallback after max attempts")
    void shouldHandleNicknameCollisionExhaustion() {
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userRepository.existsByNickname("new")).thenReturn(true);
        when(userRepository.findExistingNicknames(anyList())).thenAnswer(inv -> {
            java.util.List<String> args = inv.getArgument(0);
            if (!args.isEmpty() && args.get(0).length() <= "new".length() + 4) {
                return new java.util.ArrayList<>(args);
            }
            return java.util.Collections.emptyList();
        });
        when(userCreator.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        assertThat(user.getNickname()).startsWith("new");
        assertThat(user.getNickname().length()).isEqualTo(11);
    }

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

        User updatedUser = userService.updateProfile(userId, request);

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

        assertThatThrownBy(() -> userService.updateProfile(userId, request))
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

        User updatedUser = userService.updateProfile(userId, request);

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

        assertThatThrownBy(() -> userService.updateProfile(userId, request))
                .isInstanceOf(com.tictactore.exception.ValidationException.class)
                .hasMessage("Nickname already taken");
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

        userService.deleteAccount(userId);

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

        assertThatThrownBy(() -> userService.deleteAccount(userId))
                .isInstanceOf(com.tictactore.exception.UserNotFoundException.class)
                .hasMessageContaining("User not found");
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

        userService.deleteAccount(userId);

        assertThat(user.getId()).isEqualTo(userId);
        verify(userRepository, never()).delete(any());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Delete Account - should throw IllegalStateException when optimistic locking failure occurs")
    void deleteAccount_shouldThrowIllegalStateException_onOptimisticLockingFailure() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@example.com")
                .nickname("player1")
                .avatar("https://avatar.url")
                .providerId("google-123")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException(User.class, userId))
                .when(userRepository).flush();

        assertThatThrownBy(() -> userService.deleteAccount(userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Account was concurrently modified during deletion. Please try again.");
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

        User updatedUser = userService.updateProfile(userId, request);

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

        assertThatThrownBy(() -> userService.updateProfile(userId, request))
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

        assertThatThrownBy(() -> userService.updateProfile(userId, request))
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

        User updatedUser = userService.updateProfile(userId, request);

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

        User updatedUser = userService.updateProfile(userId, request);

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

        User updatedUser = userService.updateProfile(userId, request);

        assertThat(updatedUser.isTutorialCompleted()).isTrue();
    }
}

