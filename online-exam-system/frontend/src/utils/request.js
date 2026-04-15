import axios from 'axios'
import router from '@/router'
import { clearSession } from '@/utils/auth'

const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000,
  withCredentials: true
})

service.interceptors.response.use(
  (response) => response.data,
  async (error) => {
    if (error.response?.status === 401) {
      clearSession()
      if (router.currentRoute.value.name !== 'login') {
        await router.replace({
          name: 'login',
          query: {
            redirect: router.currentRoute.value.fullPath
          }
        })
      }
      return {
        code: 401,
        message: '登录状态已失效，请重新登录',
        data: null
      }
    }

    return Promise.reject(error)
  }
)

export default service
