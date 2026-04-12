# 数据库增量脚本

按文件名日期顺序执行。全量基准结构见仓库根路径 `backend/online_exam.sql`（开发/测试用）。

| 顺序 | 文件 | 说明 |
|------|------|------|
| 1 | `2026-04-11_exam_freeze_snapshot_audit.sql` | 考试开考时刻 `exam_start_at`、本场共用快照表、撤销审计、`admin.super_admin` |
| 2 | `2026-04-12_exam_attempt.sql` | 学生作答会话 `exam_attempt` |

执行后请在 `backend/docs/数据库变更记录.md` 登记（若团队启用该流程）。
