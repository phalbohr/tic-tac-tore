package com.tictactore.service.tournament.impl;

import com.tictactore.dto.tournament.SeededParticipant;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.service.tournament.BracketGenerator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component("randomPairingBracketGenerator")
public class RandomPairingBracketGenerator implements BracketGenerator {

    private static final int CANDIDATE_PERMUTATION_SAMPLES = 1000;
    private static final int DEFAULT_ROUND_COUNT = 3;

    @Override
    public List<TournamentMatch> generateBracket(Tournament tournament, List<SeededParticipant> seededParticipants) {
        if (seededParticipants == null || seededParticipants.size() < 4) {
            throw new IllegalArgumentException("2v2 random pairing requires at least 4 participants");
        }

        int participantCount = seededParticipants.size();
        long seed = (tournament.getId() != null)
                ? tournament.getId().getMostSignificantBits()
                : 42L;
        Random random = new Random(seed);

        int targetRounds = (tournament.getRoundCount() != null && tournament.getRoundCount() > 0)
                ? tournament.getRoundCount()
                : DEFAULT_ROUND_COUNT;

        if (participantCount % 4 == 0) {
            return generateDivisibleByFourSchedule(tournament, seededParticipants, targetRounds, random);
        }

        return generateGeneralSchedule(tournament, seededParticipants, targetRounds, random);
    }

    private List<TournamentMatch> generateDivisibleByFourSchedule(
            Tournament tournament,
            List<SeededParticipant> participants,
            int targetRounds,
            Random random
    ) {
        int n = participants.size();
        int matchesPerRound = n / 4;
        Map<String, Integer> partnerHistory = new HashMap<>();
        Map<String, Integer> opponentHistory = new HashMap<>();
        List<TournamentMatch> allMatches = new ArrayList<>();

        for (int round = 1; round <= targetRounds; round++) {
            List<SeededParticipant> bestPermutation = findBestPermutationForRound(
                    participants,
                    partnerHistory,
                    opponentHistory,
                    random
            );

            for (int m = 0; m < matchesPerRound; m++) {
                int base = m * 4;
                SeededParticipant p1 = bestPermutation.get(base);
                SeededParticipant p1Partner = bestPermutation.get(base + 1);
                SeededParticipant p2 = bestPermutation.get(base + 2);
                SeededParticipant p2Partner = bestPermutation.get(base + 3);

                recordPair(partnerHistory, p1.registration().getId(), p1Partner.registration().getId());
                recordPair(partnerHistory, p2.registration().getId(), p2Partner.registration().getId());
                recordOpponents(opponentHistory, p1, p1Partner, p2, p2Partner);

                TournamentMatch match = TournamentMatch.builder()
                        .tournament(tournament)
                        .round(round)
                        .matchOrder(m + 1)
                        .participant1(p1.registration())
                        .participant1Partner(p1Partner.registration())
                        .participant2(p2.registration())
                        .participant2Partner(p2Partner.registration())
                        .seed1(p1.seed())
                        .seed2(p2.seed())
                        .status(round == 1 ? TournamentMatchStatus.READY : TournamentMatchStatus.PENDING)
                        .build();

                allMatches.add(match);
            }
        }

        return allMatches;
    }

    private List<SeededParticipant> findBestPermutationForRound(
            List<SeededParticipant> participants,
            Map<String, Integer> partnerHistory,
            Map<String, Integer> opponentHistory,
            Random random
    ) {
        List<SeededParticipant> best = new ArrayList<>(participants);
        long bestCost = Long.MAX_VALUE;

        for (int i = 0; i < CANDIDATE_PERMUTATION_SAMPLES; i++) {
            List<SeededParticipant> candidate = new ArrayList<>(participants);
            Collections.shuffle(candidate, random);

            long cost = computePartitionCost(candidate, partnerHistory, opponentHistory);
            if (cost < bestCost) {
                bestCost = cost;
                best = candidate;
                if (cost == 0) {
                    break;
                }
            }
        }

        return best;
    }

    private long computePartitionCost(
            List<SeededParticipant> permutation,
            Map<String, Integer> partnerHistory,
            Map<String, Integer> opponentHistory
    ) {
        long cost = 0;
        int matches = permutation.size() / 4;
        for (int m = 0; m < matches; m++) {
            int base = m * 4;
            SeededParticipant p1 = permutation.get(base);
            SeededParticipant p1p = permutation.get(base + 1);
            SeededParticipant p2 = permutation.get(base + 2);
            SeededParticipant p2p = permutation.get(base + 3);

            int p1Rep = getPairCount(partnerHistory, p1.registration().getId(), p1p.registration().getId());
            int p2Rep = getPairCount(partnerHistory, p2.registration().getId(), p2p.registration().getId());
            cost += (long) (p1Rep + p2Rep) * 10000;

            cost += getOpponentCount(opponentHistory, p1, p2);
            cost += getOpponentCount(opponentHistory, p1, p2p);
            cost += getOpponentCount(opponentHistory, p1p, p2);
            cost += getOpponentCount(opponentHistory, p1p, p2p);
        }
        return cost;
    }

