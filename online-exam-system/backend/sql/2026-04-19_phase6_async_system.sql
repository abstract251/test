CREATE TABLE IF NOT EXISTS async_event_outbox (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    aggregate_type VARCHAR(64) NOT NULL,
    aggregate_id VARCHAR(128) NOT NULL,
    routing_key VARCHAR(128) NOT NULL,
    payload_json LONGTEXT NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'NEW',
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at DATETIME NULL,
    published_at DATETIME NULL,
    last_error VARCHAR(1000) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_async_event_outbox_event_id (event_id),
    KEY idx_async_event_outbox_status_next_retry_id (status, next_retry_at, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS async_event_consume_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    consumer_name VARCHAR(128) NOT NULL,
    event_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    processed_at DATETIME NULL,
    status VARCHAR(32) NOT NULL,
    error_message VARCHAR(1000) NULL,
    UNIQUE KEY uk_consumer_event (consumer_name, event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exam_score_statistics_projection (
    exam_code INT PRIMARY KEY,
    avg_score DECIMAL(10,2) NOT NULL DEFAULT 0,
    max_score INT NOT NULL DEFAULT 0,
    min_score INT NOT NULL DEFAULT 0,
    pass_rate DECIMAL(10,4) NOT NULL DEFAULT 0,
    total_count INT NOT NULL DEFAULT 0,
    pass_count INT NOT NULL DEFAULT 0,
    distribution_json JSON NOT NULL,
    last_event_id VARCHAR(64) NULL,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
