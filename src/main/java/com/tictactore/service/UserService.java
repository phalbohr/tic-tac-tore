package com.tictactore.service;

import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ERR_EMAIL_COLLISION = "Email already registered with a different identity provider";

    private final UserRepository userRepository;

    @Transactional
    public User findOrCreate(String email, String name, String providerId) {
        return userRepository.findByEmail(email)
                .map(user -> {
                    if (user.getProviderId() == null || !user.getProviderId().equals(providerId)) {
                        throw new BadCredentialsException(ERR_EMAIL_COLLISION);
                    }
                    return user;
                })
                .orElseGet(() -> {
                    try {
                        var newUser = new User();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        newUser.setProviderId(providerId);
                        return userRepository.save(newUser);
                    } catch (DataIntegrityViolationException e) {
                        return userRepository.findByEmail(email).orElseThrow();
                    }
                });
    }
}
