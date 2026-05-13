-- 기존 테이블 삭제
DROP TABLE IF EXISTS persona_descriptions;

-- 새 테이블 생성
CREATE TABLE response_styles (
                                 id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 persona_id BIGINT NOT NULL UNIQUE,
                                 style      VARCHAR(30) NOT NULL DEFAULT 'EMPATHY',
                                 CONSTRAINT fk_response_styles_persona
                                     FOREIGN KEY (persona_id) REFERENCES personas(id)
);