SET @message_title_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'message'
              AND column_name = 'title'
              AND character_maximum_length = 60
        ),
        'SELECT 1',
        'ALTER TABLE message MODIFY COLUMN title VARCHAR(60) NULL'
    )
);
PREPARE stmt FROM @message_title_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @message_creator_id_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'message'
              AND column_name = 'creator_id'
        ),
        'SELECT 1',
        'ALTER TABLE message ADD COLUMN creator_id INT NULL'
    )
);
PREPARE stmt FROM @message_creator_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @message_creator_role_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'message'
              AND column_name = 'creator_role'
        ),
        'SELECT 1',
        'ALTER TABLE message ADD COLUMN creator_role VARCHAR(32) NULL'
    )
);
PREPARE stmt FROM @message_creator_role_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @message_creator_name_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'message'
              AND column_name = 'creator_name'
        ),
        'SELECT 1',
        'ALTER TABLE message ADD COLUMN creator_name VARCHAR(64) NULL'
    )
);
PREPARE stmt FROM @message_creator_name_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @message_created_at_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'message'
              AND column_name = 'created_at'
        ),
        'SELECT 1',
        'ALTER TABLE message ADD COLUMN created_at DATETIME NULL'
    )
);
PREPARE stmt FROM @message_created_at_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @message_updated_at_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'message'
              AND column_name = 'updated_at'
        ),
        'SELECT 1',
        'ALTER TABLE message ADD COLUMN updated_at DATETIME NULL'
    )
);
PREPARE stmt FROM @message_updated_at_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @replay_creator_id_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'replay'
              AND column_name = 'creator_id'
        ),
        'SELECT 1',
        'ALTER TABLE replay ADD COLUMN creator_id INT NULL'
    )
);
PREPARE stmt FROM @replay_creator_id_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @replay_creator_role_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'replay'
              AND column_name = 'creator_role'
        ),
        'SELECT 1',
        'ALTER TABLE replay ADD COLUMN creator_role VARCHAR(32) NULL'
    )
);
PREPARE stmt FROM @replay_creator_role_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @replay_creator_name_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'replay'
              AND column_name = 'creator_name'
        ),
        'SELECT 1',
        'ALTER TABLE replay ADD COLUMN creator_name VARCHAR(64) NULL'
    )
);
PREPARE stmt FROM @replay_creator_name_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @replay_created_at_sql = (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'replay'
              AND column_name = 'created_at'
        ),
        'SELECT 1',
        'ALTER TABLE replay ADD COLUMN created_at DATETIME NULL'
    )
);
PREPARE stmt FROM @replay_created_at_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE message
SET creator_id = COALESCE(creator_id, 0),
    creator_role = COALESCE(creator_role, 'SYSTEM'),
    creator_name = COALESCE(creator_name, 'history'),
    created_at = COALESCE(created_at, STR_TO_DATE(CONCAT(`time`, ' 00:00:00'), '%Y-%m-%d %H:%i:%s')),
    updated_at = COALESCE(updated_at, STR_TO_DATE(CONCAT(`time`, ' 00:00:00'), '%Y-%m-%d %H:%i:%s'));

UPDATE replay
SET creator_id = COALESCE(creator_id, 0),
    creator_role = COALESCE(creator_role, 'SYSTEM'),
    creator_name = COALESCE(creator_name, 'history'),
    created_at = COALESCE(created_at, STR_TO_DATE(CONCAT(replayTime, ' 00:00:00'), '%Y-%m-%d %H:%i:%s'));
