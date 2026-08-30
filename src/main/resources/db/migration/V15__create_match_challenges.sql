CREATE TABLE match_challenge (
    id UUID PRIMARY KEY,
    challenger_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    target_player_id UUID REFERENCES "user"(id) ON DELETE CASCADE,
    target_group_id UUID REFERENCES player_group(id) ON DELETE CASCADE,
    match_type VARCHAR(20) NOT NULL,
    rule_config_id UUID REFERENCES rule_configuration(id) ON DELETE SET NULL,
    message VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    expires_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL,
    CONSTRAINT chk_challenge_target CHECK (target_player_id IS NOT NULL OR target_group_id IS NOT NULL)
);

CREATE INDEX idx_challenge_challenger_id ON match_challenge(challenger_id);
CREATE INDEX idx_challenge_target_player_id ON match_challenge(target_player_id);
CREATE INDEX idx_challenge_target_group_id ON match_challenge(target_group_id);
CREATE INDEX idx_challenge_status ON match_challenge(status);

ALTER TABLE notification_log
    ADD COLUMN challenge_id UUID REFERENCES match_challenge(id) ON DELETE SET NULL;

CREATE INDEX idx_notif_log_challenge_recipient ON notification_log(challenge_id, recipient_id);
