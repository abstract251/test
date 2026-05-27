# 在线考试系统 - Code Wiki

## 目录
- [项目概述](#项目概述)
- [系统架构](#系统架构)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [数据库设计](#数据库设计)
- [核心功能模块](#核心功能模块)
- [关键类与组件](#关键类与组件)
- [API接口文档](#api接口文档)
- [部署与运行](#部署与运行)

---

## 项目概述

### 项目简介
在线考试系统是一个前后端分离的Web应用，提供完整的在线考试、题库管理、试卷管理、成绩统计等功能。系统支持管理员、教师、学生三种角色，实现了从题目录入到考试评分的完整闭环。

### 主要功能
- **用户认证与授权**：基于JWT的无状态认证，支持多角色权限控制
- **题库管理**：支持选择题、判断题、填空题三种题型的增删改查
- **试卷管理**：支持从题库选择题目组卷，灵活配置试卷结构
- **考试管理**：支持创建考试、分配考试、考试时间控制
- **学生考试**：支持学生在线答题、交卷
- **成绩管理**：支持成绩查询、成绩统计分析
- **消息交流**：支持学生与教师之间的消息互动

---

## 系统架构

### 整体架构
系统采用前后端分离架构：

```
┌─────────────────────────────────────────────────────────────┐
│                         前端 (Vue 3)                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  学生端  │  │  教师端  │  │  管理员  │  │  登录页  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ HTTP/HTTPS + JWT
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                   后端 (Spring Boot 3)                       │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Spring Security (JWT)                   │   │
│  └─────────────────────────────────────────────────────┘   │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  控制器  │  │  服务层  │  │  数据层  │  │  实体层  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    MySQL 数据库                              │
└─────────────────────────────────────────────────────────────┘
```

### 架构特点
1. **前后端分离**：前端Vue 3 + 后端Spring Boot 3，独立部署
2. **无状态认证**：使用JWT实现无状态身份验证
3. **角色权限**：基于Spring Security实现细粒度权限控制
4. **ORM框架**：使用MyBatis-Plus简化数据库操作
5. **响应式设计**：前端支持移动端适配（学生端）

---

## 技术栈

### 后端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.5 | 主框架 |
| Spring Security | 6.x | 安全认证框架 |
| MyBatis-Plus | 3.5.16 | ORM框架 |
| MySQL Connector | 8.x | 数据库驱动 |
| JWT (jjwt) | 0.12.5 | Token生成与验证 |
| Lombok | 1.18.32 | 简化代码 |
| Maven | - | 项目构建工具 |

### 前端技术栈
| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| Vue Router | 5.x | 路由管理 |
| Element Plus | 2.x | UI组件库 |
| Axios | 1.x | HTTP客户端 |
| Day.js | 1.x | 日期处理 |

---

## 项目结构

### 完整目录结构
```
online-exam-system/
├── backend/                          # 后端项目
│   ├── docs/                         # 文档目录
│   │   ├── JWT+Spring Security登录鉴权改造方案.md
│   │   ├── JWT登录鉴权联调说明.md
│   │   ├── apifox-openapi.json
│   │   ├── points-of-use-git.md
│   │   ├── 前端接口文档.md
│   │   ├── 当前后端已实现功能说明.md
│   │   └── 数据库变更记录.md
│   ├── sql/                          # 数据库脚本
│   │   ├── 2026-04-12_exam_attempt.sql
│   │   ├── README.md
│   │   ├── auth_security_upgrade.sql
│   │   ├── exam_addition.sql
│   │   └── online_exam.sql
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/test/oes/
│   │   │   │   ├── config/           # 配置类
│   │   │   │   │   └── SecurityConfig.java
│   │   │   │   ├── controller/       # 控制器层
│   │   │   │   │   ├── AdminController.java
│   │   │   │   │   ├── AnswerController.java
│   │   │   │   │   ├── ExamAdminController.java
│   │   │   │   │   ├── ExamManageController.java
│   │   │   │   │   ├── ExamSnapshotController.java
│   │   │   │   │   ├── FillQuestionController.java
│   │   │   │   │   ├── ItemController.java
│   │   │   │   │   ├── JudgeQuestionController.java
│   │   │   │   │   ├── LoginController.java
│   │   │   │   │   ├── MessageController.java
│   │   │   │   │   ├── MultiQuestionController.java
│   │   │   │   │   ├── PaperController.java
│   │   │   │   │   ├── QuestionBankController.java
│   │   │   │   │   ├── ReplayController.java
│   │   │   │   │   ├── ScoreController.java
│   │   │   │   │   ├── StudentController.java
│   │   │   │   │   ├── StudentExamController.java
│   │   │   │   │   └── TeacherController.java
│   │   │   │   ├── dto/              # 数据传输对象
│   │   │   │   │   └── auth/
│   │   │   │   │       ├── AuthTokenResponse.java
│   │   │   │   │       ├── LoginRequest.java
│   │   │   │   │       ├── LogoutRequest.java
│   │   │   │   │       └── RefreshTokenRequest.java
│   │   │   │   ├── entity/           # 实体类
│   │   │   │   │   ├── Admin.java
│   │   │   │   │   ├── ApiResult.java
│   │   │   │   │   ├── AuthRefreshToken.java
│   │   │   │   │   ├── ExamAttempt.java
│   │   │   │   │   ├── ExamManage.java
│   │   │   │   │   ├── ExamRevokeAudit.java
│   │   │   │   │   ├── ExamSharedSnapshot.java
│   │   │   │   │   ├── ExamSharedSnapshotItem.java
│   │   │   │   │   ├── FillQuestion.java
│   │   │   │   │   ├── JudgeQuestion.java
│   │   │   │   │   ├── Login.java
│   │   │   │   │   ├── Message.java
│   │   │   │   │   ├── MultiQuestion.java
│   │   │   │   │   ├── PaperManage.java
│   │   │   │   │   ├── Replay.java
│   │   │   │   │   ├── Score.java
│   │   │   │   │   ├── Student.java
│   │   │   │   │   └── Teacher.java
│   │   │   │   ├── exception/        # 异常处理
│   │   │   │   │   ├── ExamBusinessException.java
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   ├── mapper/           # MyBatis Mapper
│   │   │   │   │   ├── AdminMapper.java
│   │   │   │   │   ├── AuthRefreshTokenMapper.java
│   │   │   │   │   ├── ExamAttemptMapper.java
│   │   │   │   │   ├── ExamManageMapper.java
│   │   │   │   │   ├── ExamRevokeAuditMapper.java
│   │   │   │   │   ├── ExamSharedSnapshotItemMapper.java
│   │   │   │   │   ├── ExamSharedSnapshotMapper.java
│   │   │   │   │   ├── FillQuestionMapper.java
│   │   │   │   │   ├── JudgeQuestionMapper.java
│   │   │   │   │   ├── LoginMapper.java
│   │   │   │   │   ├── MessageMapper.java
│   │   │   │   │   ├── MultiQuestionMapper.java
│   │   │   │   │   ├── PaperMapper.java
│   │   │   │   │   ├── QuestionBankMapper.java
│   │   │   │   │   ├── ReplayMapper.java
│   │   │   │   │   ├── ScoreMapper.java
│   │   │   │   │   ├── StudentMapper.java
│   │   │   │   │   └── TeacherMapper.java
│   │   │   │   ├── security/         # 安全认证
│   │   │   │   │   ├── AccountRole.java
│   │   │   │   │   ├── ApiAccessDeniedHandler.java
│   │   │   │   │   ├── ApiAuthenticationEntryPoint.java
│   │   │   │   │   ├── CurrentUserService.java
│   │   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   │   ├── JwtProperties.java
│   │   │   │   │   ├── JwtTokenService.java
│   │   │   │   │   ├── LoginUser.java
│   │   │   │   │   └── PasswordService.java
│   │   │   │   ├── service/          # 服务层
│   │   │   │   │   ├── exam/         # 考试相关服务
│   │   │   │   │   ├── identity/     # 身份验证服务
│   │   │   │   │   ├── impl/         # 服务实现
│   │   │   │   │   └── ...           # 服务接口
│   │   │   │   ├── tool/             # 工具类
│   │   │   │   ├── util/             # 工具类
│   │   │   │   ├── vo/               # 视图对象
│   │   │   │   └── OnlineExamSystemApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/                     # 测试
│   └── pom.xml
│
├── frontend/                         # 前端项目
│   ├── docs/                         # 文档
│   ├── public/                       # 静态资源
│   ├── src/
│   │   ├── api/                      # API接口
│   │   ├── assets/                   # 资源文件
│   │   │   └── styles/
│   │   ├── bootstrap/                # 启动引导
│   │   ├── components/               # 组件
│   │   │   ├── charts/
│   │   │   ├── common/
│   │   │   ├── exam/
│   │   │   ├── forms/
│   │   │   └── message/
│   │   ├── composables/              # 组合式函数
│   │   ├── layout/                   # 布局
│   │   ├── plugins/                  # 插件
│   │   ├── router/                   # 路由
│   │   │   ├── modules/
│   │   │   ├── guards.js
│   │   │   └── index.js
│   │   ├── utils/                    # 工具函数
│   │   ├── views/                    # 页面视图
│   │   │   ├── admin/
│   │   │   ├── common/
│   │   │   ├── login/
│   │   │   ├── shared/
│   │   │   ├── student/
│   │   │   └── teacher/
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vue.config.js
│
├── 当前进度与后续工作说明.md
└── CODE_WIKI.md
```

---

## 数据库设计

### ER图概述
系统包含以下核心实体：用户（管理员/教师/学生）、考试、试卷、题目、成绩、消息等。

### 数据表说明

#### 1. admin - 管理员表
| 字段 | 类型 | 说明 |
|------|------|------|
| adminId | int | 管理员ID（主键） |
| adminName | varchar(20) | 姓名 |
| sex | varchar(2) | 性别 |
| tel | varchar(11) | 电话号码 |
| email | varchar(20) | 邮箱 |
| pwd | varchar(16) | 密码 |
| cardId | varchar(18) | 身份证号 |
| role | varchar(1) | 角色（0管理员） |

#### 2. teacher - 教师表
| 字段 | 类型 | 说明 |
|------|------|------|
| teacherId | int | 教师ID（主键） |
| teacherName | varchar(20) | 姓名 |
| institute | varchar(20) | 学院 |
| sex | varchar(2) | 性别 |
| tel | varchar(11) | 电话号码 |
| email | varchar(20) | 邮箱 |
| pwd | varchar(16) | 密码 |
| cardId | varchar(18) | 身份证号 |
| type | varchar(20) | 职称 |
| role | varchar(1) | 角色（1教师） |

#### 3. student - 学生表
| 字段 | 类型 | 说明 |
|------|------|------|
| studentId | int | 学生ID（主键） |
| studentName | varchar(20) | 姓名 |
| grade | varchar(4) | 年级 |
| major | varchar(20) | 专业 |
| clazz | varchar(10) | 班级 |
| institute | varchar(30) | 学院 |
| tel | varchar(11) | 电话号码 |
| email | varchar(30) | 邮箱 |
| pwd | varchar(16) | 密码 |
| cardId | varchar(18) | 身份证号 |
| sex | varchar(2) | 性别 |
| role | varchar(1) | 角色（2学生） |

#### 4. exam_manage - 考试管理表
| 字段 | 类型 | 说明 |
|------|------|------|
| examCode | int | 考试编号（主键） |
| description | varchar(50) | 考试介绍 |
| source | varchar(20) | 课程名称 |
| paperId | int | 试卷编号 |
| examDate | varchar(10) | 考试日期 |
| totalTime | int | 考试时长（分钟） |
| grade | varchar(10) | 年级 |
| term | varchar(10) | 学期 |
| major | varchar(20) | 专业 |
| institute | varchar(20) | 学院 |
| totalScore | int | 总分 |
| type | varchar(255) | 考试类型 |
| tips | varchar(255) | 考生须知 |

#### 5. paper_manage - 试卷管理表
| 字段 | 类型 | 说明 |
|------|------|------|
| paperId | int | 试卷编号 |
| questionType | int | 题目类型（1选择/2判断/3填空） |
| questionId | int | 题目编号 |

#### 6. multi_question - 选择题题库表
| 字段 | 类型 | 说明 |
|------|------|------|
| questionId | int | 试题编号（主键） |
| subject | varchar(20) | 考试科目 |
| question | varchar(255) | 问题题目 |
| answerA | varchar(255) | 选项A |
| answerB | varchar(255) | 选项B |
| answerC | varchar(255) | 选项C |
| answerD | varchar(255) | 选项D |
| rightAnswer | varchar(10) | 正确答案 |
| analysis | varchar(255) | 题目解析 |
| score | int | 分数 |
| section | varchar(20) | 所属章节 |
| level | varchar(1) | 难度等级 |

#### 7. judge_question - 判断题题库表
| 字段 | 类型 | 说明 |
|------|------|------|
| questionId | int | 试题编号（主键） |
| subject | varchar(20) | 考试科目 |
| question | varchar(255) | 试题内容 |
| answer | varchar(255) | 正确答案（T/F） |
| analysis | varchar(255) | 题目解析 |
| score | int | 分数 |
| level | varchar(1) | 难度等级 |
| section | varchar(20) | 所属章节 |

#### 8. fill_question - 填空题题库表
| 字段 | 类型 | 说明 |
|------|------|------|
| questionId | int | 试题编号（主键） |
| subject | varchar(20) | 考试科目 |
| question | varchar(255) | 试题内容 |
| answer | varchar(255) | 正确答案 |
| analysis | varchar(255) | 题目解析 |
| score | int | 分数 |
| level | varchar(5) | 难度等级 |
| section | varchar(20) | 所属章节 |

#### 9. score - 成绩管理表
| 字段 | 类型 | 说明 |
|------|------|------|
| scoreId | int | 分数编号（主键） |
| examCode | int | 考试编号 |
| studentId | int | 学号 |
| subject | varchar(20) | 课程名称 |
| ptScore | int | 是否及格（0不及格/1及格） |
| etScore | int | 成绩 |
| score | int | 试卷满分 |
| answerDate | varchar(10) | 答题日期 |

#### 10. message - 留言表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | 留言编号（主键） |
| title | varchar(20) | 标题 |
| content | varchar(255) | 留言内容 |
| time | date | 留言时间 |

#### 11. replay - 回复表
| 字段 | 类型 | 说明 |
|------|------|------|
| messageId | int | 留言编号 |
| replayId | int | 回复编号（主键） |
| replay | varchar(255) | 回复内容 |
| replayTime | date | 回复时间 |

#### 12. auth_refresh_token - Refresh Token表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | int | ID（主键） |
| user_id | int | 用户ID |
| account_type | varchar(20) | 账户类型 |
| refresh_token | varchar(500) | Refresh Token |
| jti | varchar(100) | JWT ID |
| expires_at | datetime | 过期时间 |
| created_at | datetime | 创建时间 |
| revoked | tinyint | 是否已撤销 |

#### 13. exam_attempt - 考试尝试表（新增）
用于记录学生考试过程

---

## 核心功能模块

### 1. 认证与授权模块
**功能描述**：负责用户登录、Token生成与刷新、权限验证

**核心组件**：
- [SecurityConfig.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/config/SecurityConfig.java) - Spring Security配置
- [JwtTokenService.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/security/JwtTokenService.java) - JWT服务
- [JwtAuthenticationFilter.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/security/JwtAuthenticationFilter.java) - JWT过滤器
- [AccountRole.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/security/AccountRole.java) - 角色枚举

**工作流程**：
```
用户登录 → 验证用户名密码 → 生成Access Token + Refresh Token
                                              ↓
请求API → Header携带Access Token → JWT过滤器验证 → 放行/拒绝
                                              ↓
Access Token过期 → 使用Refresh Token刷新 → 获取新的Token对
```

### 2. 题库管理模块
**功能描述**：支持选择题、判断题、填空题的增删改查，按科目、章节筛选

**核心组件**：
- [MultiQuestionController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/MultiQuestionController.java)
- [JudgeQuestionController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/JudgeQuestionController.java)
- [FillQuestionController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/FillQuestionController.java)
- [QuestionBankController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/QuestionBankController.java)

### 3. 试卷管理模块
**功能描述**：从题库选择题目组成试卷，管理试卷结构

**核心组件**：
- [PaperController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/PaperController.java)
- [ItemController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/ItemController.java)

### 4. 考试管理模块
**功能描述**：创建考试、配置考试信息、关联试卷、分配考试对象

**核心组件**：
- [ExamManageController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/ExamManageController.java)
- [ExamAdminController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/ExamAdminController.java)

### 5. 学生考试模块
**功能描述**：学生查看可参加的考试、在线答题、交卷

**核心组件**：
- [StudentExamController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/StudentExamController.java)
- [AnswerController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/AnswerController.java)

### 6. 成绩管理模块
**功能描述**：成绩查询、成绩统计

**核心组件**：
- [ScoreController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/ScoreController.java)

### 7. 消息交流模块
**功能描述**：学生留言、教师回复

**核心组件**：
- [MessageController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/MessageController.java)
- [ReplayController.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/controller/ReplayController.java)

---

## 关键类与组件

### 后端核心类

#### 1. OnlineExamSystemApplication
**文件**：[OnlineExamSystemApplication.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/OnlineExamSystemApplication.java)

**职责**：Spring Boot应用入口，配置MyBatis-Plus分页插件

**关键代码**：
```java
@SpringBootApplication
@MapperScan("com.test.oes.mapper")
public class OnlineExamSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlineExamSystemApplication.class, args);
    }

    @Bean
    public MybatisPlusInterceptor innerInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

#### 2. SecurityConfig
**文件**：[SecurityConfig.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/config/SecurityConfig.java)

**职责**：Spring Security安全配置，配置权限规则、CORS、JWT过滤器等

**关键配置**：
- 禁用CSRF，启用CORS
- 配置无状态会话管理
- 配置JWT认证过滤器
- 配置角色权限规则：
  - `/auth/login`、`/auth/refresh` 允许匿名访问
  - `/admin/**` 需要ADMIN角色
  - `/student/exam/**` 需要STUDENT角色
  - `/question-bank/**`、`/papers/**` 等需要ADMIN或TEACHER角色

#### 3. JwtTokenService
**文件**：[JwtTokenService.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/security/JwtTokenService.java)

**职责**：JWT Token的生成、解析、验证

**核心方法**：
- `generateAccessToken(LoginUser)` - 生成访问令牌
- `generateRefreshToken(LoginUser)` - 生成刷新令牌
- `parseAccessToken(String)` - 解析访问令牌
- `parseRefreshToken(String)` - 解析刷新令牌

**Token结构**：
Access Token包含：
- `sub` - 用户名
- `uid` - 用户ID
- `role` - 角色权限
- `accountType` - 账户类型
- `displayName` - 显示名称
- `tokenType` - "access"

#### 4. ApiResult
**文件**：[ApiResult.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/entity/ApiResult.java)

**职责**：统一API响应封装

**结构**：
```java
{
  code: int,        // 状态码
  message: String,  // 消息
  data: T           // 数据
}
```

#### 5. 实体类示例 - ExamManage
**文件**：[ExamManage.java](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/java/com/test/oes/entity/ExamManage.java)

**职责**：考试管理实体

### 前端核心组件

#### 1. main.js
**文件**：[main.js](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/frontend/src/main.js)

**职责**：Vue应用入口，初始化路由、Element Plus、认证会话

#### 2. request.js
**文件**：[request.js](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/frontend/src/utils/request.js)

**职责**：Axios实例封装，配置请求/响应拦截器、Token刷新逻辑

**核心功能**：
- 请求拦截器：自动添加Authorization头
- 响应拦截器：处理401错误、自动刷新Token
- Mock数据支持（开发环境）
- 统一错误处理

#### 3. 路由配置
**文件**：[router/index.js](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/frontend/src/router/index.js)

**职责**：前端路由配置，按角色分离路由

**路由结构**：
- 公共路由：登录页、404
- 管理后台路由（/console）：管理员和教师共用
- 学生端路由：学生专用页面

#### 4. 路由守卫
**文件**：[router/guards.js](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/frontend/src/router/guards.js)

**职责**：路由权限验证、认证检查

---

## API接口文档

### 认证接口

#### 1. 用户登录
- **URL**: `POST /auth/login`
- **请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 900,
    "user": {
      "userId": 1,
      "username": "admin",
      "displayName": "管理员",
      "accountType": "ADMIN"
    }
  }
}
```

#### 2. 刷新Token
- **URL**: `POST /auth/refresh`
- **请求体**:
```json
{
  "refreshToken": "string"
}
```
- **响应**: 同登录接口，返回新的Token对

#### 3. 获取当前用户信息
- **URL**: `GET /auth/me`
- **Authorization**: Bearer Token
- **响应**: 当前用户信息

#### 4. 退出登录
- **URL**: `POST /auth/logout`
- **Authorization**: Bearer Token

### 题库接口

#### 选择题管理
- `GET /MultiQuestions` - 获取选择题列表
- `POST /MultiQuestion` - 新增选择题
- `PUT /editMultiQuestion` - 编辑选择题
- `GET /MultiQuestion/{id}` - 获取选择题详情

#### 判断题管理
- `GET /JudgeQuestions` - 获取判断题列表
- `POST /JudgeQuestion` - 新增判断题
- `PUT /editJudgeQuestion` - 编辑判断题

#### 填空题管理
- `GET /FillQuestions` - 获取填空题列表
- `POST /FillQuestion` - 新增填空题
- `PUT /editFillQuestion` - 编辑填空题

### 试卷接口
- `GET /papers` - 获取试卷列表
- `GET /paper/{paperId}` - 获取试卷详情
- `GET /item/{paperId}` - 获取试卷题目
- `POST /item` - 添加题目到试卷
- `DELETE /item` - 从试卷移除题目

### 考试接口
- `GET /exams` - 获取考试列表
- `GET /exams/{examCode}` - 获取考试详情
- `POST /exam` - 新增考试
- `PUT /exam` - 编辑考试
- `DELETE /exam/{examCode}` - 删除考试

### 学生考试接口
- `GET /student/exams` - 获取学生可参加的考试
- `GET /student/exam/{examCode}` - 获取考试详情（学生视角）

### 成绩接口
- `GET /scores` - 获取成绩列表

### 用户管理接口

#### 教师管理
- `GET /teachers` - 获取教师列表
- `POST /teacher` - 新增教师
- `PUT /teacher` - 编辑教师
- `DELETE /teacher/{id}` - 删除教师

#### 学生管理
- `GET /students` - 获取学生列表
- `POST /student` - 新增学生
- `PUT /student` - 编辑学生
- `DELETE /student/{id}` - 删除学生

---

## 部署与运行

### 环境要求
- **JDK**: 17+
- **Node.js**: 16+
- **MySQL**: 8.0+
- **Maven**: 3.6+

### 后端部署

#### 1. 数据库配置
1. 创建数据库：`CREATE DATABASE online_exam DEFAULT CHARACTER SET utf8mb4;`
2. 执行初始化脚本：[online_exam.sql](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/sql/online_exam.sql)
3. 执行安全升级脚本：[auth_security_upgrade.sql](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/sql/auth_security_upgrade.sql)
4. 执行考试尝试表脚本：[2026-04-12_exam_attempt.sql](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/sql/2026-04-12_exam_attempt.sql)

#### 2. 配置文件修改
修改 [application.properties](file:///e:/DevelopProjects/OnlineExamSystem/test/online-exam-system/backend/src/main/resources/application.properties)：
```properties
# 数据库配置
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.url=jdbc:mysql://localhost:3306/online_exam?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true

# JWT配置（生产环境请修改）
app.security.jwt.secret=your_base64_encoded_secret_key
app.security.jwt.access-token-minutes=15
app.security.jwt.refresh-token-days=7
```

#### 3. 编译运行
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
后端服务将在 `http://localhost:8080` 启动

### 前端部署

#### 1. 安装依赖
```bash
cd frontend
npm install
```

#### 2. 开发模式运行
```bash
npm run serve
```
前端将在 `http://localhost:8080` 或其他可用端口启动

#### 3. 生产构建
```bash
npm run build
```
构建产物将输出到 `dist` 目录

### 默认账号
系统初始化时包含以下测试账号：

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | 9991 | 123456 |
| 教师 | 20081001 | 123456 |
| 学生 | 20224084 | 123456 |

---

## 开发指南

### 后端开发规范
1. **包结构**：controller → service → mapper
2. **统一响应**：使用 `ApiResult` 包装所有API响应
3. **异常处理**：使用 `GlobalExceptionHandler` 统一处理异常
4. **权限控制**：使用 `@PreAuthorize` 注解或SecurityConfig配置
5. **MyBatis-Plus**：继承 `BaseMapper` 获得基础CRUD功能

### 前端开发规范
1. **组件命名**：使用PascalCase
2. **API调用**：使用 `api/` 目录下的封装函数
3. **状态管理**：使用组合式函数或本地存储
4. **路由**：按模块组织路由配置
5. **样式**：使用Element Plus主题变量

### 安全注意事项
1. **密码加密**：使用BCrypt加密存储密码
2. **JWT安全**：
   - Access Token短期有效（15分钟）
   - Refresh Token长期有效（7天）
   - Token泄露可通过logout撤销
3. **CORS配置**：配置允许的源，生产环境禁用通配符
4. **SQL注入防护**：使用MyBatis-Plus参数化查询

---

## 常见问题

### 1. 跨域问题
确保后端CORS配置允许前端地址，修改 `app.security.allowed-origin-patterns` 配置

### 2. Token过期
前端已实现自动刷新Token机制，当Access Token过期时会自动使用Refresh Token刷新

### 3. 数据库连接失败
检查MySQL服务是否启动，用户名密码是否正确，数据库是否已创建

---

## 更新日志

### v1.0 (当前版本)
- 实现基础的用户认证与授权（JWT+Spring Security）
- 实现题库管理功能（选择/判断/填空三种题型）
- 实现试卷管理功能
- 实现考试管理功能
- 实现学生考试入口
- 实现学生/教师/管理员信息管理
- 实现前端Vue 3界面

---

## 参考资料
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Vue 3官方文档](https://vuejs.org/)
- [MyBatis-Plus文档](https://baomidou.com/)
- [Element Plus文档](https://element-plus.org/)
