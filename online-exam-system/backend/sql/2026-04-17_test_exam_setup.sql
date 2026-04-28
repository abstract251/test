-- 用于学生考试业务联调与 Prometheus 长期抓取期间的测试数据准备
-- 约定：
-- 1. 测试学生密码统一为 123456（直接复用当前库中可用 BCrypt 哈希）
-- 2. 测试考试开考时间固定为 2026-04-17 22:25:00，持续 20 分钟
-- 3. 本脚本可重复执行；会先清理同编号测试考试相关的会话、快照和成绩

SET NAMES utf8mb4;

SET @test_student_pwd_hash := '$2a$10$jLCyVGYZyIWKU78LwGoIS.VFGn0C5CFQd9eCyH.bMIy5sss/h9JDe';
SET @test_exam_code := 20991001;
SET @test_exam_start := DATE_ADD(NOW(), INTERVAL 5 MINUTE);
SET @test_exam_date_text := DATE_FORMAT(@test_exam_start, '%Y-%m-%d %H:%i:%s');
SET @test_grade := '2023';
SET @test_major := '联调专用';
SET @test_institute := '联调学院';

DELETE FROM exam_attempt WHERE exam_code = @test_exam_code;
DELETE FROM exam_shared_snapshot_item WHERE exam_code = @test_exam_code;
DELETE FROM exam_shared_snapshot WHERE exam_code = @test_exam_code;
DELETE FROM score WHERE examCode = @test_exam_code;
DELETE FROM exam_manage WHERE examCode = @test_exam_code;

DELETE FROM student WHERE studentId BETWEEN 20226001 AND 20226020;

INSERT INTO student(
    studentId, studentName, grade, major, clazz, institute, tel, email, pwd, cardId, sex, role
) VALUES
    (20226001, '联调学生01', @test_grade, @test_major, 'T1', @test_institute, '13900000001', 'test-student-01@example.com', @test_student_pwd_hash, '660000202604170001', '男', '2'),
    (20226002, '联调学生02', @test_grade, @test_major, 'T1', @test_institute, '13900000002', 'test-student-02@example.com', @test_student_pwd_hash, '660000202604170002', '女', '2'),
    (20226003, '联调学生03', @test_grade, @test_major, 'T1', @test_institute, '13900000003', 'test-student-03@example.com', @test_student_pwd_hash, '660000202604170003', '男', '2'),
    (20226004, '联调学生04', @test_grade, @test_major, 'T1', @test_institute, '13900000004', 'test-student-04@example.com', @test_student_pwd_hash, '660000202604170004', '女', '2'),
    (20226005, '联调学生05', @test_grade, @test_major, 'T1', @test_institute, '13900000005', 'test-student-05@example.com', @test_student_pwd_hash, '660000202604170005', '男', '2'),
    (20226006, '联调学生06', @test_grade, @test_major, 'T1', @test_institute, '13900000006', 'test-student-06@example.com', @test_student_pwd_hash, '660000202604170006', '女', '2'),
    (20226007, '联调学生07', @test_grade, @test_major, 'T2', @test_institute, '13900000007', 'test-student-07@example.com', @test_student_pwd_hash, '660000202604170007', '男', '2'),
    (20226008, '联调学生08', @test_grade, @test_major, 'T2', @test_institute, '13900000008', 'test-student-08@example.com', @test_student_pwd_hash, '660000202604170008', '女', '2'),
    (20226009, '联调学生09', @test_grade, @test_major, 'T2', @test_institute, '13900000009', 'test-student-09@example.com', @test_student_pwd_hash, '660000202604170009', '男', '2'),
    (20226010, '联调学生10', @test_grade, @test_major, 'T2', @test_institute, '13900000010', 'test-student-10@example.com', @test_student_pwd_hash, '660000202604170010', '女', '2'),
    (20226011, '联调学生11', @test_grade, @test_major, 'T3', @test_institute, '13900000011', 'test-student-11@example.com', @test_student_pwd_hash, '660000202604170011', '男', '2'),
    (20226012, '联调学生12', @test_grade, @test_major, 'T3', @test_institute, '13900000012', 'test-student-12@example.com', @test_student_pwd_hash, '660000202604170012', '女', '2'),
    (20226013, '联调学生13', @test_grade, @test_major, 'T3', @test_institute, '13900000013', 'test-student-13@example.com', @test_student_pwd_hash, '660000202604170013', '男', '2'),
    (20226014, '联调学生14', @test_grade, @test_major, 'T3', @test_institute, '13900000014', 'test-student-14@example.com', @test_student_pwd_hash, '660000202604170014', '女', '2'),
    (20226015, '联调学生15', @test_grade, @test_major, 'T3', @test_institute, '13900000015', 'test-student-15@example.com', @test_student_pwd_hash, '660000202604170015', '男', '2'),
    (20226016, '联调学生16', @test_grade, @test_major, 'T4', @test_institute, '13900000016', 'test-student-16@example.com', @test_student_pwd_hash, '660000202604170016', '女', '2'),
    (20226017, '联调学生17', @test_grade, @test_major, 'T4', @test_institute, '13900000017', 'test-student-17@example.com', @test_student_pwd_hash, '660000202604170017', '男', '2'),
    (20226018, '联调学生18', @test_grade, @test_major, 'T4', @test_institute, '13900000018', 'test-student-18@example.com', @test_student_pwd_hash, '660000202604170018', '女', '2'),
    (20226019, '联调学生19', @test_grade, @test_major, 'T4', @test_institute, '13900000019', 'test-student-19@example.com', @test_student_pwd_hash, '660000202604170019', '男', '2'),
    (20226020, '联调学生20', @test_grade, @test_major, 'T4', @test_institute, '13900000020', 'test-student-20@example.com', @test_student_pwd_hash, '660000202604170020', '女', '2');

INSERT INTO exam_manage(
    examCode, description, source, paperId, examDate, exam_start_at, totalTime,
    paper_frozen_at, grade, term, major, institute, totalScore, type, tips, revoked_at, revoke_reason
) VALUES (
    @test_exam_code,
    '学生考试业务联调专用考试',
    '在线考试系统联调',
    1015,
    @test_exam_date_text,
    @test_exam_start,
    20,
    NULL,
    @test_grade,
    '1',
    @test_major,
    @test_institute,
    100,
    '联调测试',
    '仅用于学生考试业务与监控联调，请勿用于真实考试。',
    NULL,
    NULL
);
