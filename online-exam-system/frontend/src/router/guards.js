import { getSession, resolveHomePath } from '@/utils/auth'
import { hasRoleAccess } from '@/utils/permission'

export function setupRouterGuards(router) {
  router.beforeEach((to) => {
    const session = getSession()
    const isAuthenticated = Boolean(session)

    if (to.meta.guestOnly && isAuthenticated) {
      return resolveHomePath(session.role)
    }

    if (to.meta.requiresAuth && !isAuthenticated) {
      return {
        name: 'login',
        query: {
          redirect: to.fullPath
        }
      }
    }

    if (to.meta.roles && isAuthenticated && !hasRoleAccess(to.meta.roles, session.role)) {
      return resolveHomePath(session.role)
    }

    return true
  })
}
