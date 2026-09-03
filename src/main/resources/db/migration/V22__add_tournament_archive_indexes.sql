CREATE INDEX IF NOT EXISTS idx_tournament_status_updated_at ON tournament (status, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_tournament_status_created_at ON tournament (status, created_at DESC);