    private List<TournamentMatch> generateGeneralSchedule(
            Tournament tournament,
            List<SeededParticipant> participants,
            int targetRounds,
            Random random
    ) {
        int n = participants.size();
        int gcd = gcd(n, 4);
        int minMatchesPerPlayer = 4 / gcd;
        int multiplier = Math.max(1, (int) Math.round((double) targetRounds / minMatchesPerPlayer));
        int matchesPerPlayer = multiplier * minMatchesPerPlayer;
        int totalMatches = (n * matchesPerPlayer) / 4;

        Map<java.util.UUID, Integer> remaining = new HashMap<>();
        for (SeededParticipant p : participants) {
            remaining.put(p.registration().getId(), matchesPerPlayer);
        }

        Map<String, Integer> partnerHistory = new HashMap<>();
        Map<String, Integer> opponentHistory = new HashMap<>();
        List<TournamentMatch> matches = new ArrayList<>();

        for (int k = 0; k < totalMatches; k++) {
            int remainingMatches = totalMatches - k;
            List<SeededParticipant> sorted = new ArrayList<>(participants);
            sorted.sort(Comparator
                    .comparingInt((SeededParticipant p) -> remaining.get(p.registration().getId())).reversed()
                    .thenComparing(p -> p.registration().getId()));

            List<SeededParticipant> selected = new ArrayList<>();
            for (SeededParticipant p : sorted) {
                if (remaining.get(p.registration().getId()) == remainingMatches && selected.size() < 4) {
                    selected.add(p);
                }
            }

            for (SeededParticipant p : sorted) {
                if (selected.size() == 4) {
                    break;
                }
                if (!selected.contains(p) && remaining.get(p.registration().getId()) > 0) {
                    selected.add(p);
                }
            }

            for (SeededParticipant p : selected) {
                remaining.merge(p.registration().getId(), -1, Integer::sum);
            }

            int round = (totalMatches <= targetRounds) ? (k + 1) : (k * targetRounds / totalMatches) + 1;
            int matchOrder = 1;
            for (TournamentMatch existing : matches) {
                if (existing.getRound() == round) {
                    matchOrder++;
                }
            }

            SeededParticipant p1 = selected.get(0);
            SeededParticipant p1Partner = selected.get(1);
            SeededParticipant p2 = selected.get(2);
            SeededParticipant p2Partner = selected.get(3);

            recordPair(partnerHistory, p1.registration().getId(), p1Partner.registration().getId());
            recordPair(partnerHistory, p2.registration().getId(), p2Partner.registration().getId());
            recordOpponents(opponentHistory, p1, p1Partner, p2, p2Partner);

            matches.add(TournamentMatch.builder()
                    .tournament(tournament)
                    .round(round)
                    .matchOrder(matchOrder)
                    .participant1(p1.registration())
                    .participant1Partner(p1Partner.registration())
                    .participant2(p2.registration())
                    .participant2Partner(p2Partner.registration())
                    .seed1(p1.seed())
                    .seed2(p2.seed())
                    .status(round == 1 ? TournamentMatchStatus.READY : TournamentMatchStatus.PENDING)
                    .build());
        }

        return matches;
    }

    private int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    private void recordPair(Map<String, Integer> pairHistory, java.util.UUID id1, java.util.UUID id2) {
        String key = pairKey(id1, id2);
        pairHistory.merge(key, 1, Integer::sum);
    }

    private int getPairCount(Map<String, Integer> pairHistory, java.util.UUID id1, java.util.UUID id2) {
        return pairHistory.getOrDefault(pairKey(id1, id2), 0);
    }

    private void recordOpponents(
            Map<String, Integer> opponentHistory,
            SeededParticipant p1,
            SeededParticipant p1Partner,
            SeededParticipant p2,
            SeededParticipant p2Partner
    ) {
        recordPair(opponentHistory, p1.registration().getId(), p2.registration().getId());
        recordPair(opponentHistory, p1.registration().getId(), p2Partner.registration().getId());
        recordPair(opponentHistory, p1Partner.registration().getId(), p2.registration().getId());
        recordPair(opponentHistory, p1Partner.registration().getId(), p2Partner.registration().getId());
    }

    private int getOpponentCount(Map<String, Integer> opponentHistory, SeededParticipant a, SeededParticipant b) {
        return opponentHistory.getOrDefault(pairKey(a.registration().getId(), b.registration().getId()), 0);
    }

    private String pairKey(java.util.UUID id1, java.util.UUID id2) {
        return id1.compareTo(id2) < 0 ? id1 + ":" + id2 : id2 + ":" + id1;
    }
}
