package com.tictactore.service;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;

import com.tictactore.dto.UpdateProfileRequest;
import com.tictactore.exception.UserNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
    private UserOperation userOperation;

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

        var user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        assertThat(user.getNickname()).isEqualTo("new");
    }

    @Test
    @DisplayName("Nickname Collision - should append random suffix when nickname exists")
    void shouldHandleNicknameCollision() {
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userRepository.existsByNickname("new")).thenReturn(true);
        when(userCreator.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        assertThat(user.getNickname()).startsWith("new");
        assertThat(user.getNickname()).hasSize(11);
    }

    @Test
    @DisplayName("Avatar Generation - should generate deterministic Dicebear URL using SHA-256")
    void shouldGenerateDeterministicAvatar() {
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userCreator.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        assertThat(user.getAvatar()).isEqualTo("player-blue-2");
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

        var user = userService.findOrCreate(EMAIL_EXISTING, SUB_EXISTING);

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
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Nickname Generation - should strip non-alphanumeric characters")
    void shouldStripNonAlphanumericCharacters() {
        when(userRepository.findByEmail("test.user+123@example.com")).thenReturn(Optional.empty());
        when(userCreator.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var user = userService.findOrCreate("test.user+123@example.com", SUB_NEW);

        assertThat(user.getNickname()).isEqualTo("testuser123");
    }

    @Test
    @DisplayName("Nickname Collision - should use UUID fallback after max attempts")
    void shouldHandleNicknameCollisionExhaustion() {
        when(userRepository.findByEmail(EMAIL_NEW)).thenReturn(Optional.empty());
        when(userRepository.existsByNickname(anyString())).thenReturn(true, true, true, true, true, true, true, true,
                true, true, true, false);
        when(userCreator.createUser(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var user = userService.findOrCreate(EMAIL_NEW, SUB_NEW);

        assertThat(user.getNickname()).startsWith("new");
        assertThat(user.getNickname().length()).isEqualTo(11);
    }

    @Test
    @DisplayName("Update Profile - should delegate to UserOperation")
    void updateProfile_shouldDelegateToUserOperation() {
        var userId = UUID.randomUUID();
        var groupId = UUID.randomUUID();
        var ruleId = UUID.randomUUID();
        var request = UpdateProfileRequest.builder()
                .nickname("newNickname")
                .language("DE")
                .avatar("ball-classic")
                .tutorialCompleted(true)
                .defaultGroupId(groupId)
                .defaultRuleConfigurationId(ruleId)
                .clearDefaultGroup(false)
                .clearDefaultRuleConfiguration(false)
                .build();
        var expectedUser = new User();
        expectedUser.setNickname("newNickname");
        when(userOperation.updateProfile(userId, "newNickname", "DE", "ball-classic", true, groupId, ruleId, false,
                false, null)).thenReturn(expectedUser);

        var actualUser = userService.updateProfile(userId, request);

        assertThat(actualUser).isSameAs(expectedUser);
        verify(userOperation).updateProfile(userId, "newNickname", "DE", "ball-classic", true, groupId, ruleId, false,
                false, null);
    }

    @Test
    @DisplayName("Delete Account - should delegate to UserOperation")
    void deleteAccount_shouldDelegateToUserOperation() {
        var userId = UUID.randomUUID();

        userService.deleteAccount(userId);

        verify(userOperation).deleteAccount(userId);
    }

    @Test
    @DisplayName("Search Active Users - should filter deleted accounts and match nickname case-insensitively")
    void searchActiveUsers_filtersDeletedAccountsAndMatchesNickname() {
        var activeUser = User.builder()
                .id(UUID.randomUUID())
                .email("active@example.com")
                .nickname("Alice")
                .avatar("avatar-1")
                .build();
        var deletedUser = User.builder()
                .id(UUID.randomUUID())
                .email("deleted-user@example.com")
                .nickname("DeletedPlayer")
                .avatar("avatar-2")
                .build();
        var exPlayer = User.builder()
                .id(UUID.randomUUID())
                .email("ex@example.com")
                .nickname("ex-player-1")
                .avatar("avatar-3")
                .build();

        when(userRepository.searchActiveUsers(eq("ali"), any(Pageable.class))).thenReturn(List.of(activeUser));

        var results = userService.searchActiveUsers("ali");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).nickname()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("Search Active Users - should return empty list for blank query")
    void searchActiveUsers_returnsEmptyListForBlankQuery() {
        var results = userService.searchActiveUsers("   ");

        assertThat(results).isEmpty();
        verify(userRepository, never()).searchActiveUsers(anyString(), any(Pageable.class));
    }
}
