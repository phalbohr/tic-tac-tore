package com.tictactore.service;

import com.tictactore.annotation.Idempotent;
import com.tictactore.exception.UserNotFoundException;
import com.tictactore.exception.ValidationException;
import com.tictactore.model.User;
import com.tictactore.model.RuleConfigurationType;
import com.tictactore.repository.PlayerGroupRepository;
import com.tictactore.repository.RuleConfigurationRepository;
import com.tictactore.repository.UserRepository;
import com.tictactore.validation.AvatarValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserOperation {

    private static final String ERR_USER_NOT_FOUND = "User not found";
    private static final String ERR_NICKNAME_EMPTY = "Nickname cannot be empty";
    private static final String ERR_NICKNAME_COOLDOWN = "Nickname can only be changed once every 30 days";
    private static final String ERR_NICKNAME_TAKEN = "Nickname already taken";
    private static final String ERR_LANGUAGE_INVALID = "Language must be EN or DE";
    private static final String ERR_AVATAR_INVALID = "Invalid avatar selection";
    private static final String ERR_USER_ID_NULL = "User ID cannot be null";
    private static final int DAYS_30 = 30;
    private static final String LANG_EN = "EN";
    private static final String LANG_DE = "DE";
    private static final String ANONYMOUS_AVATAR = "anonymous";
    private static final String DELETED_EMAIL_PREFIX = "deleted-";
    private static final String EX_PLAYER_PREFIX = "ex-player-";
    private static final String DELETED_EMAIL_SUFFIX = "@tic-tac-tore.invalid";

    private final UserRepository userRepository;
    private final PlayerGroupRepository playerGroupRepository;
    private final RuleConfigurationRepository ruleConfigurationRepository;
    private final Clock clock;

    private String sanitizeNickname(String nickname) {
        if (nickname == null) {
            return "";
        }
        return nickname.replaceAll("[^a-zA-Z0-9]", "");
    }

    @Idempotent
    @Transactional(propagation = Propagation.REQUIRED)
    public User updateProfile(UUID userId, String nickname, String language, String avatar, Boolean tutorialCompleted) {
        return updateProfile(userId, nickname, language, avatar, tutorialCompleted, null, null, false, false, null);
    }

    @Idempotent
    @Transactional(propagation = Propagation.REQUIRED)
    public User updateProfile(
            UUID userId,
            String nickname,
            String language,
            String avatar,
            Boolean tutorialCompleted,
            UUID defaultGroupId,
            UUID defaultRuleConfigurationId,
            Boolean clearDefaultGroup,
            Boolean clearDefaultRuleConfiguration
    ) {
        return updateProfile(userId, nickname, language, avatar, tutorialCompleted, defaultGroupId, defaultRuleConfigurationId, clearDefaultGroup, clearDefaultRuleConfiguration, null);
    }

    @Idempotent
    @Transactional(propagation = Propagation.REQUIRED)
    public User updateProfile(
            UUID userId,
            String nickname,
            String language,
            String avatar,
            Boolean tutorialCompleted,
            UUID defaultGroupId,
            UUID defaultRuleConfigurationId,
            Boolean clearDefaultGroup,
            Boolean clearDefaultRuleConfiguration,
            Boolean poolNotificationsEnabled
    ) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ERR_USER_NOT_FOUND));

        if (nickname != null && !nickname.trim().isEmpty()) {
            var sanitized = sanitizeNickname(nickname);
            if (sanitized.isEmpty()) {
                throw new ValidationException(ERR_NICKNAME_EMPTY);
            }
            if (!sanitized.equals(user.getNickname())) {
                if (user.getLastNicknameUpdate() != null) {
                    var nextAllowedUpdate = user.getLastNicknameUpdate().plus(DAYS_30, ChronoUnit.DAYS);
                    if (Instant.now(clock).isBefore(nextAllowedUpdate)) {
                        throw new ValidationException(ERR_NICKNAME_COOLDOWN);
                    }
                }
                if (userRepository.existsByNickname(sanitized)) {
                    throw new ValidationException(ERR_NICKNAME_TAKEN);
                }
                user.setNickname(sanitized);
                user.setLastNicknameUpdate(Instant.now(clock));
            }
        }

        if (language != null) {
            var lang = language.toUpperCase();
            if (!lang.equals(LANG_EN) && !lang.equals(LANG_DE)) {
                throw new ValidationException(ERR_LANGUAGE_INVALID);
            }
            user.setLanguage(lang);
        }

        if (avatar != null) {
            var avatarVal = avatar.trim();
            if (!AvatarValidator.ALLOWED_AVATARS.contains(avatarVal)) {
                throw new ValidationException(ERR_AVATAR_INVALID);
            }
            user.setAvatar(avatarVal);
        }

        if (tutorialCompleted != null) {
            user.setTutorialCompleted(tutorialCompleted);
        }

        if (defaultGroupId != null) {
            playerGroupRepository.findByIdAndCreatorId(defaultGroupId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Selected player group does not exist or does not belong to the user"));
            user.setDefaultGroupId(defaultGroupId);
        } else if (Boolean.TRUE.equals(clearDefaultGroup)) {
            user.setDefaultGroupId(null);
        }

        if (defaultRuleConfigurationId != null) {
            ruleConfigurationRepository.findById(defaultRuleConfigurationId)
                    .filter(r -> r.getType() == RuleConfigurationType.PRESET || userId.equals(r.getCreatedBy()))
                    .orElseThrow(() -> new IllegalArgumentException("Selected rule configuration does not exist or is not accessible"));
            user.setDefaultRuleConfigurationId(defaultRuleConfigurationId);
        } else if (Boolean.TRUE.equals(clearDefaultRuleConfiguration)) {
            user.setDefaultRuleConfigurationId(null);
        }

        if (poolNotificationsEnabled != null) {
            user.setPoolNotificationsEnabled(poolNotificationsEnabled);
        }

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException(ERR_NICKNAME_TAKEN);
        }
    }

    @Idempotent
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteAccount(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException(ERR_USER_ID_NULL);
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ERR_USER_NOT_FOUND));

        if (user.getProviderId() == null && ANONYMOUS_AVATAR.equals(user.getAvatar())) {
            return;
        }

        var uuid = UUID.randomUUID();
        user.setEmail(DELETED_EMAIL_PREFIX + uuid + DELETED_EMAIL_SUFFIX);
        user.setNickname(EX_PLAYER_PREFIX + uuid);
        user.setAvatar(ANONYMOUS_AVATAR);
        user.setProviderId(null);
        user.setLanguage(null);
        user.setLastNicknameUpdate(null);

        userRepository.flush();
    }
}