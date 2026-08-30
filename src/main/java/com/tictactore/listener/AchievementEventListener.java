package com.tictactore.listener;

import com.tictactore.event.MatchConfirmedEvent;
import com.tictactore.service.AchievementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AchievementEventListener {

    private final AchievementService achievementService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMatchConfirmed(MatchConfirmedEvent event) {
        if (event == null || event.matchId() == null) {
            return;
        }
        log.debug("Received MatchConfirmedEvent for match ID: {}", event.matchId());
        try {
            achievementService.evaluateMatchAchievements(event.matchId(), event.participantIds());
        } catch (Exception e) {
            log.error("Failed to evaluate achievements for confirmed match: {}", event.matchId(), e);
        }
    }
}
