package com.tictactore.service;

import com.tictactore.config.ApplicationProperties;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
        for (int i = 0; i < maxRetries; i++) {
            try {
                var newUser = new User();
                newUser.setEmail(email);
                newUser.setProviderId(providerId);
                newUser.setNickname(generateUniqueNickname(email));
                newUser.setAvatar(generateDeterministicAvatar(email));
                return userCreator.createUser(newUser);
            } catch (DataIntegrityViolationException e) {
                var existingUser = userRepository.findByEmail(email);
                if (existingUser.isPresent()) {
                    return existingUser.get();
                }
                // If findByEmail is empty, it's a nickname collision. Retry to generate a new nickname.
            }
        }
        throw new IllegalStateException("Failed to create user after retries due to nickname collision");
    }

    private String generateUniqueNickname(String email) {
        String baseNickname = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
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
            do {
                nickname = baseNickname + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
            } while (userRepository.existsByNickname(nickname));
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
}
