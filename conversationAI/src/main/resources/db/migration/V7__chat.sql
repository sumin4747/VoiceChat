CREATE TABLE chat_messages (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               voice_model_id BIGINT NOT NULL,
                               role VARCHAR(10) NOT NULL,
                               content TEXT NOT NULL,
                               audio_url VARCHAR(500),
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               KEY idx_chat_voice (voice_model_id)
);