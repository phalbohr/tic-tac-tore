CREATE TABLE player_group (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    creator_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    is_favorite BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL,
    CONSTRAINT uk_player_group_creator_name UNIQUE (creator_id, name)
);

CREATE TABLE player_group_member (
    group_id UUID NOT NULL REFERENCES player_group(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    added_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (group_id, user_id)
);

CREATE INDEX idx_player_group_creator_id ON player_group(creator_id);
CREATE INDEX idx_player_group_member_user_id ON player_group_member(user_id);
