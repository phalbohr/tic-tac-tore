package com.tictactore.service.tournament.impl;

import com.tictactore.dto.tournament.TournamentStandingResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.RegistrationStatus;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRegistrationRepository;
import com.tictactore.repository.TournamentRepository;
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

    private final TournamentRepository tournamentRepository;
    private final TournamentRegistrationRepository registrationRepository;
    private final TournamentMatchRepository tournamentMatchRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TournamentStandingResponse> calculateStandings(UUID tournamentId) {
        if (tournamentId == null) {
            throw new ResourceNotFoundException("Tournament", "null");
        }

        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId.toString()));

        List<TournamentRegistration> registrations = registrationRepository
                .findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED);
        if (registrations.isEmpty()) {
            return List.of();
        }

        List<TournamentMatch> allMatches = tournamentMatchRepository.findByTournamentId(tournamentId);

        Map<UUID, StandingAccumulator> stats = new HashMap<>();
        for (TournamentRegistration reg : registrations) {
            stats.put(reg.getId(), new StandingAccumulator(reg));
        }

        for (TournamentMatch tm : allMatches) {
            recordMatchParticipation(stats, tm);
        }

        List<TournamentMatch> completedMatches = allMatches.stream()
                .filter(m -> m.getStatus() == TournamentMatchStatus.COMPLETED)
                .toList();

        boolean isKnockout = tournament.getFormat() == TournamentFormat.CUP;

        for (TournamentMatch tournamentMatch : completedMatches) {
            boolean team1Won = tournamentMatch.getWinner() != null
                    && tournamentMatch.getParticipant1() != null
                    && tournamentMatch.getWinner().getId().equals(tournamentMatch.getParticipant1().getId());
            boolean team2Won = tournamentMatch.getWinner() != null
                    && tournamentMatch.getParticipant2() != null
                    && tournamentMatch.getWinner().getId().equals(tournamentMatch.getParticipant2().getId());

            int p1GamesWon = 0;
            int p1GamesLost = 0;
            int p2GamesWon = 0;
            int p2GamesLost = 0;

            Match coreMatch = tournamentMatch.getMatch();
            if (coreMatch != null && coreMatch.getGames() != null && !coreMatch.getGames().isEmpty()) {
                boolean side1IsTeamA = isSide1TeamA(tournamentMatch, coreMatch);
                for (Game game : coreMatch.getGames()) {
                    int teamAScore = game.getTeamAScore();
                    int teamBScore = game.getTeamBScore();
                    if (teamAScore > teamBScore) {
                        if (side1IsTeamA) {
                            p1GamesWon++;
                            p2GamesLost++;
                        } else {
                            p2GamesWon++;
                            p1GamesLost++;
                        }
                    } else if (teamBScore > teamAScore) {
                        if (side1IsTeamA) {
                            p1GamesLost++;
                            p2GamesWon++;
                        } else {
                            p2GamesLost++;
                            p1GamesWon++;
                        }
                    }
                }
            }

            if (tournamentMatch.getParticipant1() != null) {
                StandingAccumulator p1Acc = stats.get(tournamentMatch.getParticipant1().getId());
                if (p1Acc != null) {
                    p1Acc.recordMatch(team1Won, p1GamesWon, p1GamesLost);
                    if (isKnockout && !team1Won) {
                        p1Acc.eliminated = true;
                    }
                }
            }
            if (tournamentMatch.getParticipant1Partner() != null && !tournamentMatch.isParticipant1Stub()) {
                StandingAccumulator p1PartnerAcc = stats.get(tournamentMatch.getParticipant1Partner().getId());
                if (p1PartnerAcc != null) {
                    p1PartnerAcc.recordMatch(team1Won, p1GamesWon, p1GamesLost);
                    if (isKnockout && !team1Won) {
                        p1PartnerAcc.eliminated = true;
                    }
                }
            }

            if (tournamentMatch.getParticipant2() != null) {
                StandingAccumulator p2Acc = stats.get(tournamentMatch.getParticipant2().getId());
                if (p2Acc != null) {
                    p2Acc.recordMatch(team2Won, p2GamesWon, p2GamesLost);
                    if (isKnockout && !team2Won) {
                        p2Acc.eliminated = true;
                    }
                }
            }
            if (tournamentMatch.getParticipant2Partner() != null && !tournamentMatch.isParticipant2Stub()) {
                StandingAccumulator p2PartnerAcc = stats.get(tournamentMatch.getParticipant2Partner().getId());
                if (p2PartnerAcc != null) {
                    p2PartnerAcc.recordMatch(team2Won, p2GamesWon, p2GamesLost);
                    if (isKnockout && !team2Won) {
                        p2PartnerAcc.eliminated = true;
                    }
                }
            }
        }

        List<StandingAccumulator> sorted = new ArrayList<>(stats.values());
        if (isKnockout) {
            sorted.sort(Comparator
                    .comparing(StandingAccumulator::isEliminated)
                    .thenComparing(Comparator.comparingInt(StandingAccumulator::getDeepestRound).reversed())
                    .thenComparing(Comparator.comparingInt(StandingAccumulator::getWins).reversed())
                    .thenComparing(Comparator.comparingInt(StandingAccumulator::getGameDifference).reversed())
                    .thenComparing(StandingAccumulator::getNickname)
                    .thenComparing(StandingAccumulator::getRegistrationId));
        } else {
            sorted.sort(Comparator
                    .comparingInt(StandingAccumulator::getPoints).reversed()
                    .thenComparing(Comparator.comparingInt(StandingAccumulator::getWins).reversed())
                    .thenComparing(Comparator.comparingInt(StandingAccumulator::getGameDifference).reversed())
                    .thenComparingInt(StandingAccumulator::getMatchesPlayed)
                    .thenComparing(StandingAccumulator::getNickname)
                    .thenComparing(StandingAccumulator::getRegistrationId));
        }

        List<TournamentStandingResponse> result = new ArrayList<>(sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            result.add(sorted.get(i).toResponse(i + 1));
        }

        return result;
    }

    private void recordMatchParticipation(Map<UUID, StandingAccumulator> stats, TournamentMatch tm) {
        if (tm == null) {
            return;
        }
        int round = tm.getRound();
        if (tm.getParticipant1() != null) {
            StandingAccumulator acc = stats.get(tm.getParticipant1().getId());
            if (acc != null) {
                acc.updateDeepestRound(round);
            }
        }
        if (tm.getParticipant1Partner() != null && !tm.isParticipant1Stub()) {
            StandingAccumulator acc = stats.get(tm.getParticipant1Partner().getId());
            if (acc != null) {
                acc.updateDeepestRound(round);
            }
        }
        if (tm.getParticipant2() != null) {
            StandingAccumulator acc = stats.get(tm.getParticipant2().getId());
            if (acc != null) {
                acc.updateDeepestRound(round);
            }
        }
        if (tm.getParticipant2Partner() != null && !tm.isParticipant2Stub()) {
            StandingAccumulator acc = stats.get(tm.getParticipant2Partner().getId());
            if (acc != null) {
                acc.updateDeepestRound(round);
            }
        }
    }

    private boolean isSide1TeamA(TournamentMatch tournamentMatch, Match match) {
        if (tournamentMatch == null || match == null) {
            return true;
        }
        if (isRegistrationInTeamA(tournamentMatch.getParticipant1(), match)
                || isRegistrationInTeamA(tournamentMatch.getParticipant1Partner(), match)) {
            return true;
        }
        if (isRegistrationInTeamB(tournamentMatch.getParticipant1(), match)
                || isRegistrationInTeamB(tournamentMatch.getParticipant1Partner(), match)) {
            return false;
        }
        if (isRegistrationInTeamA(tournamentMatch.getParticipant2(), match)
                || isRegistrationInTeamA(tournamentMatch.getParticipant2Partner(), match)) {
            return false;
        }
        if (isRegistrationInTeamB(tournamentMatch.getParticipant2(), match)
                || isRegistrationInTeamB(tournamentMatch.getParticipant2Partner(), match)) {
            return true;
        }
        return true;
    }

    private boolean isRegistrationInTeamA(TournamentRegistration reg, Match match) {
        if (reg == null || match == null) {
            return false;
        }
        if (reg.getPlayer() != null && reg.getPlayer().getId() != null) {
            UUID playerId = reg.getPlayer().getId();
            if (playerId.equals(match.getTeamAAttackerId()) || playerId.equals(match.getTeamADefenderId())) {
                return true;
            }
        }
        if (reg.getPartner() != null && reg.getPartner().getId() != null) {
            UUID partnerId = reg.getPartner().getId();
            if (partnerId.equals(match.getTeamAAttackerId()) || partnerId.equals(match.getTeamADefenderId())) {
                return true;
            }
        }
        return false;
    }

    private boolean isRegistrationInTeamB(TournamentRegistration reg, Match match) {
        if (reg == null || match == null) {
            return false;
        }
        if (reg.getPlayer() != null && reg.getPlayer().getId() != null) {
            UUID playerId = reg.getPlayer().getId();
            if (playerId.equals(match.getTeamBAttackerId()) || playerId.equals(match.getTeamBDefenderId())) {
                return true;
            }
        }
        if (reg.getPartner() != null && reg.getPartner().getId() != null) {
            UUID partnerId = reg.getPartner().getId();
            if (partnerId.equals(match.getTeamBAttackerId()) || partnerId.equals(match.getTeamBDefenderId())) {
                return true;
            }
        }
        return false;
    }

    private static class StandingAccumulator {
        private final UUID registrationId;
        private final UUID userId;
        private final String nickname;
        private final String avatarUrl;
        private final UUID partnerUserId;
        private final String partnerNickname;
        private final String partnerAvatarUrl;
        private int deepestRound = 1;
        private int matchesPlayed;
        private int wins;
        private int losses;
        private int gamesWon;
        private int gamesLost;
        private int points;
        private boolean eliminated;

        StandingAccumulator(TournamentRegistration reg) {
            this.registrationId = reg.getId();
            this.userId = (reg.getPlayer() != null) ? reg.getPlayer().getId() : null;
            this.nickname = (reg.getPlayer() != null && reg.getPlayer().getNickname() != null && !reg.getPlayer().getNickname().isBlank())
                    ? reg.getPlayer().getNickname()
                    : "Anonymous";
            this.avatarUrl = (reg.getPlayer() != null) ? reg.getPlayer().getAvatar() : null;
            this.partnerUserId = (reg.getPartner() != null) ? reg.getPartner().getId() : null;
            this.partnerNickname = (reg.getPartner() != null)
                    ? (reg.getPartner().getNickname() != null && !reg.getPartner().getNickname().isBlank() ? reg.getPartner().getNickname() : "Anonymous")
                    : null;
            this.partnerAvatarUrl = (reg.getPartner() != null) ? reg.getPartner().getAvatar() : null;
        }

        void updateDeepestRound(int round) {
            if (round > this.deepestRound) {
                this.deepestRound = round;
            }
        }

        void recordMatch(boolean won, int gamesWon, int gamesLost) {
            this.matchesPlayed++;
            this.gamesWon += gamesWon;
            this.gamesLost += gamesLost;
            if (won) {
                this.wins++;
                this.points += 3;
            } else {
                this.losses++;
            }
        }

        UUID getRegistrationId() {
            return registrationId;
        }

        int getDeepestRound() {
            return deepestRound;
        }

        boolean isEliminated() {
            return eliminated;
        }

        int getPoints() {
            return points;
        }

        int getWins() {
            return wins;
        }

        int getGameDifference() {
            return gamesWon - gamesLost;
        }

        int getMatchesPlayed() {
            return matchesPlayed;
        }

        String getNickname() {
            return nickname;
        }

        TournamentStandingResponse toResponse(int rank) {
            return new TournamentStandingResponse(
                    registrationId,
                    userId,
                    nickname,
                    avatarUrl,
                    partnerUserId,
                    partnerNickname,
                    partnerAvatarUrl,
                    matchesPlayed,
                    wins,
                    losses,
                    gamesWon,
                    gamesLost,
                    getGameDifference(),
                    points,
                    eliminated,
                    rank
            );
        }
    }
}
