-- Migration to add last_nickname_update and language columns, and make nickname NOT NULL
ALTER TABLE "users" ADD COLUMN IF NOT EXISTS last_nickname_update TIMESTAMP WITH TIME ZONE;
ALTER TABLE "users" ADD COLUMN IF NOT EXISTS language VARCHAR(10);
ALTER TABLE "users" ALTER COLUMN nickname SET NOT NULL;
