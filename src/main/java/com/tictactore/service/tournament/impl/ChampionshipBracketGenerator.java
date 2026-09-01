package com.tictactore.service.tournament.impl;

import com.tictactore.dto.tournament.SeededParticipant;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.service.tournament.BracketGenerator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("championshipBracketGenerator")
public class ChampionshipBracketGenerator implements BracketGenerator {

    @Override
    public List<TournamentMatch> generateBracket(Tournament tournament, List<SeededParticipant> seededParticipants) {
        if (seededParticipants == null || seededParticipants.size() < 2) {
            return List.of();
        }

        int participantCount = seededParticipants.size();
        int totalSlots = (participantCount % 2 == 0) ? participantCount : participantCount + 1;

        SeededParticipant[] slots = new SeededParticipant[totalSlots];
        for (int i = 0; i < participantCount; i++) {
            slots[i] = seededParticipants.get(i);
        }

        int cycleRounds = totalSlots - 1;
        int targetRounds = tournament.getRoundCount() != null && tournament.getRoundCount() > 0
                ? tournament.getRoundCount()
                : cycleRounds;

        List<TournamentMatch> matches = new ArrayList<>();

        for (int round = 1; round <= targetRounds; round++) {
            int matchOrder = 1;
            for (int i = 0; i < totalSlots / 2; i++) {
                SeededParticipant p1 = slots[i];
                SeededParticipant p2 = slots[totalSlots - 1 - i];

                if (p1 != null && p2 != null) {
                    TournamentMatch match = TournamentMatch.builder()
                            .tournament(tournament)
                            .round(round)
                            .matchOrder(matchOrder++)
                            .participant1(p1.registration())
                            .participant2(p2.registration())
                            .seed1(p1.seed())
                            .seed2(p2.seed())
                            .status(round == 1 ? TournamentMatchStatus.READY : TournamentMatchStatus.PENDING)
                            .build();
                    matches.add(match);
                }
            }
            rotateSlots(slots);
        }

        return matches;
    }

    private void rotateSlots(SeededParticipant[] slots) {
        if (slots.length <= 2) {
            return;
        }
        SeededParticipant last = slots[slots.length - 1];
        for (int i = slots.length - 1; i > 1; i--) {
            slots[i] = slots[i - 1];
        }
        slots[1] = last;
    }
}
