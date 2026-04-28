import http from 'k6/http'
import { check, sleep } from 'k6'

const BASE_URL = __ENV.BASE_URL || 'http://localhost:18080'
const EXAM_CODE = __ENV.EXAM_CODE || '20991001'
const PASSWORD = __ENV.TEST_STUDENT_PASSWORD || '123456'
const USER_IDS = (__ENV.TEST_STUDENT_IDS || '20226001,20226002,20226003,20226004,20226005')
  .split(',')
  .map((value) => value.trim())
  .filter(Boolean)

export const options = {
  scenarios: {
    draft_flush_storm: {
      executor: 'per-vu-iterations',
      vus: USER_IDS.length,
      iterations: 20,
      maxDuration: '3m'
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

function login(studentId) {
  const response = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
    username: studentId,
    password: PASSWORD,
    role: 'STUDENT'
  }), {
    headers: { 'Content-Type': 'application/json; charset=utf-8' }
  })

  check(response, {
    'login http 200': (r) => r.status === 200,
    'login business 200': (r) => json(r).code === 200
  })

  return json(response).data?.accessToken || null
}

function authHeaders(token) {
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json; charset=utf-8'
  }
}

export default function () {
  const studentId = USER_IDS[(__VU - 1) % USER_IDS.length]
  const token = login(studentId)
  if (!token) {
    return
  }

  http.post(`${BASE_URL}/student/exam/${EXAM_CODE}/attempt/start`, null, {
    headers: authHeaders(token)
  })

  const response = http.put(`${BASE_URL}/student/exam/${EXAM_CODE}/attempt/answers`, JSON.stringify({
    answers: {
      '1_10001': `A-${__VU}-${__ITER}`,
      '2_10015': `draft-${Date.now()}`
    }
  }), {
    headers: authHeaders(token)
  })

  check(response, {
    'draft save http 200': (r) => r.status === 200,
    'draft save business 200': (r) => json(r).code === 200
  })

  sleep(0.2)
}
