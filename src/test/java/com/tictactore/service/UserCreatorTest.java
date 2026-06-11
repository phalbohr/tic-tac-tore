package com.tictactore.service;

import com.tictactore.model.User;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserCreator Tests")
class UserCreatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserCreator userCreator;

    @Test
    @DisplayName("Valid User - should save and return user successfully")
    void createUser_shouldSaveAndReturnUserSuccessfully() {
        var user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .nickname("testuser")
                .build();
        when(userRepository.save(user)).thenReturn(user);

        var result = userCreator.createUser(user);

        assertThat(result).isSameAs(user);
    }
}
