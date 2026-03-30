-- otp_codes 테이블에 verify_token 컬럼 추가
ALTER TABLE otp_codes
    ADD COLUMN verify_token VARCHAR(100) NULL;

-- users 테이블에 login_id 컬럼 추가
ALTER TABLE users
    ADD COLUMN login_id VARCHAR(50) NULL UNIQUE;

-- users 테이블에서 password_hash NOT NULL 제약 완화 (OTP 사용자 제거로 불필요)
-- 기존 OTP 전용 사용자(password_hash = '') 정리 필요 시 아래 주석 해제
DELETE FROM users WHERE password_hash = '' OR password_hash IS NULL;