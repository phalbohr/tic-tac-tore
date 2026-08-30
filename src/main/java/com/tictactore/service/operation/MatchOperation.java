package com.tictactore.service.operation;

import com.tictactore.annotation.Idempotent;
import com.tictactore.event.MatchConfirmedEvent;
import com.tictactore.model.Match;
import com.tictactore.repository.MatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class MatchOperation {

    private final MatchRepository matchRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public MatchOperation(MatchRepository matchRepository, ApplicationEventPublisher eventPublisher) {
        this.matchRepository = matchRepository;
        this.eventPublisher = eventPublisher;
    }

    public MatchOperation(MatchRepository matchRepository) {
        this(matchRepository, null);
    }

    @Idempotent
    @Transactional
    public Match saveMatch(Match match) {
        Match saved = matchRepository.save(match);
        if (Match.STATUS_CONFIRMED.equals(saved.getStatus()) && eventPublisher != null) {
            eventPublisher.publishEvent(new MatchConfirmedEvent(saved.getId(), saved.getParticipantIds()));
        }
        return saved;
    }

    @Idempotent
    @Transactional
    public Match confirmMatch(Match match, UUID opponentId) {
        match.confirmByOpponent(opponentId);
        Match saved = matchRepository.save(match);
        if (Match.STATUS_CONFIRMED.equals(saved.getStatus()) && eventPublisher != null) {
            eventPublisher.publishEvent(new MatchConfirmedEvent(saved.getId(), saved.getParticipantIds()));
        }
        return saved;
    }

    @Idempotent
    @Transactional
    public Match rejectMatch(UUID matchId, UUID opponentId, String reason, String customReason) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new com.tictactore.exception.ResourceNotFoundException("Match not found with ID: " + matchId));
        match.rejectByOpponent(opponentId, reason, customReason);
        return matchRepository.save(match);
    }

    @Idempotent
    @Transactional
    public void deleteMatch(UUID matchId, UUID userId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new com.tictactore.exception.ResourceNotFoundException("Match not found with ID: " + matchId));

        boolean isCreator = userId != null && userId.equals(match.getCreatorId());
        boolean isParticipant = userId != null && (userId.equals(match.getTeamAAttackerId())
                || userId.equals(match.getTeamADefenderId())
                || userId.equals(match.getTeamBAttackerId())
                || userId.equals(match.getTeamBDefenderId()));

        if (!isCreator && !isParticipant) {
            throw new com.tictactore.exception.UnauthorizedMatchActionException("User " + userId + " is not authorized to delete match " + matchId);
        }

        matchRepository.delete(match);
    }
}
