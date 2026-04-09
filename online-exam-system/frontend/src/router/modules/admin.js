export const adminConsoleRoutes = [
  {
    path: 'home',
    name: 'console-home',
    component: () => import('@/views/common/Welcome.vue'),
    meta: {
      requiresAuth: true,
      roles: ['0', '1'],
      title: '控制台'
    }
  },
  {
    path: 'admin/teachers',
    name: 'admin-teacher-list',
    component: () => import('@/views/admin/teacher/TeacherList.vue'),
    meta: {
      requiresAuth: true,
      roles: ['0'],
      title: '教师管理'
    }
  },
  {
    path: 'admin/teachers/new',
    name: 'admin-teacher-create',
    component: () => import('@/views/admin/teacher/TeacherForm.vue'),
    meta: {
      requiresAuth: true,
      roles: ['0'],
      title: '新增教师'
    }
  },
  {
    path: 'admin/teachers/:teacherId/edit',
    name: 'admin-teacher-edit',
    component: () => import('@/views/admin/teacher/TeacherForm.vue'),
    meta: {
      requiresAuth: true,
      roles: ['0'],
      title: '编辑教师'
    }
  }
]
