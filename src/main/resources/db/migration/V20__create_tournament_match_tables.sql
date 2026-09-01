ALTER TABLE tournament_registration ADD COLUMN seed INT;
ALTER TABLE tournament_registration ADD COLUMN strength_score DOUBLE PRECISION;

CREATE TABLE tournament_match (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL REFERENCES tournament(id) ON DELETE CASCADE,
    match_id UUID REFERENCES match(id) ON DELETE SET NULL,
    round INT NOT NULL,
    match_order INT NOT NULL,
    participant1_id UUID REFERENCES tournament_registration(id) ON DELETE CASCADE,
    participant2_id UUID REFERENCES tournament_registration(id) ON DELETE CASCADE,
    seed1 INT,
    seed2 INT,
    status VARCHAR(30) NOT NULL,
    winner_id UUID REFERENCES tournament_registration(id) ON DELETE SET NULL,
    next_match_id UUID REFERENCES tournament_match(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_tournament_match_tournament_id ON tournament_match(tournament_id);
CREATE INDEX idx_tournament_match_tournament_round ON tournament_match(tournament_id, round);
CREATE INDEX idx_tournament_match_status ON tournament_match(status);
CREATE INDEX idx_tournament_match_participant1 ON tournament_match(participant1_id);
CREATE INDEX idx_tournament_match_participant2 ON tournament_match(participant2_id);
