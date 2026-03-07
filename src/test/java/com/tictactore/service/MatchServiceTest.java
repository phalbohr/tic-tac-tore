package com.tictactore.service;

import com.tictactore.dto.GameRequest;
import com.tictactore.dto.MatchRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.mapper.MatchMapper;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchService Unit Tests")
class MatchServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private MatchMapper matchMapper;

    @InjectMocks
    private MatchService matchService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User creator;
    private User teammate;
    private User opponent1;
    private User opponent2;
    private MatchRequest validRequest;

    @BeforeEach
    void setUp() {
        creator = createTestUser(UUID.randomUUID(), "creator@test.com", "Creator");
        teammate = createTestUser(UUID.randomUUID(), "teammate@test.com", "Teammate");
        opponent1 = createTestUser(UUID.randomUUID(), "opponent1@test.com", "Opponent 1");
        opponent2 = createTestUser(UUID.randomUUID(), "opponent2@test.com", "Opponent 2");

        validRequest = new MatchRequest(
                creator.getId(), teammate.getId(),
                opponent1.getId(), opponent2.getId(),
                List.of(new GameRequest(10, 5))
        );

        SecurityContextHolder.setContext(securityContext);
    }

    private User createTestUser(UUID id, String email, String name) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    @Test
    @DisplayName("Should successfully create a match when all data is valid")
    void createMatch_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("creator@test.com");
        when(userRepository.findByEmail("creator@test.com")).thenReturn(Optional.of(creator));
        when(userRepository.findAllById(any())).thenReturn(List.of(creator, teammate, opponent1, opponent2));
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> {
            Match m = invocation.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        // Act
        MatchResponse response = matchService.createMatch(validRequest);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.creatorName()).isEqualTo("Creator");
        assertThat(response.games()).hasSize(1);
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when a player is not found")
    void createMatch_UserNotFound() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("creator@test.com");
        when(userRepository.findByEmail("creator@test.com")).thenReturn(Optional.of(creator));
        when(userRepository.findAllById(any())).thenReturn(List.of(creator, teammate, opponent1)); // One player missing

        // Act & Assert
        assertThatThrownBy(() -> matchService.createMatch(validRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creator is not a participant")
    void createMatch_CreatorNotParticipant() {
        // Arrange
        User nonParticipantCreator = createTestUser(UUID.randomUUID(), "other@test.com", "Other");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("other@test.com");
        when(userRepository.findByEmail("other@test.com")).thenReturn(Optional.of(nonParticipantCreator));

        // Act & Assert
        assertThatThrownBy(() -> matchService.createMatch(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("participant");
    }
}
