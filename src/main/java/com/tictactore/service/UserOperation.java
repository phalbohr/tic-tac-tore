package com.tictactore.service;

import com.tictactore.annotation.Idempotent;
import com.tictactore.dto.UpdateProfileRequest;
import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final Clock clock;

    public String sanitizeNickname(String nickname) {
        if (nickname == null) {
            return "";
        }
        return nickname.replaceAll("[^a-zA-Z0-9]", "");
    }

    @Idempotent
    @Transactional(propagation = Propagation.REQUIRED)
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

    @Idempotent
    @Transactional(propagation = Propagation.REQUIRED)
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

        userRepository.flush();
    }
}
