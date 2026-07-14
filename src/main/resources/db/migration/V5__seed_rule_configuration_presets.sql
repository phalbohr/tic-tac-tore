-- Seed ITSF Preset
INSERT INTO rule_configuration (
    id,
    name,
    type,
    goal_limit,
    game_limit,
    win_by_two,
    created_by,
    created_at,
    version
) VALUES (
    '50f4a8e2-888e-4f10-9173-67c8cbcf8f3a', -- Deterministic UUID for ITSF
    'ITSF Standard Matchplay',
    'PRESET',
    5,
    5, -- best of 5 (so game limit 5 max games)
    TRUE,
    '00000000-0000-0000-0000-000000000000', -- System User
    CURRENT_TIMESTAMP,
    0
);

-- Seed DTFB Preset
INSERT INTO rule_configuration (
    id,
    name,
    type,
    goal_limit,
    game_limit,
    win_by_two,
    created_by,
    created_at,
    version
) VALUES (
    '1e4b85ab-2c84-4861-ba04-eb17a3a5e8dc', -- Deterministic UUID for DTFB
    'DTFB Standard',
    'PRESET',
    5,
    3, -- typically best of 3 or played until something else. Using 3 for now.
    FALSE,
    '00000000-0000-0000-0000-000000000000', -- System User
    CURRENT_TIMESTAMP,
    0
);