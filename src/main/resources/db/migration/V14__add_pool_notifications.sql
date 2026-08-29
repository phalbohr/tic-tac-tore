ALTER TABLE notification_log
    ADD COLUMN pool_id UUID REFERENCES matchmaking_pool(id) ON DELETE SET NULL;

CREATE INDEX idx_notif_log_pool_recipient ON notification_log(pool_id, recipient_id);

ALTER TABLE "user"
    ADD COLUMN pool_notifications_enabled BOOLEAN DEFAULT TRUE NOT NULL;

CREATE INDEX idx_user_pool_notifications_enabled ON "user"(pool_notifications_enabled);
