-- Seed initial humorous anti-achievements
INSERT INTO achievement (id, code, category, name_key, description_key, icon, created_at, version)
VALUES 
('a0000000-0000-0000-0000-000000000006', 'GOOSE_EGG', 'ANTI_ACHIEVEMENT', 'achievements.goose_egg.title', 'achievements.goose_egg.description', 'egg', CURRENT_TIMESTAMP, 0),
('a0000000-0000-0000-0000-000000000007', 'GENEROUS_HOST', 'ANTI_ACHIEVEMENT', 'achievements.generous_host.title', 'achievements.generous_host.description', 'volunteer_activism', CURRENT_TIMESTAMP, 0),
('a0000000-0000-0000-0000-000000000008', 'SIEVE_DEFENSE', 'ANTI_ACHIEVEMENT', 'achievements.sieve_defense.title', 'achievements.sieve_defense.description', 'water_drop', CURRENT_TIMESTAMP, 0),
('a0000000-0000-0000-0000-000000000009', 'HEARTBREAKER', 'ANTI_ACHIEVEMENT', 'achievements.heartbreaker.title', 'achievements.heartbreaker.description', 'heart_broken', CURRENT_TIMESTAMP, 0);
