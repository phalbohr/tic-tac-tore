CREATE TABLE rule_configuration (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    goal_limit INT NOT NULL,
    game_limit INT NOT NULL,
    win_by_two BOOLEAN NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL
);
