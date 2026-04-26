SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `exam_attempt` (
  `attempt_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '作答会话主键',
  `exam_code` INT NOT NULL COMMENT '考试编号',
  `student_id` INT NOT NULL COMMENT '学生编号',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0进行中 1已交卷',
  `answers_json` LONGTEXT NULL COMMENT '当前作答JSON',
  `started_at` DATETIME NOT NULL COMMENT '开始作答时间',
  `submitted_at` DATETIME NULL DEFAULT NULL COMMENT '交卷时间',
  PRIMARY KEY (`attempt_id`),
  UNIQUE KEY `uk_exam_attempt_exam_student` (`exam_code`, `student_id`),
  KEY `idx_exam_attempt_student` (`student_id`),
  KEY `idx_exam_attempt_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生作答会话';
