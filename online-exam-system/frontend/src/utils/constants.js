export const STORAGE_KEYS = {
  SESSION: 'oes_session',
  ACCESS_TOKEN: 'oes_access_token',
  REFRESH_TOKEN: 'oes_refresh_token',
  CURRENT_USER: 'oes_current_user',
  QUESTION_DRAFT: 'oes_question_draft'
}

export const ROLES = {
  ADMIN: '0',
  TEACHER: '1',
  STUDENT: '2'
}

export const AUTH_ROLES = {
  ADMIN: 'ADMIN',
  TEACHER: 'TEACHER',
  STUDENT: 'STUDENT'
}

export const AUTH_ROLE_OPTIONS = [
  { value: AUTH_ROLES.ADMIN, label: '管理员' },
  { value: AUTH_ROLES.TEACHER, label: '教师' },
  { value: AUTH_ROLES.STUDENT, label: '学生' }
]

export const AUTH_ROLE_LABELS = AUTH_ROLE_OPTIONS.reduce((accumulator, item) => {
  accumulator[item.value] = item.label
  return accumulator
}, {})

export const AUTH_ROLE_TO_LEGACY_ROLE = {
  [AUTH_ROLES.ADMIN]: ROLES.ADMIN,
  [AUTH_ROLES.TEACHER]: ROLES.TEACHER,
  [AUTH_ROLES.STUDENT]: ROLES.STUDENT
}

export const LEGACY_ROLE_TO_AUTH_ROLE = {
  [ROLES.ADMIN]: AUTH_ROLES.ADMIN,
  [ROLES.TEACHER]: AUTH_ROLES.TEACHER,
  [ROLES.STUDENT]: AUTH_ROLES.STUDENT
}

export const ROLE_LABELS = {
  [ROLES.ADMIN]: '管理员',
  [ROLES.TEACHER]: '教师',
  [ROLES.STUDENT]: '学生'
}

export const CONSOLE_ROLE_SCOPE = {
  [ROLES.ADMIN]: 'admin',
  [ROLES.TEACHER]: 'teacher'
}

export function resolveConsoleScope(role) {
  const normalizedRole = AUTH_ROLE_TO_LEGACY_ROLE[role] || role
  return CONSOLE_ROLE_SCOPE[normalizedRole] || CONSOLE_ROLE_SCOPE[ROLES.TEACHER]
}

export function buildConsolePath(role, path = '') {
  const basePath = `/console/${resolveConsoleScope(role)}`
  const normalizedPath = String(path).replace(/^\/+|\/+$/g, '')
  return normalizedPath ? `${basePath}/${normalizedPath}` : basePath
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

export const SCORE_DISTRIBUTION_LABELS = ['0-59', '60-69', '70-79', '80-89', '90-100']

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
  [AUTH_ROLES.ADMIN]: [
    { title: '控制台', path: '/console/home', icon: 'House' },
    { title: '管理员管理', path: buildConsolePath(AUTH_ROLES.ADMIN, 'admins'), icon: 'UserFilled' },
    { title: '教师管理', path: buildConsolePath(AUTH_ROLES.ADMIN, 'teachers'), icon: 'UserFilled' },
    { title: '学生管理', path: buildConsolePath(AUTH_ROLES.ADMIN, 'students'), icon: 'User' },
    { title: '考试管理', path: buildConsolePath(AUTH_ROLES.ADMIN, 'exams'), icon: 'Reading' },
    { title: '全局题库管理', path: buildConsolePath(AUTH_ROLES.ADMIN, 'question-bank'), icon: 'EditPen' },
    { title: '成绩管理', path: buildConsolePath(AUTH_ROLES.ADMIN, 'grades'), icon: 'Histogram' },
    { title: '消息管理', path: buildConsolePath(AUTH_ROLES.ADMIN, 'messages'), icon: 'ChatDotRound' }
  ],
  [AUTH_ROLES.TEACHER]: [
    { title: '控制台', path: '/console/home', icon: 'House' },
    { title: '学生管理', path: buildConsolePath(AUTH_ROLES.TEACHER, 'students'), icon: 'User' },
    { title: '考试管理', path: buildConsolePath(AUTH_ROLES.TEACHER, 'exams'), icon: 'Reading' },
    { title: '题库工作台', path: buildConsolePath(AUTH_ROLES.TEACHER, 'questions'), icon: 'EditPen' },
    { title: '全局题库管理', path: buildConsolePath(AUTH_ROLES.TEACHER, 'question-bank'), icon: 'EditPen' },
    { title: '成绩管理', path: buildConsolePath(AUTH_ROLES.TEACHER, 'grades'), icon: 'Histogram' },
    { title: '消息管理', path: buildConsolePath(AUTH_ROLES.TEACHER, 'messages'), icon: 'ChatDotRound' }
  ]
}

export const STUDENT_NAV_ITEMS = [
  { title: '考试中心', path: '/student/home' },
  { title: '个人资料', path: '/student/profile' },
  { title: '我的成绩', path: '/student/scores' },
  { title: '消息中心', path: '/student/messages' }
]
