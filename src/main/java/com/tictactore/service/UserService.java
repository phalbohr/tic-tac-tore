package com.tictactore.service;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            nickname = baseNickname + String.format("%08d", random.nextInt(100_000_000));
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

    @Transactional
    public User updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.tictactore.exception.ResourceNotFoundException("User not found"));

        if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
            String sanitized = sanitizeNickname(request.getNickname());
            if (sanitized.isEmpty()) {
                throw new com.tictactore.exception.ValidationException("Nickname cannot be empty");
            }
            if (!sanitized.equals(user.getNickname())) {
                if (user.getLastNicknameUpdate() != null) {
                    Instant nextAllowedUpdate = user.getLastNicknameUpdate().plus(30, ChronoUnit.DAYS);
                    if (Instant.now(clock).isBefore(nextAllowedUpdate)) {
                        throw new com.tictactore.exception.ValidationException("Nickname can only be changed once every 30 days");
                    }
                }
                if (userRepository.existsByNickname(sanitized)) {
                    throw new com.tictactore.exception.ValidationException("Nickname already taken");
                }
                user.setNickname(sanitized);
                user.setLastNicknameUpdate(Instant.now(clock));
            }
        }

        if (request.getLanguage() != null) {
            String lang = request.getLanguage().toUpperCase();
            if (!lang.equals("EN") && !lang.equals("DE")) {
                throw new com.tictactore.exception.ValidationException("Language must be EN or DE");
            }
            user.setLanguage(lang);
        }

        if (request.getAvatar() != null) {
            String avatarVal = request.getAvatar().trim();
            if (!com.tictactore.validation.AvatarValidator.ALLOWED_AVATARS.contains(avatarVal)) {
                throw new com.tictactore.exception.ValidationException("Invalid avatar selection");
            }
            user.setAvatar(avatarVal);
        }

        if (request.getTutorialCompleted() != null) {
            user.setTutorialCompleted(request.getTutorialCompleted());
        }

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new com.tictactore.exception.ValidationException("Nickname already taken");
        }
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.tictactore.exception.ResourceNotFoundException("User not found"));

        if (user.getProviderId() == null && "anonymous".equals(user.getAvatar())) {
            return;
        }

        UUID uuid = java.util.UUID.randomUUID();
        user.setEmail("deleted-" + uuid + "@tic-tac-tore.invalid");
        user.setNickname("ex-player-" + uuid);
        user.setAvatar("anonymous");
        user.setProviderId(null);
        user.setLanguage(null);
        user.setLastNicknameUpdate(null);

        try {
            userRepository.flush();
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
            throw new IllegalStateException("Account was concurrently modified during deletion. Please try again.", e);
        }
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
