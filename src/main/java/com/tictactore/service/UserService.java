package com.tictactore.service;

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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ERR_EMAIL_COLLISION = "Email already registered with a different identity provider";
    private static final String AVATAR_API_URL = "https://api.dicebear.com/7.x/identicon/svg?seed=";
    private static final int MAX_NICKNAME_ATTEMPTS = 10;
    
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
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

    private User createNewUser(String email, String providerId) {
        try {
            var newUser = new User();
            newUser.setEmail(email);
            newUser.setProviderId(providerId);
            newUser.setNickname(generateUniqueNickname(email));
            newUser.setAvatar(generateDeterministicAvatar(email));
            return userRepository.save(newUser);
        } catch (DataIntegrityViolationException e) {
            return userRepository.findByEmail(email).orElseThrow();
        }
    }

    private String generateUniqueNickname(String email) {
        String baseNickname = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        String nickname = baseNickname;
        
        int attempts = 0;
        while (userRepository.existsByNickname(nickname) && attempts < MAX_NICKNAME_ATTEMPTS) {
            nickname = baseNickname + String.format("%04d", random.nextInt(10000));
            attempts++;
        }
        
        if (attempts >= MAX_NICKNAME_ATTEMPTS) {
            nickname = baseNickname + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        }
        
        return nickname;
    }

    private String generateDeterministicAvatar(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.getBytes(StandardCharsets.UTF_8));
            return AVATAR_API_URL + HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}
