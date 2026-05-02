package com.tictactore.service;

import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void findOrCreate_createsNewUser_whenEmailNotFound() {
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = userService.findOrCreate("new@example.com", "New User", "google-sub-123");

        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getName()).isEqualTo("New User");
        assertThat(result.getProviderId()).isEqualTo("google-sub-123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void findOrCreate_returnsExistingUser_whenEmailFound() {
        var existing = User.builder()
                .email("existing@example.com")
                .name("Existing User")
                .providerId("google-sub-456")
                .build();
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));

        var result = userService.findOrCreate("existing@example.com", "Existing User", "google-sub-456");

        assertThat(result).isSameAs(existing);
        verify(userRepository, never()).save(any());
    }
}
