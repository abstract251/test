// src/api/mock.js
export default {
  // ========== 认证接口 ==========
  'POST /auth/login': {
    code: 200,
    message: '登录成功',
    data: {
      accessToken: 'mock-access-token-123',
      refreshToken: 'mock-refresh-token-456',
      expiresIn: 7200,
      user: {
        userId: '10001',
        username: 'teacher01',
        displayName: '王老师',
        role: 'TEACHER'
      }
    }
  },

  'POST /auth/refresh': {
    code: 200,
    message: '刷新成功',
    data: {
      accessToken: 'new-mock-access-token',
      refreshToken: 'new-mock-refresh-token',
      expiresIn: 7200,
      user: {
        userId: '10001',
        username: 'teacher01',
        displayName: '王老师',
        role: 'TEACHER'
      }
    }
  },

  'POST /auth/logout': {
    code: 200,
    message: '已退出',
    data: null
  },

  'GET /auth/me': {
    code: 200,
    data: {
      userId: '10001',
      username: 'teacher01',
      displayName: '王老师',
      role: 'TEACHER'
    }
  },

  // ========== 学生管理（教师/管理员用） ==========
  'GET /students/1/10/@/@/@/@/@/@': {   // 第一页，每页10条，全部筛选项为@
    code: 200,
    data: {
      records: [
        {
          studentId: '20230001',
          studentName: '张三',
          grade: '2023',
          major: '软件工程',
          clazz: '1班',
          institute: '计算机学院',
          tel: '13800138001',
          email: 'zhangsan@example.com',
          sex: '男'
        },
        {
          studentId: '20230002',
          studentName: '李四',
          grade: '2023',
          major: '软件工程',
          clazz: '1班',
          institute: '计算机学院',
          tel: '13800138002',
          email: 'lisi@example.com',
          sex: '女'
        }
      ],
      total: 2,
      size: 10,
      current: 1,
      pages: 1
    }
  },

  // ========== 考试列表 ==========
  'GET /exams': {
    code: 200,
    data: [
      {
        examCode: 1001,
        source: '计算机网络',
        description: '期中考试',
        paperId: 1,
        examDate: '2026-05-01 09:00:00',
        totalTime: 90,
        grade: '2023',
        term: '2025-2026-2',
        major: '软件工程',
        institute: '计算机学院',
        totalScore: 100,
        type: '正式考试',
        tips: '请独立完成'
      }
    ]
  },

  'GET /exams/1/10': {
    code: 200,
    data: {
      records: [
        {
          examCode: 1001,
          source: '计算机网络',
          description: '期中考试',
          paperId: 1,
          examDate: '2026-05-01 09:00:00',
          totalTime: 90,
          grade: '2023',
          term: '2025-2026-2',
          major: '软件工程',
          institute: '计算机学院',
          totalScore: 100,
          type: '正式考试',
          tips: '请独立完成'
        }
      ],
      total: 1,
      size: 10,
      current: 1
    }
  },

  // ========== 成绩统计 ==========
  'GET /score/statistics/1001': {
    code: 200,
    data: {
      avgScore: 78.5,
      maxScore: 95,
      minScore: 52,
      passRate: 0.75,
      totalCount: 20,
      distribution: [
        { scoreSegment: '0-59', count: 3 },
        { scoreSegment: '60-69', count: 4 },
        { scoreSegment: '70-79', count: 5 },
        { scoreSegment: '80-89', count: 6 },
        { scoreSegment: '90-100', count: 2 }
      ]
    }
  },

  // ========== 全局题库（第一页） ==========
  'GET /question-bank/1/10': {
    code: 200,
    data: {
      records: [
        {
          questionType: 1,
          questionTypeName: '选择题',
          questionId: 1001,
          subject: '计算机网络',
          question: 'TCP协议工作在OSI模型的哪一层？',
          section: '传输层',
          level: '中等',
          score: 2,
          answer: 'A',
          answerA: '网络层',
          answerB: '传输层',
          answerC: '应用层',
          answerD: '数据链路层'
        },
        {
          questionType: 2,
          questionTypeName: '填空题',
          questionId: 2001,
          subject: '计算机网络',
          question: 'HTTP默认端口号是____。',
          section: '应用层',
          level: '简单',
          score: 2,
          answer: '80'
        }
      ],
      total: 2,
      size: 10,
      current: 1,
      pages: 1
    }
  },

  // 可以根据你的实际页面继续补充其他接口的 mock 数据
  // 例如：试卷详情、题目新增/编辑返回等
}