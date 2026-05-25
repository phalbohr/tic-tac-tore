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
import java.util.HexFormat;
import java.util.UUID;

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

    @Transactional(readOnly = true)
    public User getProfile(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
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
                var existingUser = userRepository.findByEmail(email);
                if (existingUser.isPresent()) {
                    User u = existingUser.get();
                    if (u.getProviderId() == null || !u.getProviderId().equals(providerId)) {
                        throw new BadCredentialsException(ERR_EMAIL_COLLISION);
                    }
                    return u;
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
            int fallbackAttempts = 0;
            do {
                nickname = baseNickname + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                fallbackAttempts++;
            } while (userRepository.existsByNickname(nickname) && fallbackAttempts < MAX_NICKNAME_ATTEMPTS);
            
            if (fallbackAttempts >= MAX_NICKNAME_ATTEMPTS) {
                throw new IllegalStateException("Failed to generate unique nickname after fallback attempts");
            }
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
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    @Transactional
    public User updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getNickname() != null) {
            String sanitized = sanitizeNickname(request.getNickname());
            if (sanitized.isEmpty()) {
                throw new IllegalArgumentException("Nickname cannot be empty");
            }
            if (!sanitized.equals(user.getNickname())) {
                if (user.getLastNicknameUpdate() != null) {
                    Instant nextAllowedUpdate = user.getLastNicknameUpdate().plus(30, ChronoUnit.DAYS);
                    if (Instant.now(clock).isBefore(nextAllowedUpdate)) {
                        throw new IllegalArgumentException("Nickname can only be changed once every 30 days");
                    }
                }
                if (userRepository.existsByNickname(sanitized)) {
                    throw new IllegalArgumentException("Nickname already taken");
                }
                user.setNickname(sanitized);
                user.setLastNicknameUpdate(Instant.now(clock));
            }
        }

        if (request.getLanguage() != null) {
            String lang = request.getLanguage().toUpperCase();
            if (!lang.equals("EN") && !lang.equals("DE")) {
                throw new IllegalArgumentException("Language must be EN or DE");
            }
            user.setLanguage(lang);
        }

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Nickname already taken", e);
        }
    }
}
