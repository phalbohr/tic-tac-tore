CREATE TABLE matchmaking_pool (
    id UUID PRIMARY KEY,
    creator_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    match_type VARCHAR(20) NOT NULL,
    start_condition VARCHAR(20) NOT NULL,
    scheduled_time TIMESTAMP WITH TIME ZONE,
    skill_level VARCHAR(20) DEFAULT 'OPEN_FOR_ALL' NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL
);

CREATE TABLE pool_participant (
    id UUID PRIMARY KEY,
    pool_id UUID NOT NULL REFERENCES matchmaking_pool(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_pool_participant UNIQUE (pool_id, user_id)
);

CREATE INDEX idx_pool_creator_id ON matchmaking_pool(creator_id);
CREATE INDEX idx_pool_status ON matchmaking_pool(status);
CREATE INDEX idx_pool_participant_user_id ON pool_participant(user_id);
