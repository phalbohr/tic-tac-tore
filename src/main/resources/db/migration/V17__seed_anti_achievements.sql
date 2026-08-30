-- Seed initial humorous anti-achievements
INSERT INTO achievement (id, code, category, name_key, description_key, icon, created_at, version)
VALUES 
('7b23e3e0-88df-4a6c-947b-1a9829f0c2a1', 'GOOSE_EGG', 'ANTI_ACHIEVEMENT', 'achievements.goose_egg.title', 'achievements.goose_egg.description', 'egg', CURRENT_TIMESTAMP, 0),
('5fa692c2-75d1-4db8-b59a-df34ef31a478', 'GENEROUS_HOST', 'ANTI_ACHIEVEMENT', 'achievements.generous_host.title', 'achievements.generous_host.description', 'volunteer_activism', CURRENT_TIMESTAMP, 0),
('8e3a89e1-64d8-4fbb-91bb-73138b340156', 'SIEVE_DEFENSE', 'ANTI_ACHIEVEMENT', 'achievements.sieve_defense.title', 'achievements.sieve_defense.description', 'water_drop', CURRENT_TIMESTAMP, 0),
('9d1f736a-20fa-4cf0-8451-2cfb47167523', 'HEARTBREAKER', 'ANTI_ACHIEVEMENT', 'achievements.heartbreaker.title', 'achievements.heartbreaker.description', 'heart_broken', CURRENT_TIMESTAMP, 0);
