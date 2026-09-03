package com.tictactore.service.tournament.impl;

import com.tictactore.dto.RoundMatchesResponse;
import com.tictactore.dto.TournamentBracketResponse;
import com.tictactore.dto.TournamentMatchResponse;
import com.tictactore.dto.TournamentRegistrationResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.model.RegistrationStatus;
import com.tictactore.model.Tournament;
import com.tictactore.model.TournamentFormat;
import com.tictactore.model.TournamentMatch;
import com.tictactore.model.TournamentMatchStatus;
import com.tictactore.model.TournamentRegistration;
import com.tictactore.repository.TournamentMatchRepository;
import com.tictactore.repository.TournamentRegistrationRepository;
import com.tictactore.repository.TournamentRepository;
import com.tictactore.service.tournament.TournamentMatchQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TournamentMatchQueryServiceImpl implements TournamentMatchQueryService {

    private final TournamentRepository tournamentRepository;
    private final TournamentMatchRepository tournamentMatchRepository;
    private final TournamentRegistrationRepository registrationRepository;

    @Override
    public TournamentBracketResponse getTournamentBracket(UUID tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId.toString()));

        List<TournamentMatch> allMatches =
                tournamentMatchRepository.findByTournamentIdOrderByRoundAscMatchOrderAsc(tournamentId);

        List<TournamentRegistration> registrations =
                registrationRepository.findByTournamentIdAndStatus(tournamentId, RegistrationStatus.CONFIRMED);

        Map<UUID, TournamentRegistrationResponse> registrationMap = registrations.stream()
                .collect(Collectors.toMap(TournamentRegistration::getId, this::mapToRegistrationResponse, (a, b) -> a, HashMap::new));

        List<TournamentRegistrationResponse> seededParticipants = registrations.stream()
                .filter(r -> r.getSeed() != null)
                .sorted(Comparator.comparingInt(TournamentRegistration::getSeed))
                .map(r -> registrationMap.get(r.getId()))
                .toList();

        Map<UUID, String> busyParticipantMap = buildBusyParticipantMap(tournamentId);

        Map<Integer, List<TournamentMatch>> matchesByRound = allMatches.stream()
                .collect(Collectors.groupingBy(TournamentMatch::getRound, LinkedHashMap::new, Collectors.toList()));

        int totalRounds = matchesByRound.keySet().stream().mapToInt(v -> v).max().orElse(0);

        List<RoundMatchesResponse> roundResponses = new ArrayList<>();
        for (Map.Entry<Integer, List<TournamentMatch>> entry : matchesByRound.entrySet()) {
            int roundNumber = entry.getKey();
            List<TournamentMatchResponse> matchResponses = entry.getValue().stream()
                    .map(m -> mapToMatchResponse(m, registrationMap, busyParticipantMap))
                    .toList();

            String roundName = resolveRoundName(roundNumber, totalRounds, tournament.getFormat());
            roundResponses.add(RoundMatchesResponse.builder()
                    .round(roundNumber)
                    .roundName(roundName)
                    .matches(matchResponses)
                    .build());
        }

        return TournamentBracketResponse.builder()
                .tournamentId(tournament.getId())
                .tournamentName(tournament.getName())
                .format(tournament.getFormat())
                .mode(tournament.getMode())
                .status(tournament.getStatus())
                .totalRounds(totalRounds)
                .rounds(roundResponses)
                .seededParticipants(seededParticipants)
                .build();
    }

    @Override
    public List<TournamentMatchResponse> getTournamentMatches(UUID tournamentId, Integer round) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament", tournamentId.toString());
        }

        List<TournamentMatch> matches = (round != null)
                ? tournamentMatchRepository.findByTournamentIdAndRoundOrderByMatchOrderAsc(tournamentId, round)
                : tournamentMatchRepository.findByTournamentIdOrderByRoundAscMatchOrderAsc(tournamentId);

        Map<UUID, TournamentRegistrationResponse> registrationMap = new HashMap<>();
        Map<UUID, String> busyParticipantMap = buildBusyParticipantMap(tournamentId);

        return matches.stream()
                .map(m -> mapToMatchResponse(m, registrationMap, busyParticipantMap))
                .toList();
    }

    private Map<UUID, String> buildBusyParticipantMap(UUID tournamentId) {
        List<TournamentMatch> activeMatches =
                tournamentMatchRepository.findByTournamentIdAndStatus(tournamentId, TournamentMatchStatus.IN_PROGRESS);
        Map<UUID, String> busyMap = new HashMap<>();
        for (TournamentMatch activeMatch : activeMatches) {
            addBusyRegistration(busyMap, activeMatch.getParticipant1());
            addBusyRegistration(busyMap, activeMatch.getParticipant1Partner());
            addBusyRegistration(busyMap, activeMatch.getParticipant2());
            addBusyRegistration(busyMap, activeMatch.getParticipant2Partner());
        }
        return busyMap;
    }

    private void addBusyRegistration(Map<UUID, String> busyMap, TournamentRegistration reg) {
        if (reg != null) {
            if (reg.getId() != null) {
                String nickname = (reg.getPlayer() != null && reg.getPlayer().getNickname() != null)
                        ? reg.getPlayer().getNickname()
                        : "Player";
                busyMap.put(reg.getId(), nickname);
            }
            if (reg.getPartner() != null && reg.getPartner().getId() != null) {
                String nickname = reg.getPartner().getNickname() != null ? reg.getPartner().getNickname() : "Partner";
                busyMap.put(reg.getPartner().getId(), nickname);
            }
        }
    }

    private String resolveRoundName(int round, int totalRounds, TournamentFormat format) {
        if (format == TournamentFormat.CUP) {
            if (round == totalRounds) {
                return "Final";
            } else if (round == totalRounds - 1) {
                return "Semifinals";
            } else if (round == totalRounds - 2) {
                return "Quarterfinals";
            }
        }
        return "Round " + round;
    }

    private TournamentMatchResponse mapToMatchResponse(
            TournamentMatch match,
            Map<UUID, TournamentRegistrationResponse> registrationMap,
            Map<UUID, String> busyParticipantMap
    ) {
        TournamentRegistrationResponse part1 = match.getParticipant1() != null
                ? registrationMap.computeIfAbsent(match.getParticipant1().getId(), k -> mapToRegistrationResponse(match.getParticipant1()))
                : null;
        TournamentRegistrationResponse part1Partner = match.getParticipant1Partner() != null
                ? registrationMap.computeIfAbsent(match.getParticipant1Partner().getId(), k -> mapToRegistrationResponse(match.getParticipant1Partner()))
                : null;
        TournamentRegistrationResponse part2 = match.getParticipant2() != null
                ? registrationMap.computeIfAbsent(match.getParticipant2().getId(), k -> mapToRegistrationResponse(match.getParticipant2()))
                : null;
        TournamentRegistrationResponse part2Partner = match.getParticipant2Partner() != null
                ? registrationMap.computeIfAbsent(match.getParticipant2Partner().getId(), k -> mapToRegistrationResponse(match.getParticipant2Partner()))
                : null;

        List<String> busyNicknames = new ArrayList<>();
        if (match.getStatus() == TournamentMatchStatus.READY || match.getStatus() == TournamentMatchStatus.PENDING) {
            checkBusy(match.getParticipant1(), busyParticipantMap, busyNicknames);
            checkBusy(match.getParticipant1Partner(), busyParticipantMap, busyNicknames);
            checkBusy(match.getParticipant2(), busyParticipantMap, busyNicknames);
            checkBusy(match.getParticipant2Partner(), busyParticipantMap, busyNicknames);
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

        return TournamentMatchResponse.builder()
                .id(match.getId())
                .tournamentId(match.getTournament().getId())
                .round(match.getRound())
                .matchOrder(match.getMatchOrder())
                .matchId(match.getMatch() != null ? match.getMatch().getId() : null)
                .participant1(part1)
                .participant1Partner(part1Partner)
                .participant2(part2)
                .participant2Partner(part2Partner)
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
                .build();
    }

    private void checkBusy(TournamentRegistration reg, Map<UUID, String> busyMap, List<String> busyNicknames) {
        if (reg == null) return;
        if (reg.getId() != null && busyMap.containsKey(reg.getId())) {
            String name = busyMap.get(reg.getId());
            if (!busyNicknames.contains(name)) {
                busyNicknames.add(name);
            }
        }
        if (reg.getPartner() != null && reg.getPartner().getId() != null && busyMap.containsKey(reg.getPartner().getId())) {
            String name = busyMap.get(reg.getPartner().getId());
            if (!busyNicknames.contains(name)) {
                busyNicknames.add(name);
            }
        }
    }

    private TournamentRegistrationResponse mapToRegistrationResponse(TournamentRegistration reg) {
        return TournamentRegistrationResponse.builder()
                .id(reg.getId())
                .tournamentId(reg.getTournament().getId())
                .tournamentName(reg.getTournament().getName())
                .playerId(reg.getPlayer().getId())
                .playerNickname(reg.getPlayer().getNickname())
                .playerAvatarUrl(reg.getPlayer().getAvatar())
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
