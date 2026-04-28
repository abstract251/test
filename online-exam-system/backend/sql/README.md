# 数据库增量脚本

按文件名日期顺序执行。全量基础结构见仓库根路径 [online_exam.sql](/D:/test/online-exam-system/backend/sql/online_exam.sql)。

| 顺序 | 文件 | 说明 |
| --- | --- | --- |
| 1 | `exam_addition.sql` | 考试时间、共享快照、撤销审计、`exam_attempt` 等增量表结构 |
| 2 | `auth_security_upgrade.sql` | JWT refresh token 与鉴权升级所需表结构 |
| 3 | `2026-04-12_exam_attempt.sql` | `exam_attempt` 索引补齐与兼容脚本 |
| 4 | `2026-04-19_phase5_database_optimization.sql` | `Phase 5` 的 `paper_manage` 去重重建、核心索引补齐、读优化准备 |

执行后请同步登记到相关变更文档，并优先在测试库验证唯一约束、索引创建结果和历史数据清洗结果。
