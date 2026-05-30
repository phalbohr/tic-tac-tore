ALTER TABLE "user" ADD COLUMN IF NOT EXISTS last_nickname_update TIMESTAMP WITH TIME ZONE;
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS language VARCHAR(10);
UPDATE "user" SET nickname = 'user_' || id WHERE nickname IS NULL;
ALTER TABLE "user" ADD CONSTRAINT user_nickname_not_null CHECK (nickname IS NOT NULL) NOT VALID;
ALTER TABLE "user" VALIDATE CONSTRAINT user_nickname_not_null;
ALTER TABLE "user" ALTER COLUMN nickname SET NOT NULL;
ALTER TABLE "user" DROP CONSTRAINT user_nickname_not_null;
