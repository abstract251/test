import { AUTH_ROLES } from '@/utils/constants'

export const adminConsoleRoutes = [
  {
    path: 'home',
    name: 'console-home',
    component: () => import('@/views/common/Welcome.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN, AUTH_ROLES.TEACHER],
      title: '控制台'
    }
  },
  {
    path: 'admin/admins',
    name: 'admin-admin-list',
    component: () => import('@/views/admin/admin/AdminList.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '管理员管理'
    }
  },
  {
    path: 'admin/admins/new',
    name: 'admin-admin-create',
    component: () => import('@/views/admin/admin/AdminForm.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '新增管理员'
    }
  },
  {
    path: 'admin/admins/:adminId/edit',
    name: 'admin-admin-edit',
    component: () => import('@/views/admin/admin/AdminForm.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '编辑管理员'
    }
  },
  {
    path: 'admin/teachers',
    name: 'admin-teacher-list',
    component: () => import('@/views/admin/teacher/TeacherList.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '教师管理'
    }
  },
  {
    path: 'admin/teachers/new',
    name: 'admin-teacher-create',
    component: () => import('@/views/admin/teacher/TeacherForm.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '新增教师'
    }
  },
  {
    path: 'admin/teachers/:teacherId/edit',
    name: 'admin-teacher-edit',
    component: () => import('@/views/admin/teacher/TeacherForm.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '编辑教师'
    }
  },
  {
    path: 'admin/students',
    name: 'admin-student-list',
    component: () => import('@/views/teacher/student/StudentList.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '学生管理'
    }
  },
  {
    path: 'admin/students/new',
    name: 'admin-student-create',
    component: () => import('@/views/teacher/student/StudentForm.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '新增学生'
    }
  },
  {
    path: 'admin/students/:studentId/edit',
    name: 'admin-student-edit',
    component: () => import('@/views/teacher/student/StudentForm.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '编辑学生'
    }
  },
  {
    path: 'admin/exams',
    name: 'admin-exam-list',
    component: () => import('@/views/teacher/exam/ExamList.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '考试管理'
    }
  },
  {
    path: 'admin/exams/new',
    name: 'admin-exam-create',
    component: () => import('@/views/teacher/exam/ExamForm.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '新增考试'
    }
  },
  {
    path: 'admin/exams/:examCode/edit',
    name: 'admin-exam-edit',
    component: () => import('@/views/teacher/exam/ExamForm.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '编辑考试'
    }
  },
  {
    path: 'admin/exams/:examCode/paper',
    name: 'admin-paper-builder',
    component: () => import('@/views/teacher/exam/PaperBuilder.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '试卷编辑'
    }
  },
  {
    path: 'admin/questions',
    name: 'admin-question-workbench',
    component: () => import('@/views/teacher/question/QuestionWorkbench.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '题库工作台'
    }
  },
  {
    path: 'admin/questions/new',
    name: 'admin-question-create',
    component: () => import('@/views/teacher/question/QuestionForm.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '新增题目'
    }
  },
  {
    path: 'admin/questions/edit',
    name: 'admin-question-edit',
    component: () => import('@/views/teacher/question/QuestionForm.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '编辑题目'
    }
  },
  {
    path: 'admin/question-bank',
    name: 'admin-question-bank',
    component: () => import('@/views/shared/question/GlobalQuestionBank.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '全局题库管理'
    }
  },
  {
    path: 'admin/grades',
    name: 'admin-grade-center',
    component: () => import('@/views/shared/grade/GradeCenter.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '成绩管理'
    }
  },
  {
    path: 'admin/messages',
    name: 'admin-message-manage',
    component: () => import('@/views/shared/message/MessageManage.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN],
      title: '消息管理'
    }
  }
]
