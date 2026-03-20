package com.tictactore.service;

import com.tictactore.annotation.Idempotent;
import com.tictactore.dto.MatchRequest;
import com.tictactore.dto.MatchResponse;
import com.tictactore.exception.ResourceNotFoundException;
import com.tictactore.mapper.MatchMapper;
import com.tictactore.model.Game;
import com.tictactore.model.Match;
import com.tictactore.model.User;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatchOperation {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final MatchMapper matchMapper;

    @Idempotent
    @Transactional
    public MatchResponse createMatch(MatchRequest request, User creator) {
        var playerIds = List.of(
                request.teamAAttackerId(), request.teamADefenderId(),
                request.teamBAttackerId(), request.teamBDefenderId()
        );

        Map<UUID, User> usersMap = userRepository.findAllById(playerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        var match = new Match();
        match.setCreator(creator);
        match.setTeamAAttacker(getUserFromMap(usersMap, request.teamAAttackerId(), "Team A Attacker"));
        match.setTeamADefender(getUserFromMap(usersMap, request.teamADefenderId(), "Team A Defender"));
        match.setTeamBAttacker(getUserFromMap(usersMap, request.teamBAttackerId(), "Team B Attacker"));
        match.setTeamBDefender(getUserFromMap(usersMap, request.teamBDefenderId(), "Team B Defender"));

        for (int i = 0; i < request.games().size(); i++) {
            var gameReq = request.games().get(i);
            var game = new Game();
            game.setGameNumber(i + 1);
            game.setTeamAScore(gameReq.teamAScore());
            game.setTeamBScore(gameReq.teamBScore());
            match.addGame(game);
        }

        var savedMatch = matchRepository.save(match);
        log.info("Match created successfully with ID: {}", savedMatch.getId());
        
        return matchMapper.toResponse(savedMatch);
    }

    @Idempotent
    @Transactional
    public void approveMatch(UUID matchId) {
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        match.approve();
        matchRepository.save(match);
    }

    @Idempotent
    @Transactional
    public void rejectMatch(UUID matchId) {
        var match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        match.reject();
        matchRepository.save(match);
    }

    private User getUserFromMap(Map<UUID, User> usersMap, UUID id, String roleName) {
        return Optional.ofNullable(usersMap.get(id))
                .orElseThrow(() -> new ResourceNotFoundException(roleName + " not found"));
    }
}
