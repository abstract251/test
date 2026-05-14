-- JWT + Spring Security upgrade
-- Execute on the online_exam database before switching the frontend to /auth/*

CREATE TABLE IF NOT EXISTS auth_refresh_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    jti VARCHAR(64) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    role VARCHAR(32) NOT NULL,
    username VARCHAR(64) NOT NULL,
    expires_at DATETIME NOT NULL,
    revoked TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_auth_refresh_token_user_role
    ON auth_refresh_token(user_id, role);

CREATE INDEX idx_auth_refresh_token_expires_at
    ON auth_refresh_token(expires_at);

ALTER TABLE admin
    MODIFY COLUMN pwd VARCHAR(100) NULL COMMENT '密码';

ALTER TABLE teacher
    MODIFY COLUMN pwd VARCHAR(100) NULL COMMENT '密码';

ALTER TABLE student
    MODIFY COLUMN pwd VARCHAR(100) NULL COMMENT '密码';

-- Password migration notes
-- 1. Existing admin / teacher / student passwords must be replaced with BCrypt hashes.
-- 2. Generate BCrypt values offline or with a one-off Spring runner, then execute updates like:
--    UPDATE admin   SET pwd = '$2a$10$replace_with_bcrypt_hash' WHERE adminId = 1;
--    UPDATE teacher SET pwd = '$2a$10$replace_with_bcrypt_hash' WHERE teacherId = 20001;
--    UPDATE student SET pwd = '$2a$10$replace_with_bcrypt_hash' WHERE studentId = 20230001;
-- 3. New passwords written through the application are already stored as BCrypt.
