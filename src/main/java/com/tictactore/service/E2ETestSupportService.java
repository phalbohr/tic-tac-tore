package com.tictactore.service;

import com.tictactore.dto.E2EUserDto;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Profile({"e2e", "test"})
@Service
@RequiredArgsConstructor
public class E2ETestSupportService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<E2EUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> E2EUserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .providerId(user.getProviderId())
                        .avatar(user.getAvatar())
                        .language(user.getLanguage())
                        .build())
                .toList();
    }
}
