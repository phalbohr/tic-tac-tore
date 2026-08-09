package com.tictactore.service;

import com.tictactore.model.Match;
import com.tictactore.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("MatchCooldownService Integration Tests")
class MatchCooldownServiceIntegrationTest {

    @Autowired
    private MatchRepository matchRepository;

    @MockBean
    private org.redisson.api.RedissonClient redissonClient;

    @MockBean
    private TokenRevocationService tokenRevocationService;

    @Autowired
    private MatchCooldownService matchCooldownService;

    private UUID creatorId;
    private UUID opponentA;
    private UUID opponentB;
    private UUID defenderA;

    @BeforeEach
    void setUp() {
        creatorId = UUID.randomUUID();
        opponentA = UUID.randomUUID();
        opponentB = UUID.randomUUID();
        defenderA = UUID.randomUUID();
    }

    @Test
    @DisplayName("[P0] AC3: Should auto-publish expired PARTIALLY_CONFIRMED matches via repository query")
    @Transactional
    void shouldAutoPublishExpiredCooldowns_viaRepositoryQuery() {
        Match expiredMatch = Match.builder()
                .creatorId(creatorId)
                .teamAAttackerId(creatorId)
                .teamADefenderId(defenderA)
                .teamBAttackerId(opponentA)
                .teamBDefenderId(opponentB)
                .status(Match.STATUS_PARTIALLY_CONFIRMED)
                .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                .matchFormat(Match.MATCH_FORMAT_STANDARD)
                .confirmedByOpponentIds(opponentA.toString())
                .cooldownExpiresAt(Instant.now().minusSeconds(60))
                .createdAt(Instant.now())
                .build();

        matchRepository.save(expiredMatch);
        UUID savedId = expiredMatch.getId();

        matchCooldownService.processExpiredCooldowns();

        Match updated = matchRepository.findById(savedId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Match.STATUS_CONFIRMED);
        assertThat(updated.getCooldownExpiresAt()).isNull();
        assertThat(updated.getConfirmedAt()).isNotNull();
    }

    @Test
    @DisplayName("[P1] Should not transition matches with non-expired cooldowns")
    @Transactional
    void shouldNotTransition_whenCooldownNotExpired() {
        Match activeMatch = Match.builder()
                .creatorId(creatorId)
                .teamAAttackerId(creatorId)
                .teamADefenderId(defenderA)
                .teamBAttackerId(opponentA)
                .teamBDefenderId(opponentB)
                .status(Match.STATUS_PARTIALLY_CONFIRMED)
                .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                .matchFormat(Match.MATCH_FORMAT_STANDARD)
                .confirmedByOpponentIds(opponentA.toString())
                .cooldownExpiresAt(Instant.now().plusSeconds(3600))
                .createdAt(Instant.now())
                .build();

        matchRepository.save(activeMatch);
        UUID savedId = activeMatch.getId();

        matchCooldownService.processExpiredCooldowns();

        Match updated = matchRepository.findById(savedId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Match.STATUS_PARTIALLY_CONFIRMED);
        assertThat(updated.getCooldownExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("[P1] Should skip matches that are not PARTIALLY_CONFIRMED")
    @Transactional
    void shouldSkip_whenMatchNotPartiallyConfirmed() {
        Match confirmedMatch = Match.builder()
                .creatorId(creatorId)
                .teamAAttackerId(creatorId)
                .teamADefenderId(defenderA)
                .teamBAttackerId(opponentA)
                .teamBDefenderId(opponentB)
                .status(Match.STATUS_CONFIRMED)
                .entryMode(Match.ENTRY_MODE_PARTICIPANT)
                .matchFormat(Match.MATCH_FORMAT_STANDARD)
                .confirmedByOpponentIds(opponentA.toString() + "," + opponentB.toString())
                .cooldownExpiresAt(Instant.now().minusSeconds(60))
                .createdAt(Instant.now())
                .build();

        matchRepository.save(confirmedMatch);
        UUID savedId = confirmedMatch.getId();

        matchCooldownService.processExpiredCooldowns();

        Match updated = matchRepository.findById(savedId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Match.STATUS_CONFIRMED);
        assertThat(updated.getCooldownExpiresAt()).isNotNull();
    }

    @Test
    @DisplayName("[P1] Should handle empty result set gracefully")
    @Transactional
    void shouldHandleEmptyResultSet() {
        matchCooldownService.processExpiredCooldowns();

        List<Match> all = matchRepository.findAll();
        assertThat(all).isEmpty();
    }
}
