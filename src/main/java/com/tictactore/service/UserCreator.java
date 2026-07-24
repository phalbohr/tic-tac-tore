package com.tictactore.service;

import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UserCreator {

    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRED)
    public User createUser(User user) {
        return userRepository.save(user);
    }
}
