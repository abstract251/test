import axios from 'axios'
import router from '@/router'
import {
  clearSession,
  getAccessToken,
  getRefreshToken,
  setSessionFromAuthResponse
} from '@/utils/auth'

// 在文件顶部导入 mock 数据
import mockData from '@/api/mock'

// 判断是否启用 Mock 模式（通过环境变量控制）
const USE_MOCK = process.env.NODE_ENV === 'development' && process.env.VUE_APP_USE_MOCK === 'true'

// 辅助函数：根据请求对象获取 mock 数据
function getMockResponse(config) {
  const method = (config.method || 'get').toUpperCase()
  let url = config.url || ''
  // 去掉 baseURL 前缀（如果 url 已经包含 baseURL）
  if (url.startsWith(baseURL)) {
    url = url.slice(baseURL.length)
  }
  // 去掉 query 参数（简化匹配，只匹配路径）
  const path = url.split('?')[0]
  const mockKey = `${method} ${path}`
  
  // 精确匹配
  if (mockData[mockKey]) {
    return Promise.resolve({ data: mockData[mockKey] })
  }
  
  // 可选：支持通配或正则匹配（例如 /student/数字 这种动态路径）
  for (const key in mockData) {
    const [mockMethod, mockPattern] = key.split(' ')
    if (mockMethod !== method) continue
    // 将 mockPattern 中的数字部分替换为正则 \d+
    const regexStr = mockPattern.replace(/\/\d+/g, '/\\d+')
    const regex = new RegExp(`^${regexStr}$`)
    if (regex.test(path)) {
      return Promise.resolve({ data: mockData[key] })
    }
  }
  
  console.warn(`[Mock] 未找到接口 ${method} ${path} 的 mock 数据`)
  return Promise.reject(new Error(`Mock 数据缺失: ${method} ${path}`))
}

const baseURL = 'http://localhost:8080'
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
    // 如果是 Mock 模式且请求出错（通常是连接不上后端时），返回 mock 数据
    if (USE_MOCK && error.config && (error.code === 'ERR_NETWORK' || error.message?.includes('Network Error'))) {
      try {
        const mockResponse = await getMockResponse(error.config)
        return mockResponse.data   // 直接返回 data 部分，让上层代码正常处理
      } catch (mockError) {
        // 如果没有 mock 数据，则继续抛出错误
        return Promise.reject(error)
      }
    }
    
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
