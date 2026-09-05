package com.tictactore.service.impl;

import com.tictactore.dto.GameDto;
import com.tictactore.exception.InvalidMatchScoreException;
import com.tictactore.model.MatchFormat;
import com.tictactore.model.RuleConfiguration;
import com.tictactore.model.WinByTwoRule;
import com.tictactore.service.MatchScoreValidator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MatchScoreValidatorImpl implements MatchScoreValidator {

    private static final int DEFAULT_MAX_GAMES = 3;
    private static final int DEFAULT_MAX_SCORE = 100;

    @Override
    public void validateScores(RuleConfiguration ruleConfig, List<GameDto> games) {
        int maxGames = ruleConfig != null ? ruleConfig.getGameLimit() : DEFAULT_MAX_GAMES;

        if (games == null || games.isEmpty() || games.size() > maxGames) {
            throw new InvalidMatchScoreException("Match must have between 1 and " + maxGames + " games");
        }

        int winsA = 0;
        int winsB = 0;
        for (int i = 0; i < games.size(); i++) {
            GameDto gameDto = games.get(i);
            boolean isDecisiveGame = false;
            if (ruleConfig != null) {
                if (ruleConfig.getMatchFormat() == null || ruleConfig.getMatchFormat() == MatchFormat.BEST_OF_N) {
                    int target = ruleConfig.getGamesToWin() > 0 ? ruleConfig.getGamesToWin() : (ruleConfig.getGameLimit() / 2 + 1);
                    isDecisiveGame = (winsA == target - 1 && winsB == target - 1);
                } else if (ruleConfig.getMatchFormat() == MatchFormat.FIXED_GAMES) {
                    isDecisiveGame = (i == ruleConfig.getGameLimit() - 1 && winsA == winsB);
                }
            }

            validateSingleGame(ruleConfig, gameDto, isDecisiveGame);

            if (gameDto.teamAScore() > gameDto.teamBScore()) {
                winsA++;
            } else if (gameDto.teamBScore() > gameDto.teamAScore()) {
                winsB++;
            }
        }
    }

    private void validateSingleGame(RuleConfiguration ruleConfig, GameDto gameDto, boolean isDecisiveGame) {
        int scoreA = gameDto.teamAScore();
        int scoreB = gameDto.teamBScore();

        if (scoreA < 0 || scoreB < 0) {
            throw new InvalidMatchScoreException("Game scores must be between 0 and 100");
        }

        if (ruleConfig == null) {
            if (scoreA > DEFAULT_MAX_SCORE || scoreB > DEFAULT_MAX_SCORE) {
                throw new InvalidMatchScoreException("Game scores must be between 0 and " + DEFAULT_MAX_SCORE);
            }
            return;
        }

        int goalLimit = ruleConfig.getGoalLimit();
        boolean winByTwo = false;
        if (ruleConfig.getWinByTwoRule() == WinByTwoRule.ALL_GAMES) {
            winByTwo = true;
        } else if (ruleConfig.getWinByTwoRule() == WinByTwoRule.DECISIVE_GAME_ONLY) {
            winByTwo = isDecisiveGame;
        }
        Integer absoluteScoreCap = ruleConfig.getAbsoluteScoreCap();

        if (scoreA == scoreB) {
            throw new InvalidMatchScoreException("Game scores cannot be tied");
        }

        int winnerScore = Math.max(scoreA, scoreB);
        int loserScore = Math.min(scoreA, scoreB);

        if (absoluteScoreCap != null && (winnerScore > absoluteScoreCap || loserScore > absoluteScoreCap)) {
            throw new InvalidMatchScoreException("Game score cannot exceed absolute score cap of " + absoluteScoreCap);
        }

        if (winnerScore < goalLimit) {
            throw new InvalidMatchScoreException("Winning score must reach at least the goal limit of " + goalLimit);
        }

        if (!winByTwo) {
            if (winnerScore != goalLimit) {
                throw new InvalidMatchScoreException("Winning score must equal goal limit of " + goalLimit + " when win-by-two is disabled");
            }
        } else {
            if (absoluteScoreCap != null && winnerScore == absoluteScoreCap) {
                return;
            }
            if (winnerScore == goalLimit) {
                if (winnerScore - loserScore < 2) {
                    throw new InvalidMatchScoreException("Winning margin must be at least 2 goals when win-by-two is enabled");
                }
            } else {
                if (winnerScore - loserScore != 2) {
                    throw new InvalidMatchScoreException("Game beyond goal limit must end with exactly a 2-point lead");
                }
            }
        }
    }
}
