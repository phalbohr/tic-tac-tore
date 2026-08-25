ALTER TABLE "user"
    ADD COLUMN default_group_id UUID REFERENCES player_group(id) ON DELETE SET NULL,
    ADD COLUMN default_rule_configuration_id UUID REFERENCES rule_configuration(id) ON DELETE SET NULL;

CREATE INDEX idx_user_default_group_id ON "user"(default_group_id);
CREATE INDEX idx_user_default_rule_configuration_id ON "user"(default_rule_configuration_id);
