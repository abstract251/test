export function hasRoleAccess(routeRoles = [], currentRole = '') {
  if (!routeRoles || routeRoles.length === 0) {
    return true
  }

  return routeRoles.includes(currentRole)
}
