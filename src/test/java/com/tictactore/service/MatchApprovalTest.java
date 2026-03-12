package com.tictactore.service;

import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.Match;
import com.tictactore.model.MatchStatus;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Match Approval Unit Tests")
class MatchApprovalTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MatchService matchService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User teamAAttacker; // Creator
    private User teamADefender;
    private User teamBAttacker; // Opponent
    private User teamBDefender;
    private Match match;

    @BeforeEach
    void setUp() {
        teamAAttacker = createTestUser(UUID.randomUUID(), "a1@test.com", "A1");
        teamADefender = createTestUser(UUID.randomUUID(), "a2@test.com", "A2");
        teamBAttacker = createTestUser(UUID.randomUUID(), "b1@test.com", "B1");
        teamBDefender = createTestUser(UUID.randomUUID(), "b2@test.com", "B2");

        match = new Match();
        match.setId(UUID.randomUUID());
        match.setCreator(teamAAttacker);
        match.setTeamAAttacker(teamAAttacker);
        match.setTeamADefender(teamADefender);
        match.setTeamBAttacker(teamBAttacker);
        match.setTeamBDefender(teamBDefender);
        match.setStatus(MatchStatus.PENDING_APPROVAL);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private User createTestUser(UUID id, String email, String name) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        return user;
    }

    @Test
    @DisplayName("Should successfully approve match when current user is an opponent")
    void approveMatch_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("b1@test.com");
        when(userRepository.findByEmail("b1@test.com")).thenReturn(Optional.of(teamBAttacker));
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

        // Act
        matchService.approveMatch(match.getId());

        // Assert
        assertThat(match.getStatus()).isEqualTo(MatchStatus.CONFIRMED);
        verify(matchRepository).save(match);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when creator attempts to approve")
    void approveMatch_CreatorCannotApprove() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("a1@test.com");
        when(userRepository.findByEmail("a1@test.com")).thenReturn(Optional.of(teamAAttacker));
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

        // Act & Assert
        assertThatThrownBy(() -> matchService.approveMatch(match.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only an opponent can approve this match");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when teammate of creator attempts to approve")
    void approveMatch_TeammateCannotApprove() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("a2@test.com");
        when(userRepository.findByEmail("a2@test.com")).thenReturn(Optional.of(teamADefender));
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

        // Act & Assert
        assertThatThrownBy(() -> matchService.approveMatch(match.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only an opponent can approve this match");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when non-participant attempts to approve")
    void approveMatch_NonParticipantCannotApprove() {
        // Arrange
        User stranger = createTestUser(UUID.randomUUID(), "stranger@test.com", "Stranger");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("stranger@test.com");
        when(userRepository.findByEmail("stranger@test.com")).thenReturn(Optional.of(stranger));
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

        // Act & Assert
        assertThatThrownBy(() -> matchService.approveMatch(match.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User is not a participant in this match");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when match is not PENDING_APPROVAL")
    void approveMatch_InvalidStatus() {
        // Arrange
        match.setStatus(MatchStatus.CONFIRMED);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("b1@test.com");
        when(userRepository.findByEmail("b1@test.com")).thenReturn(Optional.of(teamBAttacker));
        when(matchRepository.findById(match.getId())).thenReturn(Optional.of(match));

        // Act & Assert
        assertThatThrownBy(() -> matchService.approveMatch(match.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only be approved if it is in PENDING_APPROVAL status");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when match is not found")
    void approveMatch_NotFound() {
        // Arrange
        UUID randomId = UUID.randomUUID();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("any@test.com");
        when(userRepository.findByEmail("any@test.com")).thenReturn(Optional.of(new User()));
        when(matchRepository.findById(randomId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> matchService.approveMatch(randomId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
