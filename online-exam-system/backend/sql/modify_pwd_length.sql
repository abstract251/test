USE online_exam;

-- 修改 admin 表的 pwd 字段长度
ALTER TABLE admin MODIFY COLUMN pwd VARCHAR(100) NULL COMMENT '密码';

-- 修改 teacher 表的 pwd 字段长度
ALTER TABLE teacher MODIFY COLUMN pwd VARCHAR(100) NULL COMMENT '密码';

-- 修改 student 表的 pwd 字段长度
ALTER TABLE student MODIFY COLUMN pwd VARCHAR(100) NULL COMMENT '密码';