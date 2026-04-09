export const studentRoutes = {
  path: '/student',
  component: () => import('@/layout/StudentLayout.vue'),
  meta: {
    requiresAuth: true,
    roles: ['2']
  },
  redirect: '/student/home',
  children: [
    {
      path: 'home',
      name: 'student-home',
      component: () => import('@/views/student/exam/ExamCenter.vue'),
      meta: {
        requiresAuth: true,
        roles: ['2'],
        title: '考试中心'
      }
    },
    {
      path: 'exams/:examCode',
      name: 'student-exam-detail',
      component: () => import('@/views/student/exam/ExamDetail.vue'),
      meta: {
        requiresAuth: true,
        roles: ['2'],
        title: '考试详情'
      }
    },
    {
      path: 'exams/:examCode/answer',
      name: 'student-answer-paper',
      component: () => import('@/views/student/exam/AnswerPaper.vue'),
      meta: {
        requiresAuth: true,
        roles: ['2'],
        title: '在线答题'
      }
    },
    {
      path: 'profile',
      name: 'student-profile',
      component: () => import('@/views/student/profile/Profile.vue'),
      meta: {
        requiresAuth: true,
        roles: ['2'],
        title: '个人资料'
      }
    },
    {
      path: 'password',
      name: 'student-password',
      component: () => import('@/views/student/profile/Password.vue'),
      meta: {
        requiresAuth: true,
        roles: ['2'],
        title: '修改密码'
      }
    },
    {
      path: 'scores',
      name: 'student-score-placeholder',
      component: () => import('@/views/student/score/ScorePlaceholder.vue'),
      meta: {
        requiresAuth: true,
        roles: ['2'],
        title: '我的成绩'
      }
    }
  ]
}
