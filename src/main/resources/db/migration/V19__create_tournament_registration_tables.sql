CREATE TABLE tournament_registration (
    id UUID PRIMARY KEY,
    tournament_id UUID NOT NULL REFERENCES tournament(id) ON DELETE CASCADE,
    player_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    partner_id UUID REFERENCES "user"(id) ON DELETE CASCADE,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_tournament_registration_tournament_id ON tournament_registration(tournament_id);
CREATE INDEX idx_tournament_registration_player_id ON tournament_registration(player_id);
CREATE INDEX idx_tournament_registration_partner_id ON tournament_registration(partner_id);
CREATE INDEX idx_tournament_registration_status ON tournament_registration(status);

CREATE UNIQUE INDEX uq_tournament_registration_player ON tournament_registration(tournament_id, player_id) WHERE status IN ('PENDING_CONFIRMATION', 'CONFIRMED');
CREATE UNIQUE INDEX uq_tournament_registration_partner ON tournament_registration(tournament_id, partner_id) WHERE status IN ('PENDING_CONFIRMATION', 'CONFIRMED') AND partner_id IS NOT NULL;
