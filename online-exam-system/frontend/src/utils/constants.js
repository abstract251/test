export const STORAGE_KEYS = {
  SESSION: 'oes_session',
  QUESTION_DRAFT: 'oes_question_draft'
}

export const ROLES = {
  ADMIN: '0',
  TEACHER: '1',
  STUDENT: '2'
}

export const ROLE_LABELS = {
  [ROLES.ADMIN]: '管理员',
  [ROLES.TEACHER]: '教师',
  [ROLES.STUDENT]: '学生'
}

export const DEFAULT_PASSWORD = '123456'

export const QUESTION_TYPE_OPTIONS = [
  { value: 1, label: '选择题' },
  { value: 2, label: '填空题' },
  { value: 3, label: '判断题' }
]

export const QUESTION_TYPE_LABELS = QUESTION_TYPE_OPTIONS.reduce((accumulator, item) => {
  accumulator[item.value] = item.label
  return accumulator
}, {})

export const SEX_OPTIONS = [
  { value: '男', label: '男' },
  { value: '女', label: '女' }
]

export const SEX_VALUE_MAP = {
  M: '男',
  F: '女',
  男: '男',
  女: '女'
}

export function normalizeSex(value, fallback = '男') {
  return SEX_VALUE_MAP[value] || fallback
}

export const EXAM_TYPE_OPTIONS = [
  '正式考试',
  '阶段测验',
  '随堂练习'
]

export const TERM_OPTIONS = ['1', '2']
export const LEVEL_OPTIONS = ['基础', '中等', '提高']
export const INSTITUTE_OPTIONS = ['计算机学院', '软件工程学院', '信息工程学院']
export const MAJOR_OPTIONS = ['软件工程', '计算机科学与技术', '信息安全', '网络工程']
export const GRADE_OPTIONS = ['2022', '2023', '2024', '2025', '2026']
export const TEACHER_TYPE_OPTIONS = ['讲师', '副教授', '教授']

export const CONSOLE_MENU = {
  [ROLES.ADMIN]: [
    { title: '控制台', path: '/console/home', icon: 'House' },
    { title: '教师管理', path: '/console/admin/teachers', icon: 'UserFilled' }
  ],
  [ROLES.TEACHER]: [
    { title: '控制台', path: '/console/home', icon: 'House' },
    { title: '学生管理', path: '/console/teacher/students', icon: 'User' },
    { title: '考试管理', path: '/console/teacher/exams', icon: 'Reading' },
    { title: '题库工作台', path: '/console/teacher/questions', icon: 'EditPen' },
    { title: '成绩统计', path: '/console/teacher/grades', icon: 'Histogram' }
  ]
}

export const STUDENT_NAV_ITEMS = [
  { title: '考试中心', path: '/student/home' },
  { title: '个人资料', path: '/student/profile' },
  { title: '我的成绩', path: '/student/scores' }
]
