ALTER TABLE tournament_match ADD COLUMN participant1_partner_id UUID REFERENCES tournament_registration(id) ON DELETE CASCADE;
ALTER TABLE tournament_match ADD COLUMN participant2_partner_id UUID REFERENCES tournament_registration(id) ON DELETE CASCADE;
ALTER TABLE tournament_match ADD COLUMN is_participant1_stub BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE tournament_match ADD COLUMN is_participant2_stub BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_tournament_match_part1_partner ON tournament_match(participant1_partner_id);
CREATE INDEX idx_tournament_match_part2_partner ON tournament_match(participant2_partner_id);
