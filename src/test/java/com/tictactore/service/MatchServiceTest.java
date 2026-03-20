package com.tictactore.service;

import com.tictactore.dto.GameRequest;
import com.tictactore.dto.MatchRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.mapper.MatchMapper;
import com.tictactore.model.Match;
import com.tictactore.model.MatchStatus;
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

    @Mock
    private MatchOperation matchOperation;

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
    private Match pendingMatch;

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

        pendingMatch = new Match();
        pendingMatch.setId(UUID.randomUUID());
        pendingMatch.setCreator(creator);
        pendingMatch.setTeamAAttacker(creator);
        pendingMatch.setTeamADefender(teammate);
        pendingMatch.setTeamBAttacker(opponent1);
        pendingMatch.setTeamBDefender(opponent2);
        pendingMatch.setStatus(MatchStatus.PENDING_APPROVAL);

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
    @DisplayName("Should successfully delegate match creation to MatchOperation")
    void createMatch_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("creator@test.com");
        when(userRepository.findByEmail("creator@test.com")).thenReturn(Optional.of(creator));
        
        var expectedResponse = MatchResponse.builder().id(UUID.randomUUID()).build();
        when(matchOperation.createMatch(eq(validRequest), eq(creator))).thenReturn(expectedResponse);

        // Act
        MatchResponse response = matchService.createMatch(validRequest);

        // Assert
        assertThat(response).isEqualTo(expectedResponse);
        verify(matchOperation).createMatch(validRequest, creator);
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

    @Test
    @DisplayName("Should successfully delegate approval to MatchOperation")
    void approveMatch_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("opponent1@test.com");
        when(userRepository.findByEmail("opponent1@test.com")).thenReturn(Optional.of(opponent1));
        when(matchRepository.findById(pendingMatch.getId())).thenReturn(Optional.of(pendingMatch));

        // Act
        matchService.approveMatch(pendingMatch.getId());

        // Assert
        verify(matchOperation).approveMatch(pendingMatch.getId());
    }

    @Test
    @DisplayName("Should successfully delegate rejection to MatchOperation")
    void rejectMatch_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("opponent2@test.com");
        when(userRepository.findByEmail("opponent2@test.com")).thenReturn(Optional.of(opponent2));
        when(matchRepository.findById(pendingMatch.getId())).thenReturn(Optional.of(pendingMatch));

        // Act
        matchService.rejectMatch(pendingMatch.getId());

        // Assert
        verify(matchOperation).rejectMatch(pendingMatch.getId());
    }
}
