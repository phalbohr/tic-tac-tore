package com.tictactore.service.tournament.impl;

import com.tictactore.dto.tournament.SeededParticipant;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.service.tournament.BracketGenerator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("cupBracketGenerator")
public class CupBracketGenerator implements BracketGenerator {

    @Override
    public List<TournamentMatch> generateBracket(Tournament tournament, List<SeededParticipant> seededParticipants) {
        if (seededParticipants == null || seededParticipants.isEmpty()) {
            return List.of();
        }

        int participantCount = seededParticipants.size();
        int bracketSize = calculateSmallestPowerOfTwo(participantCount);
        int totalRounds = 31 - Integer.numberOfLeadingZeros(bracketSize);

        Map<Integer, SeededParticipant> seedMap = seededParticipants.stream()
                .collect(Collectors.toMap(SeededParticipant::seed, Function.identity()));

        Map<String, TournamentMatch> matchMap = initializeMatchTree(tournament, totalRounds, bracketSize);
        linkMatchTree(matchMap, totalRounds, bracketSize);
        populateRoundOneAndHandleByes(matchMap, bracketSize, seedMap);
        updateSubsequentRoundsStatus(matchMap, totalRounds, bracketSize);

        List<TournamentMatch> allMatches = new ArrayList<>();
        for (int round = 1; round <= totalRounds; round++) {
            int matchesInRound = bracketSize / (1 << round);
            for (int matchOrder = 1; matchOrder <= matchesInRound; matchOrder++) {
                allMatches.add(matchMap.get(key(round, matchOrder)));
            }
        }

        return allMatches;
    }

    private int calculateSmallestPowerOfTwo(int n) {
        int power = 1;
        while (power < n) {
            power <<= 1;
        }
        return Math.max(2, power);
    }

    private Map<String, TournamentMatch> initializeMatchTree(Tournament tournament, int totalRounds, int bracketSize) {
        Map<String, TournamentMatch> map = new HashMap<>();
        for (int round = 1; round <= totalRounds; round++) {
            int matchesInRound = bracketSize / (1 << round);
            for (int matchOrder = 1; matchOrder <= matchesInRound; matchOrder++) {
                TournamentMatch match = TournamentMatch.builder()
                        .tournament(tournament)
                        .round(round)
                        .matchOrder(matchOrder)
                        .status(TournamentMatchStatus.PENDING)
                        .build();
                map.put(key(round, matchOrder), match);
            }
        }
        return map;
    }

    private void linkMatchTree(Map<String, TournamentMatch> matchMap, int totalRounds, int bracketSize) {
        for (int round = 1; round < totalRounds; round++) {
            int matchesInRound = bracketSize / (1 << round);
            for (int matchOrder = 1; matchOrder <= matchesInRound; matchOrder++) {
                TournamentMatch current = matchMap.get(key(round, matchOrder));
                int nextOrder = (matchOrder + 1) / 2;
                TournamentMatch next = matchMap.get(key(round + 1, nextOrder));
                current.setNextMatch(next);
            }
        }
    }

    private void populateRoundOneAndHandleByes(
            Map<String, TournamentMatch> matchMap,
            int bracketSize,
            Map<Integer, SeededParticipant> seedMap
    ) {
        List<Integer> seedOrder = generateSeedOrder(bracketSize);
        int roundOneMatches = bracketSize / 2;

        for (int matchOrder = 1; matchOrder <= roundOneMatches; matchOrder++) {
            int seed1 = seedOrder.get(2 * (matchOrder - 1));
            int seed2 = seedOrder.get(2 * (matchOrder - 1) + 1);

            SeededParticipant p1 = seedMap.get(seed1);
            SeededParticipant p2 = seedMap.get(seed2);

            TournamentMatch match = matchMap.get(key(1, matchOrder));
            match.setParticipant1(p1 != null ? p1.registration() : null);
            match.setSeed1(p1 != null ? p1.seed() : null);
            match.setParticipant2(p2 != null ? p2.registration() : null);
            match.setSeed2(p2 != null ? p2.seed() : null);

            if (p1 != null && p2 != null) {
                match.setStatus(TournamentMatchStatus.READY);
            } else if (p1 != null) {
                match.setStatus(TournamentMatchStatus.BYE);
                match.setWinner(p1.registration());
                advanceParticipant(match.getNextMatch(), matchOrder, p1);
            } else if (p2 != null) {
                match.setStatus(TournamentMatchStatus.BYE);
                match.setWinner(p2.registration());
                advanceParticipant(match.getNextMatch(), matchOrder, p2);
            } else {
                match.setStatus(TournamentMatchStatus.CANCELLED);
            }
        }
    }

    private void advanceParticipant(TournamentMatch nextMatch, int currentMatchOrder, SeededParticipant participant) {
        if (nextMatch == null || participant == null) {
            return;
        }
        if (currentMatchOrder % 2 == 1) {
            nextMatch.setParticipant1(participant.registration());
            nextMatch.setSeed1(participant.seed());
        } else {
            nextMatch.setParticipant2(participant.registration());
            nextMatch.setSeed2(participant.seed());
        }
    }

    private void updateSubsequentRoundsStatus(Map<String, TournamentMatch> matchMap, int totalRounds, int bracketSize) {
        for (int round = 2; round <= totalRounds; round++) {
            int matchesInRound = bracketSize / (1 << round);
            for (int matchOrder = 1; matchOrder <= matchesInRound; matchOrder++) {
                TournamentMatch match = matchMap.get(key(round, matchOrder));
                if (match.getParticipant1() != null && match.getParticipant2() != null) {
                    match.setStatus(TournamentMatchStatus.READY);
                } else {
                    match.setStatus(TournamentMatchStatus.PENDING);
                }
            }
        }
    }

    private List<Integer> generateSeedOrder(int size) {
        List<Integer> list = List.of(1, 2);
        while (list.size() < size) {
            int nextSize = list.size() * 2;
            List<Integer> nextList = new ArrayList<>(nextSize);
            for (Integer seed : list) {
                nextList.add(seed);
                nextList.add(nextSize + 1 - seed);
            }
            list = nextList;
        }
        return list;
    }

    private String key(int round, int matchOrder) {
        return round + "_" + matchOrder;
    }
}
