CREATE TABLE achievement (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    name_key VARCHAR(100) NOT NULL,
    description_key VARCHAR(255) NOT NULL,
    icon VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE player_achievement (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES achievement(id) ON DELETE CASCADE,
    unlocked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_player_achievement UNIQUE (user_id, achievement_id)
);

CREATE INDEX idx_player_achievement_user_id ON player_achievement(user_id);
CREATE INDEX idx_player_achievement_achievement_id ON player_achievement(achievement_id);

-- Seed initial achievements
INSERT INTO achievement (id, code, category, name_key, description_key, icon, created_at, version)
VALUES 
('a0000000-0000-0000-0000-000000000001', 'FIRST_WIN', 'MILESTONE', 'achievements.first_win.title', 'achievements.first_win.description', 'trophy', CURRENT_TIMESTAMP, 0),
('a0000000-0000-0000-0000-000000000002', 'MATCHES_10', 'EXPERIENCE', 'achievements.matches_10.title', 'achievements.matches_10.description', 'flame', CURRENT_TIMESTAMP, 0),
('a0000000-0000-0000-0000-000000000003', 'CLEAN_SHEET', 'SKILL', 'achievements.clean_sheet.title', 'achievements.clean_sheet.description', 'shield', CURRENT_TIMESTAMP, 0),
('a0000000-0000-0000-0000-000000000004', 'STRIKER_50', 'OFFENSE', 'achievements.striker_50.title', 'achievements.striker_50.description', 'target', CURRENT_TIMESTAMP, 0),
('a0000000-0000-0000-0000-000000000005', 'DEFENSE_WALL', 'DEFENSE', 'achievements.defense_wall.title', 'achievements.defense_wall.description', 'wall', CURRENT_TIMESTAMP, 0);
