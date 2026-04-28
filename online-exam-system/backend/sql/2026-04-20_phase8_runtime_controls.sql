SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS runtime_feature_toggle (
    feature_key VARCHAR(64) NOT NULL PRIMARY KEY,
    enabled TINYINT(1) NOT NULL,
    version BIGINT NOT NULL,
    updated_by_id INT NULL,
    updated_by_role VARCHAR(32) NULL,
    updated_by_name VARCHAR(64) NULL,
    updated_reason VARCHAR(255) NOT NULL,
    preset_name VARCHAR(64) NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS runtime_feature_toggle_audit (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    batch_id VARCHAR(64) NOT NULL,
    feature_key VARCHAR(64) NOT NULL,
    old_enabled TINYINT(1) NOT NULL,
    new_enabled TINYINT(1) NOT NULL,
    operator_id INT NULL,
    operator_role VARCHAR(32) NULL,
    operator_name VARCHAR(64) NULL,
    reason VARCHAR(255) NOT NULL,
    preset_name VARCHAR(64) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_runtime_toggle_audit_batch (batch_id),
    KEY idx_runtime_toggle_audit_feature_created (feature_key, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
