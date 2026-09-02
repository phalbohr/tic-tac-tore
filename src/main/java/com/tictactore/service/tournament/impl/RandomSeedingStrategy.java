package com.tictactore.service.tournament.impl;

import com.tictactore.dto.tournament.SeededParticipant;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.service.tournament.TournamentSeedingStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component("randomSeedingStrategy")
public class RandomSeedingStrategy implements TournamentSeedingStrategy {

    @Override
    public List<SeededParticipant> seed(Tournament tournament, List<TournamentRegistration> registrations) {
        if (registrations == null || registrations.isEmpty()) {
            return List.of();
        }

        List<TournamentRegistration> shuffled = new ArrayList<>(registrations);
        Collections.shuffle(shuffled);

        List<SeededParticipant> result = new ArrayList<>(shuffled.size());
        for (int i = 0; i < shuffled.size(); i++) {
            int seed = i + 1;
            TournamentRegistration reg = shuffled.get(i);
            reg.setSeed(seed);
            reg.setStrengthScore(0.0);
            result.add(new SeededParticipant(reg, seed, 0.0));
        }

        return result;
    }
}
