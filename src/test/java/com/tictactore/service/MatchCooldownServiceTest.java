package com.tictactore.service;

import com.tictactore.model.Match;
import com.tictactore.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchCooldownService Unit Tests")
class MatchCooldownServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private MatchCooldownService matchCooldownService;

    private UUID matchId;

    @BeforeEach
    void setUp() {
        matchId = UUID.randomUUID();
    }

    @Test
    @DisplayName("[P0] AC3: Should auto-publish PARTIALLY_CONFIRMED match with expired cooldown")
    void shouldAutoPublish_whenCooldownExpired() {
        Match expiredMatch = Match.builder()
                .id(matchId)
                .creatorId(UUID.randomUUID())
                .teamAAttackerId(UUID.randomUUID())
                .teamADefenderId(UUID.randomUUID())
                .teamBAttackerId(UUID.randomUUID())
                .teamBDefenderId(UUID.randomUUID())
                .status(Match.STATUS_PARTIALLY_CONFIRMED)
                .cooldownExpiresAt(Instant.now().minusSeconds(60))
                .build();

        when(matchRepository.findByCooldownExpiresAtBeforeAndStatus(any(Instant.class), eq(Match.STATUS_PARTIALLY_CONFIRMED))).thenReturn(List.of(expiredMatch));
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

        matchCooldownService.processExpiredCooldowns();

        assertThat(expiredMatch.getStatus()).isEqualTo(Match.STATUS_CONFIRMED);
        assertThat(expiredMatch.getCooldownExpiresAt()).isNull();
        assertThat(expiredMatch.getConfirmedAt()).isNotNull();
        verify(matchRepository).save(expiredMatch);
    }

    @Test
    @DisplayName("[P1] Should skip matches that are not PARTIALLY_CONFIRMED")
    void shouldSkip_whenMatchNotPartiallyConfirmed() {
        Match confirmedMatch = Match.builder()
                .id(matchId)
                .creatorId(UUID.randomUUID())
                .teamAAttackerId(UUID.randomUUID())
                .teamADefenderId(UUID.randomUUID())
                .teamBAttackerId(UUID.randomUUID())
                .teamBDefenderId(UUID.randomUUID())
                .status(Match.STATUS_CONFIRMED)
                .cooldownExpiresAt(Instant.now().minusSeconds(60))
                .build();

        when(matchRepository.findByCooldownExpiresAtBeforeAndStatus(any(Instant.class), eq(Match.STATUS_PARTIALLY_CONFIRMED))).thenReturn(List.of(confirmedMatch));

        matchCooldownService.processExpiredCooldowns();

        assertThat(confirmedMatch.getStatus()).isEqualTo(Match.STATUS_CONFIRMED);
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    @DisplayName("[P1] Should skip matches with non-expired cooldown")
    void shouldSkip_whenCooldownNotExpired() {
        Match activeCooldownMatch = Match.builder()
                .id(matchId)
                .creatorId(UUID.randomUUID())
                .teamAAttackerId(UUID.randomUUID())
                .teamADefenderId(UUID.randomUUID())
                .teamBAttackerId(UUID.randomUUID())
                .teamBDefenderId(UUID.randomUUID())
                .status(Match.STATUS_PARTIALLY_CONFIRMED)
                .cooldownExpiresAt(Instant.now().plusSeconds(60))
                .build();

        when(matchRepository.findByCooldownExpiresAtBeforeAndStatus(any(Instant.class), eq(Match.STATUS_PARTIALLY_CONFIRMED))).thenReturn(List.of(activeCooldownMatch));

        matchCooldownService.processExpiredCooldowns();

        assertThat(activeCooldownMatch.getStatus()).isEqualTo(Match.STATUS_PARTIALLY_CONFIRMED);
        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    @DisplayName("[P1] Should handle empty result set gracefully")
    void shouldHandleEmptyResultSet() {
        when(matchRepository.findByCooldownExpiresAtBeforeAndStatus(any(Instant.class), eq(Match.STATUS_PARTIALLY_CONFIRMED))).thenReturn(List.of());

        matchCooldownService.processExpiredCooldowns();

        verify(matchRepository, never()).save(any(Match.class));
    }

    @Test
    @DisplayName("[P1] Should continue processing when one match fails to auto-publish")
    void shouldContinue_whenOneMatchFails() {
        Match failingMatch = Match.builder()
                .id(matchId)
                .creatorId(UUID.randomUUID())
                .teamAAttackerId(UUID.randomUUID())
                .teamADefenderId(UUID.randomUUID())
                .teamBAttackerId(UUID.randomUUID())
                .teamBDefenderId(UUID.randomUUID())
                .status(Match.STATUS_PARTIALLY_CONFIRMED)
                .cooldownExpiresAt(Instant.now().minusSeconds(60))
                .build();

        Match succeedingMatch = Match.builder()
                .id(UUID.randomUUID())
                .creatorId(UUID.randomUUID())
                .teamAAttackerId(UUID.randomUUID())
                .teamADefenderId(UUID.randomUUID())
                .teamBAttackerId(UUID.randomUUID())
                .teamBDefenderId(UUID.randomUUID())
                .status(Match.STATUS_PARTIALLY_CONFIRMED)
                .cooldownExpiresAt(Instant.now().minusSeconds(60))
                .build();

        when(matchRepository.findByCooldownExpiresAtBeforeAndStatus(any(Instant.class), eq(Match.STATUS_PARTIALLY_CONFIRMED)))
                .thenReturn(List.of(failingMatch, succeedingMatch));
        when(matchRepository.save(any(Match.class))).thenAnswer(invocation -> invocation.getArgument(0));

        matchCooldownService.processExpiredCooldowns();

        assertThat(succeedingMatch.getStatus()).isEqualTo(Match.STATUS_CONFIRMED);
        verify(matchRepository).save(succeedingMatch);
    }
}
