package com.tictactore.controller;

import com.tictactore.dto.ProfileDto;
import com.tictactore.model.User;
import com.tictactore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserController implements ProfileApi {

    private final UserService userService;

    @Override
    @GetMapping("/me")
    public ResponseEntity<ProfileDto> getMyProfile(@AuthenticationPrincipal User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            var user = userService.getProfile(principal.getId());
            var profile = ProfileDto.builder()
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .language(user.getLanguage())
                    .build();
                    
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
