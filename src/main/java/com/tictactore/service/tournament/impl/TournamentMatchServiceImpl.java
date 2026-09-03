package com.tictactore.service.tournament.impl;

import com.tictactore.dto.TournamentMatchResponse;
import com.tictactore.dto.TournamentRegistrationResponse;
import com.tictactore.event.TournamentMatchCancelledEvent;
import com.tictactore.event.TournamentMatchStartedEvent;
import com.tictactore.exception.InvalidMatchStateException;
import com.tictactore.exception.ParticipantBusyException;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.exception.UnauthorizedMatchActionException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    private MatchRepository matchRepository;

    @Autowired(required = false)
    private TournamentStandingsService tournamentStandingsService;

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
        if (matchRepository != null && matchId != null) {
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

        if (tournamentStandingsService != null) {
            tournamentStandingsService.calculateStandings(tournamentMatch.getTournament().getId());
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
        if (match.getParticipant1() != null && match.getParticipant1().getPlayer() != null
                && userId.equals(match.getParticipant1().getPlayer().getId())) {
            return true;
        }
        if (match.getParticipant1Partner() != null && match.getParticipant1Partner().getPlayer() != null
                && userId.equals(match.getParticipant1Partner().getPlayer().getId())) {
            return true;
        }
        if (match.getParticipant2() != null && match.getParticipant2().getPlayer() != null
                && userId.equals(match.getParticipant2().getPlayer().getId())) {
            return true;
        }
        if (match.getParticipant2Partner() != null && match.getParticipant2Partner().getPlayer() != null
                && userId.equals(match.getParticipant2Partner().getPlayer().getId())) {
            return true;
        }
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
        if (match.getParticipant1() != null && match.getParticipant1().getPlayer() != null) {
            userIds.add(match.getParticipant1().getPlayer().getId());
        }
        if (match.getParticipant1Partner() != null && match.getParticipant1Partner().getPlayer() != null) {
            userIds.add(match.getParticipant1Partner().getPlayer().getId());
        }
        if (match.getParticipant2() != null && match.getParticipant2().getPlayer() != null) {
            userIds.add(match.getParticipant2().getPlayer().getId());
        }
        if (match.getParticipant2Partner() != null && match.getParticipant2Partner().getPlayer() != null) {
            userIds.add(match.getParticipant2Partner().getPlayer().getId());
        }
        return userIds;
    }

    private TournamentRegistration determineWinner(TournamentMatch tournamentMatch) {
        if (tournamentMatch.getMatch() == null || tournamentMatch.getMatch().getGames() == null) {
            return tournamentMatch.getParticipant1();
        }
        int teamAWins = 0;
        int teamBWins = 0;
        for (var game : tournamentMatch.getMatch().getGames()) {
            if (game.getTeamAScore() > game.getTeamBScore()) {
                teamAWins++;
            } else if (game.getTeamBScore() > game.getTeamAScore()) {
                teamBWins++;
            }
        }
        return teamAWins >= teamBWins ? tournamentMatch.getParticipant1() : tournamentMatch.getParticipant2();
    }

    private void advanceWinnerInCup(TournamentMatch tournamentMatch, TournamentRegistration winner) {
        TournamentMatch nextMatch = tournamentMatch.getNextMatch();
        if (tournamentMatch.getMatchOrder() % 2 == 1) {
            nextMatch.setParticipant1(winner);
            nextMatch.setSeed1(tournamentMatch.getSeed1());
        } else {
            nextMatch.setParticipant2(winner);
            nextMatch.setSeed2(tournamentMatch.getSeed2());
        }

        if (nextMatch.getParticipant1() != null && nextMatch.getParticipant2() != null
                && nextMatch.getStatus() == TournamentMatchStatus.PENDING) {
            nextMatch.setStatus(TournamentMatchStatus.READY);
        }
        tournamentMatchRepository.save(nextMatch);
    }

    private TournamentMatchResponse mapToMatchResponse(TournamentMatch match) {
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
                .isAvailable(match.getStatus() == TournamentMatchStatus.READY)
                .isOpponentBusy(false)
                .busyParticipantNicknames(List.of())
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
