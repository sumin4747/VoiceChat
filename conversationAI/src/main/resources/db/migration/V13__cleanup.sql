-- anniversaries 테이블 삭제
DROP TABLE IF EXISTS anniversaries;

-- consents 테이블 삭제
DROP TABLE IF EXISTS consents;

-- users 테이블에서 fcm_token 컬럼 삭제
ALTER TABLE users DROP COLUMN fcm_token;

-- voice_models 테이블에서 reminder_interval_days 컬럼 삭제
ALTER TABLE voice_models DROP COLUMN reminder_interval_days;