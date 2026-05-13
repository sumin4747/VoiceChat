CREATE TABLE saved_phrases (
                               id            BIGINT AUTO_INCREMENT PRIMARY KEY,
                               voice_model_id BIGINT NOT NULL,
                               content       TEXT NOT NULL,
                               audio_url     VARCHAR(500) NULL,
                               created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_saved_phrases_voice_model
                                   FOREIGN KEY (voice_model_id) REFERENCES voice_models(id)
);