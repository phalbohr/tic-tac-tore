package com.tictactore.controller;

import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import com.tictactore.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/dev/seed")
@RequiredArgsConstructor
public class DevTestController {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping
    public Map<String, Map<String, String>> seed() {
        Map<String, Map<String, String>> result = new HashMap<>();
        result.put("me", createUserData("ppolukhin@itemis.com", "Pavel"));
        result.put("opp1", createUserData("pavel.polukhin.de@gmail.com", "Account B"));
        result.put("alice", createUserData("alice@test.com", "Alice"));
        result.put("opp2", createUserData("opponent2@test.com", "Opponent 2"));
        return result;
    }

    private Map<String, String> createUserData(String email, String name) {
        User user = userRepository.findByEmail(email)
            .orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                u.setName(name);
                return userRepository.save(u);
            });
        
        Map<String, String> data = new HashMap<>();
        data.put("id", user.getId().toString());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("token", jwtService.generateToken(user.getEmail()));
        return data;
    }
}
