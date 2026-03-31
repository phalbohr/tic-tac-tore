package com.tictactore.controller;

import com.tictactore.model.*;
import com.tictactore.repository.MatchRepository;
import com.tictactore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/dev/seed-matches")
@RequiredArgsConstructor
public class DevDataController {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    @PostMapping
    public String seedMatches() {
        List<User> users = userRepository.findAll();
        if (users.size() < 4) {
            return "Not enough users. Run /api/v1/dev/seed first.";
        }

        for (int i = 0; i < 10; i++) {
            Match match = new Match();
            match.setCreator(users.get(0));
            match.setTeamAAttacker(users.get(0));
            match.setTeamADefender(users.get(1));
            match.setTeamBAttacker(users.get(2));
            match.setTeamBDefender(users.get(3));
            match.setStatus(MatchStatus.CONFIRMED);

            List<Game> games = new ArrayList<>();
            int gamesToPlay = random.nextBoolean() ? 2 : 3;
            int teamAWins = 0;
            int teamBWins = 0;

            for (int g = 1; g <= gamesToPlay; g++) {
                Game game = new Game();
                game.setGameNumber(g);
                game.setMatch(match);

                // Ensure someone wins the match
                if (teamAWins < 2 && teamBWins < 2) {
                    if (random.nextBoolean()) {
                        game.setTeamAScore(10);
                        game.setTeamBScore(random.nextInt(10));
                        teamAWins++;
                    } else {
                        game.setTeamAScore(random.nextInt(10));
                        game.setTeamBScore(10);
                        teamBWins++;
                    }
                } else if (teamAWins < 2) {
                    game.setTeamAScore(10);
                    game.setTeamBScore(random.nextInt(10));
                    teamAWins++;
                } else {
                    game.setTeamAScore(random.nextInt(10));
                    game.setTeamBScore(10);
                    teamBWins++;
                }
                games.add(game);
            }
            match.setGames(games);
            match.calculateWinner();
            matchRepository.save(match);
        }

        return "10 matches seeded.";
    }
}
