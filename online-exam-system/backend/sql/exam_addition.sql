-- 考试业务：开考时刻、冻结快照、撤销审计（评审稿 §4–§7、§12）
-- 执行前请备份；建议在测试库验证后再用于其他环境。
-- 时区语义：业务时间按 Asia/Shanghai 解释（与 application 中 serverTimezone 一致）。

SET NAMES utf8mb4;

-- 1) exam_manage：开考时刻、冻结完成时刻、撤销
ALTER TABLE `exam_manage`
  MODIFY COLUMN `examDate` VARCHAR(32) NULL DEFAULT NULL COMMENT '考试日期/展示用(兼容旧数据，可为日期或完整时间串)',
  ADD COLUMN `exam_start_at` DATETIME NULL COMMENT '开考时刻(Asia/Shanghai)' AFTER `examDate`,
  ADD COLUMN `paper_frozen_at` DATETIME NULL COMMENT '本场共用快照生成完成时刻' AFTER `totalTime`,
  ADD COLUMN `revoked_at` DATETIME NULL COMMENT '撤销时刻' AFTER `tips`,
  ADD COLUMN `revoke_reason` VARCHAR(500) NULL DEFAULT NULL COMMENT '撤销原因' AFTER `revoked_at`;

-- 历史数据：仅有 examDate 日期时，补全为当日 00:00:00 开考
UPDATE `exam_manage`
SET `exam_start_at` = STR_TO_DATE(CONCAT(TRIM(`examDate`), ' 00:00:00'), '%Y-%m-%d %H:%i:%s')
WHERE (`exam_start_at` IS NULL OR `exam_start_at` = '1970-01-01 00:00:00')
  AND `examDate` IS NOT NULL
  AND TRIM(`examDate`) <> ''
  AND TRIM(`examDate`) NOT LIKE '%:%';

-- 若 examDate 已含时间（兼容误写入的长字符串）
UPDATE `exam_manage`
SET `exam_start_at` = STR_TO_DATE(TRIM(`examDate`), '%Y-%m-%d %H:%i:%s')
WHERE `exam_start_at` IS NULL
  AND `examDate` IS NOT NULL
  AND TRIM(`examDate`) LIKE '%:%';

-- 2) 本场共用快照（每场考试至多一行头表 + 多行题目）
CREATE TABLE IF NOT EXISTS `exam_shared_snapshot` (
  `exam_code` INT NOT NULL COMMENT '考试编号',
  `paper_id` INT NULL DEFAULT NULL COMMENT '冻结时关联的试卷编号',
  `created_at` DATETIME NOT NULL COMMENT '快照生成时间(上海)',
  PRIMARY KEY (`exam_code`),
  KEY `idx_paper_id` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试本场共用快照(头)';

CREATE TABLE IF NOT EXISTS `exam_shared_snapshot_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `exam_code` INT NOT NULL,
  `question_type` TINYINT NOT NULL COMMENT '1选择 2填空 3判断',
  `question_id` INT NOT NULL,
  `display_order` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_order` (`exam_code`, `display_order`),
  KEY `idx_exam` (`exam_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试本场共用快照题目行';

-- 3) 撤销审计
CREATE TABLE IF NOT EXISTS `exam_revoke_audit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `exam_code` INT NOT NULL,
  `admin_id` INT NOT NULL,
  `admin_name` VARCHAR(64) NULL DEFAULT NULL,
  `reason` VARCHAR(500) NOT NULL,
  `detail` VARCHAR(1000) NULL DEFAULT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_exam` (`exam_code`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='超级管理员撤销考试审计';

SET FOREIGN_KEY_CHECKS = 1;
