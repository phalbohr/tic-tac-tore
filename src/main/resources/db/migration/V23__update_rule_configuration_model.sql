-- Update Rule Configuration model to support MatchFormat, GamesToWin, and WinByTwoRule enum

ALTER TABLE rule_configuration
    ADD COLUMN match_format VARCHAR(30) DEFAULT 'BEST_OF_N' NOT NULL,
    ADD COLUMN games_to_win INT DEFAULT 3 NOT NULL,
    ADD COLUMN win_by_two_rule VARCHAR(30) DEFAULT 'ALL_GAMES' NOT NULL;

-- Migrate legacy win_by_two boolean data to win_by_two_rule
UPDATE rule_configuration
SET win_by_two_rule = CASE
    WHEN win_by_two = TRUE THEN 'ALL_GAMES'
    ELSE 'NONE'
END;

-- Drop legacy win_by_two column
ALTER TABLE rule_configuration
    DROP COLUMN win_by_two;

-- Update ITSF Standard Matchplay preset: Best of 5 (first to 3), Win by 2 only in decisive game (5th), Free position swap
UPDATE rule_configuration
SET match_format = 'BEST_OF_N',
    game_limit = 5,
    games_to_win = 3,
    win_by_two_rule = 'DECISIVE_GAME_ONLY',
    position_swap_rule = 'FREE',
    side_swap_rule = 'BETWEEN_GAMES',
    absolute_score_cap = 8
WHERE id = '50f4a8e2-888e-4f10-9173-67c8cbcf8f3a';

-- Update check constraint for point_distribution to allow ONE_POINT_PER_GAME_WON
ALTER TABLE rule_configuration
    DROP CONSTRAINT IF EXISTS rule_configuration_point_distribution_check;

ALTER TABLE rule_configuration
    ADD CONSTRAINT rule_configuration_point_distribution_check
    CHECK (point_distribution IN ('WIN_LOSS_3_0', 'WIN_LOSS_2_0', 'WIN_DRAW_LOSS_3_1_0', 'ONE_POINT_PER_GAME_WON'));

-- Update DTFB Standard preset: Fixed 2 games, no win by 2, no position swap, 1 point per game won
UPDATE rule_configuration
SET match_format = 'FIXED_GAMES',
    game_limit = 2,
    games_to_win = 2,
    win_by_two_rule = 'NONE',
    position_swap_rule = 'NEVER',
    side_swap_rule = 'NONE',
    point_distribution = 'ONE_POINT_PER_GAME_WON',
    absolute_score_cap = NULL
WHERE id = '1e4b85ab-2c84-4861-ba04-eb17a3a5e8dc';
