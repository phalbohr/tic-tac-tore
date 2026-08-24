ALTER TABLE rule_configuration
    ADD COLUMN absolute_score_cap INT,
    ADD COLUMN timeouts_per_game INT DEFAULT 2 NOT NULL,
    ADD COLUMN timeout_duration_seconds INT DEFAULT 30 NOT NULL,
    ADD COLUMN possession_limit_5bar_seconds INT DEFAULT 10 NOT NULL,
    ADD COLUMN possession_limit_other_seconds INT DEFAULT 15 NOT NULL,
    ADD COLUMN side_swap_rule VARCHAR(30) DEFAULT 'BETWEEN_GAMES' NOT NULL,
    ADD COLUMN restart_rule VARCHAR(30) DEFAULT 'CONCEDING_TEAM' NOT NULL,
    ADD COLUMN spinning_allowed BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN aerials_allowed BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN position_swap_rule VARCHAR(30) DEFAULT 'BETWEEN_GAMES' NOT NULL,
    ADD COLUMN point_distribution VARCHAR(30) DEFAULT 'WIN_LOSS_3_0' NOT NULL;

CREATE INDEX idx_rule_configuration_created_by ON rule_configuration(created_by);
CREATE INDEX idx_rule_configuration_type ON rule_configuration(type);

-- Update seeded ITSF preset with absolute_score_cap = 8
UPDATE rule_configuration
SET absolute_score_cap = 8
WHERE id = '50f4a8e2-888e-4f10-9173-67c8cbcf8f3a';
