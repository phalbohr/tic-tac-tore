package com.tictactore.controller;

import com.tictactore.service.E2ETestSupportService;
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

    private final E2ETestSupportService e2eTestSupportService;

    @GetMapping("/users")
    public List<com.tictactore.dto.E2EUserDto> getAllUsers() {
        return e2eTestSupportService.getAllUsers();
    }
}
