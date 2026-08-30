package com.tictactore.service;

import com.tictactore.event.MatchConfirmedEvent;
import com.tictactore.model.Match;
import com.tictactore.repository.MatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class MatchCooldownService {

    private final MatchRepository matchRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public MatchCooldownService(MatchRepository matchRepository, ApplicationEventPublisher eventPublisher) {
        this.matchRepository = matchRepository;
        this.eventPublisher = eventPublisher;
    }

    public MatchCooldownService(MatchRepository matchRepository) {
        this(matchRepository, null);
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void processExpiredCooldowns() {
        List<Match> expiredMatches = matchRepository.findByCooldownExpiresAtBeforeAndStatus(Instant.now(), Match.STATUS_PARTIALLY_CONFIRMED);
        int processed = 0;
        for (Match match : expiredMatches) {
            if (Match.STATUS_PARTIALLY_CONFIRMED.equals(match.getStatus()) && match.isCooldownExpired()) {
                try {
                    match.publishAfterCooldown();
                    Match saved = matchRepository.save(match);
                    if (eventPublisher != null) {
                        eventPublisher.publishEvent(new MatchConfirmedEvent(saved.getId(), saved.getParticipantIds()));
                    }
                    processed++;
                    log.info("Auto-published match {} after cooldown expiry", match.getId());
                } catch (Exception e) {
                    log.error("Failed to auto-publish match {} after cooldown expiry", match.getId(), e);
                }
            }
        }
        if (processed > 0) {
            log.info("Processed {} expired cooldowns", processed);
        }
    }
}
