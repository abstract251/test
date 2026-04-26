import { createRouter, createWebHistory } from 'vue-router'
import { setupRouterGuards } from '@/router/guards'
import { commonRoutes } from '@/router/modules/common'
import { adminConsoleRoutes } from '@/router/modules/admin'
import { teacherConsoleRoutes } from '@/router/modules/teacher'
import { studentRoutes } from '@/router/modules/student'
import { AUTH_ROLES } from '@/utils/constants'

const routes = [
  ...commonRoutes,
  {
    path: '/console',
    component: () => import('@/layout/ConsoleLayout.vue'),
    meta: {
      requiresAuth: true,
      roles: [AUTH_ROLES.ADMIN, AUTH_ROLES.TEACHER]
    },
    redirect: '/console/home',
    children: [...adminConsoleRoutes, ...teacherConsoleRoutes]
  },
  studentRoutes
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

setupRouterGuards(router)

export default router
