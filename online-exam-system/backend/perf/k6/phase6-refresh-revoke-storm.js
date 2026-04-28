import http from 'k6/http'
import { check, sleep } from 'k6'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:18080'
const PASSWORD = __ENV.TEST_TEACHER_PASSWORD || '123456'
const USERNAME = __ENV.TEST_TEACHER_ID || '20081001'

export const options = {
  scenarios: {
    refresh_revoke_storm: {
      executor: 'per-vu-iterations',
      vus: 5,
      iterations: 10,
      maxDuration: '2m'
    }
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<3000']
  }
}

function json(response) {
  try {
    return response.json()
  } catch (error) {
    return {}
  }
}

export default function () {
  const loginResponse = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
    username: USERNAME,
    password: PASSWORD,
    role: 'TEACHER'
  }), {
    headers: { 'Content-Type': 'application/json; charset=utf-8' }
  })

  check(loginResponse, {
    'login ok': (r) => r.status === 200 && json(r).code === 200
  })

  const accessToken = json(loginResponse).data?.accessToken
  const refreshToken = json(loginResponse).data?.refreshToken
  if (!refreshToken) {
    return
  }

  const refreshResponse = http.post(`${BASE_URL}/auth/refresh`, JSON.stringify({
    refreshToken
  }), {
    headers: { 'Content-Type': 'application/json; charset=utf-8' }
  })

  check(refreshResponse, {
    'refresh success or revoked': (r) => r.status === 200 && [200, 401].includes(json(r).code)
  })

  const logoutResponse = http.post(`${BASE_URL}/auth/logout`, JSON.stringify({
    refreshToken
  }), {
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json; charset=utf-8'
    }
  })

  check(logoutResponse, {
    'logout accepted': (r) => r.status === 200 && json(r).code === 200
  })

  sleep(0.1)
}
