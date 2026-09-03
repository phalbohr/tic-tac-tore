package com.tictactore.service.tournament.impl;

import com.tictactore.dto.TournamentMatchResponse;
import com.tictactore.dto.TournamentRegistrationResponse;
import com.tictactore.dto.tournament.TournamentStandingResponse;
import com.tictactore.event.TournamentCompletedEvent;
import com.tictactore.event.TournamentMatchCancelledEvent;
import com.tictactore.event.TournamentMatchStartedEvent;
import com.tictactore.exception.InvalidMatchStateException;
import com.tictactore.exception.ParticipantBusyException;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.exception.UnauthorizedMatchActionException;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.model.TournamentStatus;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRepository;
import com.tictactore.service.tournament.TournamentMatchService;
import com.tictactore.service.tournament.TournamentStandingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TournamentMatchServiceImpl implements TournamentMatchService {

    private final TournamentRepository tournamentRepository;
    private final TournamentMatchRepository tournamentMatchRepository;
    private final MatchRepository matchRepository;
    private final TournamentStandingsService tournamentStandingsService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public TournamentMatchResponse startMatch(UUID tournamentId, UUID tournamentMatchId, UUID currentUserId) {
        Tournament tournament = getTournament(tournamentId);
        validateTournamentInProgress(tournament);

        TournamentMatch match = getTournamentMatch(tournamentMatchId);
        validateMatchBelongsToTournament(match, tournamentId);
        validateUserAuthorizedForMatch(match, tournament, currentUserId);
        validateMatchCanBeStarted(match);
        validateParticipantsNotBusy(tournamentId, match);

        match.setStatus(TournamentMatchStatus.IN_PROGRESS);
        TournamentMatch savedMatch = tournamentMatchRepository.save(match);

        List<UUID> participantUserIds = extractParticipantUserIds(savedMatch);
        eventPublisher.publishEvent(new TournamentMatchStartedEvent(tournamentId, tournamentMatchId, participantUserIds));

        return mapToMatchResponse(savedMatch);
    }

    @Override
    public TournamentMatchResponse cancelMatch(UUID tournamentId, UUID tournamentMatchId, UUID currentUserId) {
        Tournament tournament = getTournament(tournamentId);
        validateTournamentInProgress(tournament);

        TournamentMatch match = getTournamentMatch(tournamentMatchId);
        validateMatchBelongsToTournament(match, tournamentId);
        validateUserAuthorizedForMatch(match, tournament, currentUserId);

        if (match.getStatus() != TournamentMatchStatus.IN_PROGRESS) {
            throw new InvalidMatchStateException("Only in-progress matches can be cancelled");
        }

        match.setStatus(TournamentMatchStatus.READY);
        TournamentMatch savedMatch = tournamentMatchRepository.save(match);

        eventPublisher.publishEvent(new TournamentMatchCancelledEvent(tournamentId, tournamentMatchId, currentUserId));

        return mapToMatchResponse(savedMatch);
    }

    @Override
    public void completeMatch(UUID tournamentMatchId, UUID matchId) {
        TournamentMatch tournamentMatch = getTournamentMatch(tournamentMatchId);
        if (matchId != null) {
            Match coreMatch = matchRepository.findById(matchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Match", matchId.toString()));
            tournamentMatch.setMatch(coreMatch);
        }

        tournamentMatch.setStatus(TournamentMatchStatus.COMPLETED);
        TournamentRegistration winner = determineWinner(tournamentMatch);
        tournamentMatch.setWinner(winner);

        if (tournamentMatch.getTournament().getFormat() == TournamentFormat.CUP && tournamentMatch.getNextMatch() != null && winner != null) {
            advanceWinnerInCup(tournamentMatch, winner);
        }

        tournamentMatchRepository.save(tournamentMatch);
        tournamentStandingsService.calculateStandings(tournamentMatch.getTournament().getId());
        checkAndCompleteTournament(tournamentMatch);
    }

    private void checkAndCompleteTournament(TournamentMatch completedMatch) {
        Tournament tournament = completedMatch.getTournament();
        if (tournament == null || tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            return;
        }

        boolean isCompleted = false;
        UUID winnerRegistrationId = null;

        if (tournament.getFormat() == TournamentFormat.CUP) {
            if (completedMatch.getNextMatch() == null) {
                isCompleted = true;
                winnerRegistrationId = completedMatch.getWinner() != null ? completedMatch.getWinner().getId() : null;
            }
        } else {
            List<TournamentMatch> allMatches = tournamentMatchRepository.findByTournamentId(tournament.getId());
            boolean allMatchesConcluded = !allMatches.isEmpty() && allMatches.stream().allMatch(m ->
                    m.getStatus() == TournamentMatchStatus.COMPLETED
                            || m.getStatus() == TournamentMatchStatus.BYE
                            || m.getStatus() == TournamentMatchStatus.CANCELLED);

            if (allMatchesConcluded) {
                isCompleted = true;
                List<TournamentStandingResponse> standings =
                        tournamentStandingsService.calculateStandings(tournament.getId());
                if (!standings.isEmpty()) {
                    winnerRegistrationId = standings.get(0).registrationId();
                }
            }
        }

        if (isCompleted) {
            tournament.setStatus(TournamentStatus.COMPLETED);
            tournamentRepository.save(tournament);
            eventPublisher.publishEvent(new TournamentCompletedEvent(tournament.getId(), winnerRegistrationId, Instant.now()));
        }
    }

    private Tournament getTournament(UUID tournamentId) {
        return tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId.toString()));
    }

    private TournamentMatch getTournamentMatch(UUID tournamentMatchId) {
        return tournamentMatchRepository.findById(tournamentMatchId)
                .orElseThrow(() -> new ResourceNotFoundException("TournamentMatch", tournamentMatchId.toString()));
    }

    private void validateTournamentInProgress(Tournament tournament) {
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS) {
            throw new InvalidMatchStateException("Tournament is not in progress");
        }
    }

    private void validateMatchBelongsToTournament(TournamentMatch match, UUID tournamentId) {
        if (!Objects.equals(match.getTournament().getId(), tournamentId)) {
            throw new InvalidMatchStateException("Match does not belong to the tournament");
        }
    }

    private void validateUserAuthorizedForMatch(TournamentMatch match, Tournament tournament, UUID userId) {
        if (userId == null) {
            throw new UnauthorizedMatchActionException("User ID cannot be null");
        }

        boolean isCreator = tournament.getCreator() != null && userId.equals(tournament.getCreator().getId());
        boolean isParticipant = isUserParticipantInMatch(match, userId);

        if (!isCreator && !isParticipant) {
            throw new UnauthorizedMatchActionException("User " + userId + " is not authorized to act on this tournament match");
        }
    }

    private boolean isUserParticipantInMatch(TournamentMatch match, UUID userId) {
        if (userId == null) return false;
        return isRegistrationUser(match.getParticipant1(), userId)
                || isRegistrationUser(match.getParticipant1Partner(), userId)
                || isRegistrationUser(match.getParticipant2(), userId)
                || isRegistrationUser(match.getParticipant2Partner(), userId);
    }

    private boolean isRegistrationUser(TournamentRegistration reg, UUID userId) {
        if (reg == null) return false;
        if (reg.getPlayer() != null && userId.equals(reg.getPlayer().getId())) return true;
        if (reg.getPartner() != null && userId.equals(reg.getPartner().getId())) return true;
        return false;
    }

    private void validateMatchCanBeStarted(TournamentMatch match) {
        if (match.getStatus() == TournamentMatchStatus.COMPLETED) {
            throw new InvalidMatchStateException("Match cannot be started: already completed");
        }
        if (match.getStatus() == TournamentMatchStatus.IN_PROGRESS) {
            throw new InvalidMatchStateException("Match cannot be started: already in progress");
        }
        if (match.getStatus() == TournamentMatchStatus.CANCELLED || match.getStatus() == TournamentMatchStatus.BYE) {
            throw new InvalidMatchStateException("Match cannot be started in status " + match.getStatus());
        }
        if (match.getParticipant1() == null || match.getParticipant2() == null) {
            throw new InvalidMatchStateException("Match cannot be started: participants not resolved");
        }
    }

    private void validateParticipantsNotBusy(UUID tournamentId, TournamentMatch match) {
        List<UUID> regIds = extractRegistrationIds(match);
        if (regIds.isEmpty()) {
            return;
        }

        List<TournamentMatch> activeMatches = tournamentMatchRepository.findActiveMatchesForParticipants(
                tournamentId,
                TournamentMatchStatus.IN_PROGRESS,
                regIds
        );

        List<TournamentMatch> otherActiveMatches = activeMatches.stream()
                .filter(m -> !Objects.equals(m.getId(), match.getId()))
                .toList();

        if (!otherActiveMatches.isEmpty()) {
            String busyNickname = findBusyParticipantNickname(match, otherActiveMatches);
            throw new ParticipantBusyException("Participant " + busyNickname + " is currently playing another match");
        }
    }

    private List<UUID> extractRegistrationIds(TournamentMatch match) {
        List<UUID> regIds = new ArrayList<>();
        if (match.getParticipant1() != null) regIds.add(match.getParticipant1().getId());
        if (match.getParticipant1Partner() != null) regIds.add(match.getParticipant1Partner().getId());
        if (match.getParticipant2() != null) regIds.add(match.getParticipant2().getId());
        if (match.getParticipant2Partner() != null) regIds.add(match.getParticipant2Partner().getId());
        return regIds;
    }

    private String findBusyParticipantNickname(TournamentMatch targetMatch, List<TournamentMatch> activeMatches) {
        for (TournamentMatch activeMatch : activeMatches) {
            List<UUID> activeRegIds = extractRegistrationIds(activeMatch);
            if (targetMatch.getParticipant1() != null && activeRegIds.contains(targetMatch.getParticipant1().getId())) {
                return getParticipantNickname(targetMatch.getParticipant1());
            }
            if (targetMatch.getParticipant1Partner() != null && activeRegIds.contains(targetMatch.getParticipant1Partner().getId())) {
                return getParticipantNickname(targetMatch.getParticipant1Partner());
            }
            if (targetMatch.getParticipant2() != null && activeRegIds.contains(targetMatch.getParticipant2().getId())) {
                return getParticipantNickname(targetMatch.getParticipant2());
            }
            if (targetMatch.getParticipant2Partner() != null && activeRegIds.contains(targetMatch.getParticipant2Partner().getId())) {
                return getParticipantNickname(targetMatch.getParticipant2Partner());
            }
        }
        return "Unknown";
    }

    private String getParticipantNickname(TournamentRegistration registration) {
        if (registration != null && registration.getPlayer() != null && registration.getPlayer().getNickname() != null) {
            return registration.getPlayer().getNickname();
        }
        return "Player";
    }

    private List<UUID> extractParticipantUserIds(TournamentMatch match) {
        List<UUID> userIds = new ArrayList<>();
        addRegistrationUserIds(userIds, match.getParticipant1());
        addRegistrationUserIds(userIds, match.getParticipant1Partner());
        addRegistrationUserIds(userIds, match.getParticipant2());
        addRegistrationUserIds(userIds, match.getParticipant2Partner());
        return userIds;
    }

    private void addRegistrationUserIds(List<UUID> userIds, TournamentRegistration reg) {
        if (reg == null) return;
        if (reg.getPlayer() != null && reg.getPlayer().getId() != null && !userIds.contains(reg.getPlayer().getId())) {
            userIds.add(reg.getPlayer().getId());
        }
        if (reg.getPartner() != null && reg.getPartner().getId() != null && !userIds.contains(reg.getPartner().getId())) {
            userIds.add(reg.getPartner().getId());
        }
    }

    private TournamentRegistration determineWinner(TournamentMatch tournamentMatch) {
        Match match = tournamentMatch.getMatch();
        if (match == null || match.getGames() == null || match.getGames().isEmpty()) {
            return tournamentMatch.getWinner();
        }

        int teamAWins = 0;
        int teamBWins = 0;
        for (Game game : match.getGames()) {
            if (game.getTeamAScore() > game.getTeamBScore()) {
                teamAWins++;
            } else if (game.getTeamBScore() > game.getTeamAScore()) {
                teamBWins++;
            }
        }

        if (teamAWins == teamBWins) {
            return null;
        }

        boolean teamAWon = teamAWins > teamBWins;
        TournamentRegistration part1 = tournamentMatch.getParticipant1();
        TournamentRegistration part2 = tournamentMatch.getParticipant2();

        if (isRegistrationInTeamA(part1, match)) {
            return teamAWon ? part1 : part2;
        }
        if (isRegistrationInTeamA(part2, match)) {
            return teamAWon ? part2 : part1;
        }
        return teamAWon ? part1 : part2;
    }

    private boolean isRegistrationInTeamA(TournamentRegistration reg, Match match) {
        if (reg == null || match == null) {
            return false;
        }
        if (reg.getPlayer() != null) {
            UUID playerId = reg.getPlayer().getId();
            if (playerId.equals(match.getTeamAAttackerId()) || playerId.equals(match.getTeamADefenderId())) {
                return true;
            }
        }
        if (reg.getPartner() != null) {
            UUID partnerId = reg.getPartner().getId();
            if (partnerId.equals(match.getTeamAAttackerId()) || partnerId.equals(match.getTeamADefenderId())) {
                return true;
            }
        }
        return false;
    }

    private void advanceWinnerInCup(TournamentMatch tournamentMatch, TournamentRegistration winner) {
        TournamentMatch nextMatch = tournamentMatch.getNextMatch();
        if (nextMatch == null || winner == null) {
            return;
        }
        if (nextMatch.getStatus() == TournamentMatchStatus.IN_PROGRESS || nextMatch.getStatus() == TournamentMatchStatus.COMPLETED) {
            return;
        }

        Integer winnerSeed = winner.equals(tournamentMatch.getParticipant1())
                ? tournamentMatch.getSeed1()
                : tournamentMatch.getSeed2();

        if (tournamentMatch.getMatchOrder() % 2 == 1) {
            nextMatch.setParticipant1(winner);
            nextMatch.setSeed1(winnerSeed);
        } else {
            nextMatch.setParticipant2(winner);
            nextMatch.setSeed2(winnerSeed);
        }

        if (nextMatch.getParticipant1() != null && nextMatch.getParticipant2() != null
                && nextMatch.getStatus() == TournamentMatchStatus.PENDING) {
            nextMatch.setStatus(TournamentMatchStatus.READY);
        }
        tournamentMatchRepository.save(nextMatch);
    }

    private TournamentMatchResponse mapToMatchResponse(TournamentMatch match) {
        List<String> busyNicknames = new ArrayList<>();
        if (match.getStatus() == TournamentMatchStatus.READY || match.getStatus() == TournamentMatchStatus.PENDING) {
            List<UUID> regIds = extractRegistrationIds(match);
            if (!regIds.isEmpty()) {
                List<TournamentMatch> activeMatches = tournamentMatchRepository.findActiveMatchesForParticipants(
                        match.getTournament().getId(),
                        TournamentMatchStatus.IN_PROGRESS,
                        regIds
                );
                List<TournamentMatch> otherActive = activeMatches.stream()
                        .filter(m -> !Objects.equals(m.getId(), match.getId()))
                        .toList();
                for (TournamentMatch activeMatch : otherActive) {
                    String busy = findBusyParticipantNickname(match, List.of(activeMatch));
                    if (!"Unknown".equals(busy) && !busyNicknames.contains(busy)) {
                        busyNicknames.add(busy);
                    }
                }
            }
        }

        boolean isOpponentBusy = !busyNicknames.isEmpty();
        boolean isStub = match.isParticipant1Stub() || match.isParticipant2Stub();
        boolean hasBothParticipants = match.getParticipant1() != null && match.getParticipant2() != null;
        boolean isPlayableStatus = match.getStatus() == TournamentMatchStatus.READY
                || (match.getStatus() == TournamentMatchStatus.PENDING && hasBothParticipants && match.getTournament().getFormat() != TournamentFormat.CUP);

        boolean isAvailable = isPlayableStatus
                && !isOpponentBusy
                && !isStub
                && hasBothParticipants
                && match.getStatus() != TournamentMatchStatus.BYE
                && match.getStatus() != TournamentMatchStatus.COMPLETED
                && match.getStatus() != TournamentMatchStatus.CANCELLED
                && match.getStatus() != TournamentMatchStatus.IN_PROGRESS;

        UUID ruleConfigId = match.getTournament() != null && match.getTournament().getRuleConfiguration() != null
                ? match.getTournament().getRuleConfiguration().getId()
                : null;
        String ruleConfigName = match.getTournament() != null && match.getTournament().getRuleConfiguration() != null
                ? match.getTournament().getRuleConfiguration().getName()
                : null;

        return TournamentMatchResponse.builder()
                .id(match.getId())
                .tournamentId(match.getTournament().getId())
                .round(match.getRound())
                .matchOrder(match.getMatchOrder())
                .matchId(match.getMatch() != null ? match.getMatch().getId() : null)
                .participant1(mapToRegistrationResponse(match.getParticipant1()))
                .participant1Partner(mapToRegistrationResponse(match.getParticipant1Partner()))
                .participant2(mapToRegistrationResponse(match.getParticipant2()))
                .participant2Partner(mapToRegistrationResponse(match.getParticipant2Partner()))
                .isParticipant1Stub(match.isParticipant1Stub())
                .isParticipant2Stub(match.isParticipant2Stub())
                .seed1(match.getSeed1())
                .seed2(match.getSeed2())
                .status(match.getStatus())
                .winnerRegistrationId(match.getWinner() != null ? match.getWinner().getId() : null)
                .nextMatchId(match.getNextMatch() != null ? match.getNextMatch().getId() : null)
                .createdAt(match.getCreatedAt())
                .isAvailable(isAvailable)
                .isOpponentBusy(isOpponentBusy)
                .busyParticipantNicknames(busyNicknames)
                .ruleConfigurationId(ruleConfigId)
                .ruleConfigurationName(ruleConfigName)
                .build();
    }

    private TournamentRegistrationResponse mapToRegistrationResponse(TournamentRegistration reg) {
        if (reg == null) return null;
        return TournamentRegistrationResponse.builder()
                .id(reg.getId())
                .tournamentId(reg.getTournament().getId())
                .tournamentName(reg.getTournament().getName())
                .playerId(reg.getPlayer() != null ? reg.getPlayer().getId() : null)
                .playerNickname(reg.getPlayer() != null ? reg.getPlayer().getNickname() : null)
                .playerAvatarUrl(reg.getPlayer() != null ? reg.getPlayer().getAvatar() : null)
                .partnerId(reg.getPartner() != null ? reg.getPartner().getId() : null)
                .partnerNickname(reg.getPartner() != null ? reg.getPartner().getNickname() : null)
                .partnerAvatarUrl(reg.getPartner() != null ? reg.getPartner().getAvatar() : null)
                .status(reg.getStatus())
                .seed(reg.getSeed())
                .strengthScore(reg.getStrengthScore())
                .createdAt(reg.getCreatedAt() != null ? reg.getCreatedAt().atOffset(ZoneOffset.UTC) : null)
                .updatedAt(reg.getUpdatedAt() != null ? reg.getUpdatedAt().atOffset(ZoneOffset.UTC) : null)
                .build();
    }
}
