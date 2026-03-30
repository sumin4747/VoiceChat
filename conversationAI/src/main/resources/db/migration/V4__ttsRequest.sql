CREATE TABLE tts_requests (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              message_id BIGINT NOT NULL,
                              provider VARCHAR(50) NOT NULL,
                              input_text_length INT NOT NULL,
                              latency_ms INT NULL,
                              audio_url VARCHAR(500) NULL,
                              cost_estimate DECIMAL(10,4) NULL,
                              status ENUM('SUCCESS','FAILED') NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              KEY idx_tts_requests_message (message_id),

                              CONSTRAINT fk_tts_requests_message
                                  FOREIGN KEY (message_id) REFERENCES messages(id)
                                      ON DELETE CASCADE
);

ALTER TABLE conversations
    ADD CONSTRAINT uk_user_persona_date
        UNIQUE (user_id, persona_id, conversation_date);

CREATE INDEX idx_messages_conv_created
    ON messages(conversation_id, created_at);