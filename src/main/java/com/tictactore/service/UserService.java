package com.tictactore.service;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.tictactore.dto.UpdateProfileRequest;
import com.tictactore.exception.UserNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import com.tictactore.controller.UserMatchController.PlayerDto;
import com.tictactore.controller.UserMatchController.UserPreferencesDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ERR_EMAIL_COLLISION = "Email already registered with a different identity provider";
    private static final int MAX_NICKNAME_ATTEMPTS = 10;

    private final UserRepository userRepository;
    private final UserCreator userCreator;
    private final UserOperation userOperation;
    private final ApplicationProperties properties;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public User findOrCreate(String email, String providerId) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    if (user.getProviderId() == null || !user.getProviderId().equals(providerId)) {
                        throw new BadCredentialsException(ERR_EMAIL_COLLISION);
                    }
                    return user;
                })
                .orElseGet(() -> createNewUser(email, providerId));
    }

    @Transactional
    public User findOrCreateTestUser(String email, String nickname, Boolean tutorialCompleted) {
        var existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            var user = existingUser.get();
            if (tutorialCompleted != null) {
                user.setTutorialCompleted(tutorialCompleted);
                return userRepository.save(user);
            }
            return user;
        }

        var newUser = User.builder()
                .email(email)
                .nickname(nickname)
                .avatar(generateDeterministicAvatar(email))
                .language("EN")
                .tutorialCompleted(tutorialCompleted != null ? tutorialCompleted : false)
                .build();
        return userRepository.save(newUser);
    }

    @Transactional(readOnly = true)
    public User getProfile(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new com.tictactore.exception.UserNotFoundException("User not found"));
    }

    private User createNewUser(String email, String providerId) {
        var maxRetries = 3;
        DataIntegrityViolationException lastException = null;
        for (var i = 0; i < maxRetries; i++) {
            try {
                var newUser = new User();
                newUser.setEmail(email);
                newUser.setProviderId(providerId);
                newUser.setNickname(generateUniqueNickname(email));
                newUser.setAvatar(generateDeterministicAvatar(email));
                return userCreator.createUser(newUser);
            } catch (DataIntegrityViolationException e) {
                lastException = e;
                var existingUser = userRepository.findByEmail(email)
                        .map(u -> {
                            if (u.getProviderId() == null || !u.getProviderId().equals(providerId)) {
                                throw new BadCredentialsException(ERR_EMAIL_COLLISION);
                            }
                            return u;
                        })
                        .orElse(null);
                if (existingUser != null) {
                    return existingUser;
                }
                // If findByEmail is empty, it's a nickname collision. Retry to generate a new nickname.
            }
        }
        throw new IllegalStateException("Failed to create user after retries due to database constraints", lastException);
    }

    public String sanitizeNickname(String nickname) {
        if (nickname == null) {
            return "";
        }
        return nickname.replaceAll("[^a-zA-Z0-9]", "");
    }

    private String generateUniqueNickname(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        String prefix = email.split("@")[0];
        String baseNickname = sanitizeNickname(prefix);
        if (baseNickname.isEmpty()) {
            baseNickname = "user";
        }
        baseNickname = baseNickname.substring(0, Math.min(baseNickname.length(), 40));
        String nickname = baseNickname;

        if (!userRepository.existsByNickname(nickname)) {
            return nickname;
        }

        List<String> suffixCandidates = new ArrayList<>();
        for (int i = 0; i < MAX_NICKNAME_ATTEMPTS; i++) {
            suffixCandidates.add(baseNickname + String.format("%08d", random.nextInt(100_000_000)));
        }

        List<String> existingSuffixes = userRepository.findExistingNicknames(suffixCandidates);
        for (String candidate : suffixCandidates) {
            if (!existingSuffixes.contains(candidate)) {
                return candidate;
            }
        }

        List<String> fallbackCandidates = new ArrayList<>();
        for (int i = 0; i < MAX_NICKNAME_ATTEMPTS; i++) {
            fallbackCandidates.add(baseNickname + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8));
        }

        List<String> existingFallbacks = userRepository.findExistingNicknames(fallbackCandidates);
        for (String candidate : fallbackCandidates) {
            if (!existingFallbacks.contains(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException("Failed to generate unique nickname after fallback attempts");
    }

    private String generateDeterministicAvatar(String email) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            var input = email.trim().toLowerCase() + properties.getAvatar().getSalt();
            var hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return properties.getAvatar().getApiUrl() + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not found", e);
        }
    }

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public User updateProfile(UUID userId, UpdateProfileRequest request) {
        return userOperation.updateProfile(userId, request.getNickname(), request.getLanguage(), request.getAvatar(), request.getTutorialCompleted());
    }

    @Retryable(
            retryFor = {ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    public void deleteAccount(UUID userId) {
        userOperation.deleteAccount(userId);
    }
    public UserPreferencesDto getLastRuleSystem() {
        return new UserPreferencesDto("STANDARD");
    }

    public List<PlayerDto> getFrequentOpponents() {
        return List.of(
                new PlayerDto("550e8400-e29b-41d4-a716-446655440000", "Mock Player 1", generateDeterministicAvatar("mock1@example.com")),
                new PlayerDto("550e8400-e29b-41d4-a716-446655440001", "Mock Player 2", generateDeterministicAvatar("mock2@example.com")),
                new PlayerDto("550e8400-e29b-41d4-a716-446655440002", "Mock Player 3", generateDeterministicAvatar("mock3@example.com")),
                new PlayerDto("550e8400-e29b-41d4-a716-446655440003", "Mock Player 4", generateDeterministicAvatar("mock4@example.com"))
        );
    }
}
