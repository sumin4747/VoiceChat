-- users 테이블에 fcm_token 추가
ALTER TABLE users ADD COLUMN fcm_token VARCHAR(500) NULL;

-- 알림 설정 테이블 생성
CREATE TABLE notification_settings (
                                       id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       user_id     BIGINT NOT NULL UNIQUE,
                                       notify_hour INT NOT NULL COMMENT '알림 시간 (0~23)',
                                       notify_minute INT NOT NULL DEFAULT 0 COMMENT '알림 분 (0~59)',
                                       enabled     BOOLEAN NOT NULL DEFAULT TRUE,
                                       CONSTRAINT fk_notification_settings_user
                                           FOREIGN KEY (user_id) REFERENCES users(id)
);