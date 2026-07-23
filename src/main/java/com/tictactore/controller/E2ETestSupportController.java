package com.tictactore.controller;

import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Profile({"e2e", "test"})
@RestController
@RequestMapping("/api/e2e")
@RequiredArgsConstructor
public class E2ETestSupportController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
