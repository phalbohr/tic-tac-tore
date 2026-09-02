package com.tictactore.service.tournament.impl;

import com.tictactore.dto.tournament.TournamentStandingResponse;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRegistrationRepository;
import com.tictactore.service.tournament.TournamentStandingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TournamentStandingsServiceImpl implements TournamentStandingsService {

    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentMatchRepository tournamentMatchRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TournamentStandingResponse> calculateStandings(UUID tournamentId) {
        if (tournamentId == null) {
            return List.of();
        }

        List<TournamentRegistration> registrations = registrationRepository.findByTournamentId(tournamentId);
        if (registrations.isEmpty()) {
            return List.of();
        }

        List<TournamentMatch> completedMatches = tournamentMatchRepository
                .findByTournamentIdAndStatusIn(tournamentId, List.of(TournamentMatchStatus.COMPLETED));

        Map<UUID, StandingAccumulator> stats = new HashMap<>();
        for (TournamentRegistration reg : registrations) {
            stats.put(reg.getId(), new StandingAccumulator(reg));
        }

        for (TournamentMatch match : completedMatches) {
            boolean isKnockout = match.getTournament() != null
                    && match.getTournament().getFormat() == TournamentFormat.CUP;

            boolean team1Won = match.getWinner() != null
                    && match.getParticipant1() != null
                    && match.getWinner().getId().equals(match.getParticipant1().getId());
            boolean team2Won = match.getWinner() != null
                    && match.getParticipant2() != null
                    && match.getWinner().getId().equals(match.getParticipant2().getId());

            if (match.getParticipant1() != null) {
                StandingAccumulator p1Acc = stats.get(match.getParticipant1().getId());
                if (p1Acc != null) {
                    p1Acc.recordMatch(team1Won);
                    if (isKnockout && !team1Won) {
                        p1Acc.eliminated = true;
                    }
                }
            }
            if (match.getParticipant1Partner() != null) {
                if (!match.isParticipant1Stub()) {
                    StandingAccumulator p1PartnerAcc = stats.get(match.getParticipant1Partner().getId());
                    if (p1PartnerAcc != null) {
                        p1PartnerAcc.recordMatch(team1Won);
                        if (isKnockout && !team1Won) {
                            p1PartnerAcc.eliminated = true;
                        }
                    }
                }
            }

            if (match.getParticipant2() != null) {
                StandingAccumulator p2Acc = stats.get(match.getParticipant2().getId());
                if (p2Acc != null) {
                    p2Acc.recordMatch(team2Won);
                    if (isKnockout && !team2Won) {
                        p2Acc.eliminated = true;
                    }
                }
            }
            if (match.getParticipant2Partner() != null) {
                if (!match.isParticipant2Stub()) {
                    StandingAccumulator p2PartnerAcc = stats.get(match.getParticipant2Partner().getId());
                    if (p2PartnerAcc != null) {
                        p2PartnerAcc.recordMatch(team2Won);
                        if (isKnockout && !team2Won) {
                            p2PartnerAcc.eliminated = true;
                        }
                    }
                }
            }
        }

        List<StandingAccumulator> sorted = new ArrayList<>(stats.values());
        sorted.sort(Comparator
                .comparingInt(StandingAccumulator::getPoints).reversed()
                .thenComparingInt(StandingAccumulator::getWins).reversed()
                .thenComparingInt(StandingAccumulator::getMatchesPlayed)
                .thenComparing(StandingAccumulator::getNickname));

        return sorted.stream()
                .map(StandingAccumulator::toResponse)
                .toList();
    }

    private static class StandingAccumulator {
        private final UUID registrationId;
        private final UUID userId;
        private final String nickname;
        private int matchesPlayed;
        private int wins;
        private int losses;
        private int points;
        private boolean eliminated;

        StandingAccumulator(TournamentRegistration reg) {
            this.registrationId = reg.getId();
            this.userId = (reg.getPlayer() != null) ? reg.getPlayer().getId() : null;
            this.nickname = (reg.getPlayer() != null && reg.getPlayer().getNickname() != null)
                    ? reg.getPlayer().getNickname()
                    : "Player";
        }

        void recordMatch(boolean won) {
            this.matchesPlayed++;
            if (won) {
                this.wins++;
                this.points += 3;
            } else {
                this.losses++;
            }
        }

        int getPoints() {
            return points;
        }

        int getWins() {
            return wins;
        }

        int getMatchesPlayed() {
            return matchesPlayed;
        }

        String getNickname() {
            return nickname;
        }

        TournamentStandingResponse toResponse() {
            return new TournamentStandingResponse(
                    registrationId,
                    userId,
                    nickname,
                    matchesPlayed,
                    wins,
                    losses,
                    points,
                    eliminated
            );
        }
    }
}
