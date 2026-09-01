CREATE TABLE tournament (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    format VARCHAR(30) NOT NULL,
    mode VARCHAR(30) NOT NULL,
    rule_configuration_id UUID NOT NULL REFERENCES rule_configuration(id),
    min_participants INT NOT NULL,
    max_participants INT NOT NULL,
    registration_deadline TIMESTAMP WITH TIME ZONE NOT NULL,
    round_count INT,
    has_playoff BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL,
    creator_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_tournament_status ON tournament(status);
CREATE INDEX idx_tournament_creator_id ON tournament(creator_id);
CREATE INDEX idx_tournament_rule_config_id ON tournament(rule_configuration_id);
