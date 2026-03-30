ALTER TABLE personas
    ADD COLUMN deleted_at TIMESTAMP NULL;

CREATE INDEX idx_personas_deleted_at ON personas(deleted_at);

ALTER TABLE voice_models
    ADD COLUMN progress_percent INT DEFAULT 0,
    ADD COLUMN thumbnail_url VARCHAR(255) NULL;

ALTER TABLE users
    ADD COLUMN nickname VARCHAR(50) NULL;