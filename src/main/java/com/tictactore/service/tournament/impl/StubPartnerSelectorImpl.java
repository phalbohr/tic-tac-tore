package com.tictactore.service.tournament.impl;

import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.service.tournament.StubPartnerSelector;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class StubPartnerSelectorImpl implements StubPartnerSelector {

    private static final double DEFAULT_STRENGTH_SCORE = 1000.0;

    @Override
    public TournamentRegistration selectStubPartner(
            Tournament tournament,
            TournamentRegistration deletedRegistration,
            UUID matchPartnerRegistrationId,
            List<TournamentRegistration> candidatePool
    ) {
        if (deletedRegistration == null) {
            throw new IllegalArgumentException("Deleted registration cannot be null");
        }
        if (candidatePool == null || candidatePool.isEmpty()) {
            throw new IllegalStateException("No eligible stub partner candidates available for tournament "
                    + (tournament != null ? tournament.getId() : "null"));
        }

        UUID deletedRegId = deletedRegistration.getId();
        double targetStrength = deletedRegistration.getStrengthScore() != null
                ? deletedRegistration.getStrengthScore()
                : DEFAULT_STRENGTH_SCORE;

        List<TournamentRegistration> eligibleCandidates = candidatePool.stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getId() != null)
                .filter(c -> !c.getId().equals(deletedRegId))
                .filter(c -> matchPartnerRegistrationId == null || !c.getId().equals(matchPartnerRegistrationId))
                .toList();

        if (eligibleCandidates.isEmpty()) {
            throw new IllegalStateException("No eligible stub partner candidates available for tournament "
                    + (tournament != null ? tournament.getId() : "null"));
        }

        Comparator<TournamentRegistration> comparator = Comparator
                .comparingDouble((TournamentRegistration c) -> {
                    double score = c.getStrengthScore() != null ? c.getStrengthScore() : DEFAULT_STRENGTH_SCORE;
                    return Math.abs(score - targetStrength);
                })
                .thenComparing(TournamentRegistration::getId);

        return eligibleCandidates.stream()
                .min(comparator)
                .orElseThrow(() -> new IllegalStateException("No eligible stub partner candidates"));
    }
}
