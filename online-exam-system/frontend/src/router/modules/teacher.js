export const teacherConsoleRoutes = [
  {
    path: 'teacher/students',
    name: 'teacher-student-list',
    component: () => import('@/views/teacher/student/StudentList.vue'),
    meta: {
      requiresAuth: true,
      roles: ['1'],
      title: '学生管理'
    }
  },
  {
    path: 'teacher/students/new',
    name: 'teacher-student-create',
    component: () => import('@/views/teacher/student/StudentForm.vue'),
    meta: {
      requiresAuth: true,
      roles: ['1'],
      title: '新增学生'
    }
  },
  {
    path: 'teacher/students/:studentId/edit',
    name: 'teacher-student-edit',
    component: () => import('@/views/teacher/student/StudentForm.vue'),
    meta: {
      requiresAuth: true,
      roles: ['1'],
      title: '编辑学生'
    }
  },
  {
    path: 'teacher/exams',
    name: 'teacher-exam-list',
    component: () => import('@/views/teacher/exam/ExamList.vue'),
    meta: {
      requiresAuth: true,
      roles: ['1'],
      title: '考试管理'
    }
  },
  {
    path: 'teacher/exams/new',
    name: 'teacher-exam-create',
    component: () => import('@/views/teacher/exam/ExamForm.vue'),
    meta: {
      requiresAuth: true,
      roles: ['1'],
      title: '新增考试'
    }
  },
  {
    path: 'teacher/exams/:examCode/edit',
    name: 'teacher-exam-edit',
    component: () => import('@/views/teacher/exam/ExamForm.vue'),
    meta: {
      requiresAuth: true,
      roles: ['1'],
      title: '编辑考试'
    }
  },
  {
    path: 'teacher/exams/:examCode/paper',
    name: 'teacher-paper-builder',
    component: () => import('@/views/teacher/exam/PaperBuilder.vue'),
    meta: {
      requiresAuth: true,
      roles: ['1'],
      title: '试卷编辑'
    }
  },
  {
    path: 'teacher/questions',
    name: 'teacher-question-workbench',
    component: () => import('@/views/teacher/question/QuestionWorkbench.vue'),
    meta: {
      requiresAuth: true,
      roles: ['1'],
      title: '题库工作台'
    }
  },
  {
    path: 'teacher/questions/new',
    name: 'teacher-question-create',
    component: () => import('@/views/teacher/question/QuestionForm.vue'),
    meta: {
      requiresAuth: true,
      roles: ['1'],
      title: '新增题目'
    }
  },
  {
    path: 'teacher/questions/edit',
    name: 'teacher-question-edit',
    component: () => import('@/views/teacher/question/QuestionForm.vue'),
    meta: {
      requiresAuth: true,
      roles: ['1'],
      title: '编辑题目'
    }
  },
  {
    path: 'teacher/grades',
    name: 'teacher-grade-placeholder',
    component: () => import('@/views/teacher/grade/GradePlaceholder.vue'),
    meta: {
      requiresAuth: true,
      roles: ['1'],
      title: '成绩统计'
    }
  }
]
