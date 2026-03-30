CREATE TABLE consents (
                          id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                          user_id      BIGINT NOT NULL,
                          persona_id   BIGINT NOT NULL,
                          consent_type ENUM('terms_of_service','privacy_policy','voice_usage','ai_training') NOT NULL,

                          created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          revoked_at   TIMESTAMP NULL,

                          UNIQUE KEY uk_consents_user_persona_type (user_id, persona_id, consent_type),
                          KEY idx_consents_user (user_id),
                          KEY idx_consents_persona (persona_id),

                          CONSTRAINT fk_consents_user
                              FOREIGN KEY (user_id) REFERENCES users(id)
                                  ON DELETE CASCADE,
                          CONSTRAINT fk_consents_persona
                              FOREIGN KEY (persona_id) REFERENCES personas(id)
                                  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
