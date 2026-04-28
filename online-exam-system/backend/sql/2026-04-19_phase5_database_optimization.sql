SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- Phase 5 database optimization.
-- Execute in a test database first.

DROP TABLE IF EXISTS `paper_manage_phase5_backup`;
RENAME TABLE `paper_manage` TO `paper_manage_phase5_backup`;

CREATE TABLE `paper_manage` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `paperId` INT NOT NULL COMMENT 'paper id',
  `questionType` INT NOT NULL COMMENT 'question type',
  `questionId` INT NOT NULL COMMENT 'question id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_question` (`paperId`, `questionType`, `questionId`),
  KEY `idx_paper_lookup` (`paperId`, `questionType`, `questionId`),
  KEY `idx_question_ref` (`questionType`, `questionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='paper manage';

INSERT IGNORE INTO `paper_manage` (`paperId`, `questionType`, `questionId`)
SELECT DISTINCT
  `paperId`,
  `questionType`,
  `questionId`
FROM `paper_manage_phase5_backup`
WHERE `paperId` IS NOT NULL
  AND `questionType` IS NOT NULL
  AND `questionId` IS NOT NULL;

ALTER TABLE `score`
  ADD UNIQUE KEY `uk_score_exam_student` (`examCode`, `studentId`),
  ADD KEY `idx_score_student_score_id` (`studentId`, `scoreId`),
  ADD KEY `idx_score_exam_score_id` (`examCode`, `scoreId`),
  ADD KEY `idx_score_exam_et_score` (`examCode`, `etScore`);

ALTER TABLE `exam_manage`
  ADD KEY `idx_exam_start` (`exam_start_at`, `examCode`),
  ADD KEY `idx_exam_scope_time` (`grade`, `major`, `institute`, `exam_start_at`, `examCode`),
  ADD KEY `idx_exam_paper` (`paperId`),
  ADD KEY `idx_exam_revoked` (`revoked_at`, `examCode`),
  ADD KEY `idx_exam_frozen` (`paper_frozen_at`, `examCode`);

ALTER TABLE `replay`
  ADD KEY `idx_replay_message` (`messageId`, `replayId`);

ALTER TABLE `multi_question`
  ADD KEY `idx_multi_subject_qid` (`subject`, `questionId`);

ALTER TABLE `fill_question`
  ADD KEY `idx_fill_subject_qid` (`subject`, `questionId`);

ALTER TABLE `judge_question`
  ADD KEY `idx_judge_subject_qid` (`subject`, `questionId`);

ALTER TABLE `student`
  ADD KEY `idx_student_card_id` (`cardId`);

ALTER TABLE `teacher`
  ADD KEY `idx_teacher_card_id` (`cardId`);

ALTER TABLE `admin`
  ADD KEY `idx_admin_card_id` (`cardId`);

SET FOREIGN_KEY_CHECKS = 1;
