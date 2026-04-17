-- 重置测试考试作答状态，便于重复联调

SET NAMES utf8mb4;

SET @test_exam_code := 20991001;
SET @test_exam_start := DATE_ADD(NOW(), INTERVAL 5 MINUTE);
SET @test_exam_date_text := DATE_FORMAT(@test_exam_start, '%Y-%m-%d %H:%i:%s');

DELETE FROM exam_attempt WHERE exam_code = @test_exam_code;
DELETE FROM exam_shared_snapshot_item WHERE exam_code = @test_exam_code;
DELETE FROM exam_shared_snapshot WHERE exam_code = @test_exam_code;
DELETE FROM score WHERE examCode = @test_exam_code;

UPDATE exam_manage
SET paper_frozen_at = NULL,
    revoked_at = NULL,
    revoke_reason = NULL,
    examDate = @test_exam_date_text,
    exam_start_at = @test_exam_start,
    totalTime = 20
WHERE examCode = @test_exam_code;
