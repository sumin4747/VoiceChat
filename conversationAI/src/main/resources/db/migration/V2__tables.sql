CREATE TABLE users (
                       id              BIGINT PRIMARY KEY AUTO_INCREMENT,
                       email           VARCHAR(255) NOT NULL,
                       password_hash   VARCHAR(255) NOT NULL,
                       status          ENUM('ACTIVE','BLOCKED','DELETED') NOT NULL DEFAULT 'ACTIVE',
                       created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       deleted_at      TIMESTAMP NULL,   -- soft
                       KEY idx_users_deleted_at (deleted_at),
                       UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE personas (
                          id             BIGINT PRIMARY KEY AUTO_INCREMENT,
                          user_id        BIGINT NOT NULL,
                          persona_name   VARCHAR(100) NOT NULL,
                          relationship   VARCHAR(50) NULL,
                          created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          KEY idx_personas_user (user_id),
                          CONSTRAINT fk_personas_user
                              FOREIGN KEY (user_id) REFERENCES users(id)
                                  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE persona_descriptions (
                                      id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      persona_id            BIGINT NOT NULL,
                                      persona_tone          TEXT NOT NULL,
                                      persona_personality   TEXT NULL,
                                      UNIQUE KEY uk_persona_descriptions_persona (persona_id),
                                      CONSTRAINT fk_persona_descriptions_persona
                                          FOREIGN KEY (persona_id) REFERENCES personas(id)
                                              ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE voice_sources (
                               id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
                               persona_id         BIGINT NOT NULL,
                               voice_path         VARCHAR(255) NOT NULL,
                               voice_type         VARCHAR(20) NOT NULL,
                               duration_seconds   INT NULL,
                               status             ENUM('UPLOADED','PROCESSED','FAILED') NOT NULL DEFAULT 'UPLOADED',
                               created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               KEY idx_voice_sources_persona (persona_id),
                               CONSTRAINT fk_voice_sources_persona
                                   FOREIGN KEY (persona_id) REFERENCES personas(id)
                                       ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE voice_models (
                              id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
                              persona_id          BIGINT NOT NULL,
                              provider            VARCHAR(50) NOT NULL,          -- local or 돌릴 AI명
                              external_model_id   VARCHAR(255) NOT NULL,   -- 모델 ID(모델 같아도 V2일 수 있기 때문)
                              status              ENUM('READY','TRAINING','FAILED') NOT NULL DEFAULT 'TRAINING',
                              created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              KEY idx_voice_models_persona (persona_id),
                              CONSTRAINT fk_voice_models_persona
                                  FOREIGN KEY (persona_id) REFERENCES personas(id)
                                      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE conversations (
                               id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
                               user_id             BIGINT NOT NULL,
                               persona_id          BIGINT NOT NULL,
                               conversation_date   DATE NOT NULL,
                               created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               deleted_at          TIMESTAMP NULL,  -- soft
                               KEY idx_conversations_user_date (user_id, conversation_date),
                               KEY idx_conversations_deleted_at (deleted_at),
                               KEY idx_conversations_user_date_deleted (user_id, conversation_date, deleted_at),

                               KEY idx_conversations_user (user_id),
                               KEY idx_conversations_persona (persona_id),
                               CONSTRAINT fk_conversations_user
                                   FOREIGN KEY (user_id) REFERENCES users(id)
                                       ON DELETE CASCADE,
                               CONSTRAINT fk_conversations_persona
                                   FOREIGN KEY (persona_id) REFERENCES personas(id)
                                       ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE messages (
                          id                BIGINT PRIMARY KEY AUTO_INCREMENT,
                          conversation_id   BIGINT NOT NULL,
                          sender            ENUM('user', 'persona', 'system') NOT NULL,
                          content           TEXT NOT NULL,
                          created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          deleted_at        TIMESTAMP NULL, -- soft
                          KEY idx_messages_deleted_at (deleted_at),

                          KEY idx_messages_conversation (conversation_id),
                          KEY idx_messages_created_at (created_at),
                          CONSTRAINT fk_messages_conversation
                              FOREIGN KEY (conversation_id) REFERENCES conversations(id)
                                  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE anniversaries (
                               id          BIGINT PRIMARY KEY AUTO_INCREMENT,
                               user_id     BIGINT NOT NULL,
                               persona_id  BIGINT NULL,
                               event_name       VARCHAR(200) NOT NULL,
                               event_date  DATE NOT NULL,
                               repeat_yearly BOOLEAN NOT NULL DEFAULT TRUE,
                               is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
                               created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               KEY idx_anniversaries_user (user_id),
                               KEY idx_anniversaries_persona (persona_id),
                               CONSTRAINT fk_anniversaries_user
                                   FOREIGN KEY (user_id) REFERENCES users(id)
                                       ON DELETE CASCADE,
                               CONSTRAINT fk_anniversaries_persona
                                   FOREIGN KEY (persona_id) REFERENCES personas(id)
                                       ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;