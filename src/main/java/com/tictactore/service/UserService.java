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
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (tutorialCompleted != null) {
                user.setTutorialCompleted(tutorialCompleted);
                return userRepository.save(user);
            }
            return user;
        }

        User newUser = User.builder()
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
                .orElseThrow(() -> new com.tictactore.exception.ResourceNotFoundException("User not found"));
    }

    private User createNewUser(String email, String providerId) {
        int maxRetries = 3;
        DataIntegrityViolationException lastException = null;
        for (int i = 0; i < maxRetries; i++) {
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
        String prefix = email.split("@")[0];
        String baseNickname = sanitizeNickname(prefix);
        if (baseNickname.isEmpty()) {
            baseNickname = "user";
        }
        String nickname = baseNickname;

        int attempts = 0;
        while (userRepository.existsByNickname(nickname) && attempts < MAX_NICKNAME_ATTEMPTS) {
            nickname = baseNickname + String.format("%04d", random.nextInt(10000));
            attempts++;
        }

        if (attempts >= MAX_NICKNAME_ATTEMPTS) {
            List<String> candidates = new ArrayList<>();
            for (int i = 0; i < MAX_NICKNAME_ATTEMPTS; i++) {
                candidates.add(baseNickname + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8));
            }

            List<String> existing = userRepository.findExistingNicknames(candidates);

            String selectedFallback = null;
            for (String candidate : candidates) {
                if (!existing.contains(candidate)) {
                    selectedFallback = candidate;
                    break;
                }
            }
            
            if (selectedFallback == null) {
                throw new IllegalStateException("Failed to generate unique nickname after fallback attempts");
            }
            nickname = selectedFallback;
        }

        return nickname;
    }

    private String generateDeterministicAvatar(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = email.trim().toLowerCase() + properties.getAvatar().getSalt();
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
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
        return userOperation.updateProfile(userId, request);
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
