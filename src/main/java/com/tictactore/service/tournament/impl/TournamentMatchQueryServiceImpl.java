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
                .collect(Collectors.toMap(TournamentRegistration::getId, this::mapToRegistrationResponse, (a, b) -> a, java.util.HashMap::new));

        List<TournamentRegistrationResponse> seededParticipants = registrations.stream()
                .filter(r -> r.getSeed() != null)
                .sorted(Comparator.comparingInt(TournamentRegistration::getSeed))
                .map(r -> registrationMap.get(r.getId()))
                .toList();

        Map<Integer, List<TournamentMatch>> matchesByRound = allMatches.stream()
                .collect(Collectors.groupingBy(TournamentMatch::getRound, LinkedHashMap::new, Collectors.toList()));

        int totalRounds = matchesByRound.keySet().stream().mapToInt(v -> v).max().orElse(0);

        List<RoundMatchesResponse> roundResponses = new ArrayList<>();
        for (Map.Entry<Integer, List<TournamentMatch>> entry : matchesByRound.entrySet()) {
            int roundNumber = entry.getKey();
            List<TournamentMatchResponse> matchResponses = entry.getValue().stream()
                    .map(m -> mapToMatchResponse(m, registrationMap))
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

        Map<UUID, TournamentRegistrationResponse> registrationMap = new java.util.HashMap<>();
        return matches.stream().map(m -> mapToMatchResponse(m, registrationMap)).toList();
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
            Map<UUID, TournamentRegistrationResponse> registrationMap
    ) {
        TournamentRegistrationResponse part1 = match.getParticipant1() != null
                ? registrationMap.computeIfAbsent(match.getParticipant1().getId(), k -> mapToRegistrationResponse(match.getParticipant1()))
                : null;
        TournamentRegistrationResponse part2 = match.getParticipant2() != null
                ? registrationMap.computeIfAbsent(match.getParticipant2().getId(), k -> mapToRegistrationResponse(match.getParticipant2()))
                : null;

        return TournamentMatchResponse.builder()
                .id(match.getId())
                .tournamentId(match.getTournament().getId())
                .round(match.getRound())
                .matchOrder(match.getMatchOrder())
                .matchId(match.getMatch() != null ? match.getMatch().getId() : null)
                .participant1(part1)
                .participant2(part2)
                .seed1(match.getSeed1())
                .seed2(match.getSeed2())
                .status(match.getStatus())
                .winnerRegistrationId(match.getWinner() != null ? match.getWinner().getId() : null)
                .nextMatchId(match.getNextMatch() != null ? match.getNextMatch().getId() : null)
                .createdAt(match.getCreatedAt())
                .build();
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
