CREATE TABLE otp_codes (
                           id              BIGINT PRIMARY KEY AUTO_INCREMENT,
                           email           VARCHAR(255) NOT NULL,
                           code            VARCHAR(10) NOT NULL,
                           expires_at      DATETIME NOT NULL,
                           verified        BOOLEAN NOT NULL DEFAULT FALSE,
                           created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           KEY idx_otp_email (email),
                           KEY idx_otp_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;