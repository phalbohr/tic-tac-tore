package com.tictactore.service.operation;

import com.tictactore.annotation.Idempotent;
import com.tictactore.model.Match;
import com.tictactore.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MatchOperation {

    private final MatchRepository matchRepository;

    @Idempotent
    @Transactional
    public Match saveMatch(Match match) {
        return matchRepository.save(match);
    }

    @Idempotent
    @Transactional
    public Match confirmMatch(Match match, java.util.UUID opponentId) {
        match.confirmByOpponent(opponentId);
        return matchRepository.save(match);
    }

    @Idempotent
    @Transactional
    public Match rejectMatch(Match match, java.util.UUID opponentId, String reason, String customReason) {
        match.rejectByOpponent(opponentId, reason, customReason);
        return matchRepository.save(match);
    }
}
