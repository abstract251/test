import axios from 'axios'
import router from '@/router'
import {
  clearSession,
  getAccessToken,
  getRefreshToken,
  setSessionFromAuthResponse
} from '@/utils/auth'

const baseURL = process.env.VUE_APP_API_BASE_URL || '/api'
const authFreePaths = ['/auth/login', '/auth/refresh', '/auth/logout']

const service = axios.create({
  baseURL,
  timeout: 10000
})

const refreshClient = axios.create({
  baseURL,
  timeout: 10000
})

let refreshPromise = null

function isAuthFreeRequest(config = {}) {
  return authFreePaths.some((path) => String(config.url || '').startsWith(path))
}

async function redirectToLogin() {
  const currentRoute = router.currentRoute.value
  if (currentRoute?.name === 'login') {
    return
  }

  await router.replace({
    name: 'login',
    query: {
      redirect: currentRoute?.fullPath || '/'
    }
  })
}

async function refreshAccessToken() {
  if (refreshPromise) {
    return refreshPromise
  }

  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    throw new Error('NO_REFRESH_TOKEN')
  }

  refreshPromise = refreshClient
    .post('/auth/refresh', { refreshToken })
    .then((response) => {
      const payload = response?.data
      if (payload?.code !== 200 || !payload?.data) {
        throw new Error(payload?.message || 'REFRESH_FAILED')
      }

      const session = setSessionFromAuthResponse(payload.data)
      if (!session) {
        throw new Error('INVALID_REFRESH_PAYLOAD')
      }

      return session.accessToken
    })
    .finally(() => {
      refreshPromise = null
    })

  return refreshPromise
}

service.interceptors.request.use((config) => {
  const nextConfig = { ...config }
  if (!isAuthFreeRequest(nextConfig)) {
    const accessToken = getAccessToken()
    if (accessToken) {
      nextConfig.headers = {
        ...(nextConfig.headers || {}),
        Authorization: `Bearer ${accessToken}`
      }
    }
  }
  return nextConfig
})

service.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    const { response, config } = error
    const originalConfig = config || {}

    if (response?.status !== 401) {
      return Promise.reject(error)
    }

    if (originalConfig.skipAuthRefresh || isAuthFreeRequest(originalConfig) || originalConfig._retry) {
      clearSession()
      await redirectToLogin()
      return {
        code: 401,
        message: response?.data?.message || '登录状态已失效，请重新登录',
        data: null
      }
    }

    try {
      const accessToken = await refreshAccessToken()
      originalConfig._retry = true
      originalConfig.headers = {
        ...(originalConfig.headers || {}),
        Authorization: `Bearer ${accessToken}`
      }
      return service(originalConfig)
    } catch (refreshError) {
      clearSession()
      await redirectToLogin()
      return {
        code: 401,
        message: '登录状态已失效，请重新登录',
        data: null
      }
    }
  }
)

export default service
