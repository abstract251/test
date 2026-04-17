import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:18080';
const EXAM_CODE = __ENV.EXAM_CODE || '20991001';
const PASSWORD = __ENV.TEST_STUDENT_PASSWORD || '123456';
const USER_IDS = (__ENV.TEST_STUDENT_IDS || '20226001,20226002,20226003,20226004,20226005,20226006,20226007,20226008,20226009,20226010')
  .split(',')
  .map((value) => value.trim())
  .filter(Boolean);

export const options = {
  scenarios: {
    concurrent_student_exam_flow: {
      executor: 'per-vu-iterations',
      vus: USER_IDS.length,
      iterations: 1,
      maxDuration: '2m',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<3000', 'p(99)<5000'],
  },
};

function json(response) {
  try {
    return response.json();
  } catch (error) {
    return {};
  }
}

function authHeaders(token) {
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json; charset=utf-8',
  };
}

function login(studentId) {
  const response = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
    username: studentId,
    password: PASSWORD,
    role: 'STUDENT',
  }), {
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
    },
  });

  check(response, {
    'login http 200': (r) => r.status === 200,
    'login business 200': (r) => json(r).code === 200,
    'login returns access token': (r) => Boolean(json(r).data?.accessToken),
  });

  return json(response).data?.accessToken || null;
}

export default function () {
  const studentId = USER_IDS[__VU - 1];
  const token = login(studentId);
  if (!token) {
    return;
  }

  const listResponse = http.get(`${BASE_URL}/student/exams`, {
    headers: authHeaders(token),
  });
  check(listResponse, {
    'student exams http 200': (r) => r.status === 200,
    'student exams business 200': (r) => json(r).code === 200,
  });

  const startResponse = http.post(`${BASE_URL}/student/exam/${EXAM_CODE}/attempt/start`, null, {
    headers: authHeaders(token),
  });
  check(startResponse, {
    'attempt start http 200': (r) => r.status === 200,
    'attempt start business 200': (r) => json(r).code === 200,
  });

  const saveResponse = http.put(`${BASE_URL}/student/exam/${EXAM_CODE}/attempt/answers`, JSON.stringify({
    answers: {
      '1_10001': 'D',
      '1_10005': 'B',
      '1_10006': 'A',
      '1_10012': 'A',
      '1_10015': 'A',
      '2_10015': '\u63a5\u53e3\u670d\u52a1',
      '2_10017': '\u5206\u7ec4\u4ea4\u6362\u7f51',
      '3_10001': 'T',
      '3_10010': 'F',
    },
  }), {
    headers: authHeaders(token),
  });
  check(saveResponse, {
    'answer save http 200': (r) => r.status === 200,
    'answer save business 200': (r) => json(r).code === 200,
  });

  const submitResponse = http.post(`${BASE_URL}/student/exam/${EXAM_CODE}/attempt/submit`, null, {
    headers: authHeaders(token),
  });
  check(submitResponse, {
    'attempt submit http 200': (r) => r.status === 200,
    'attempt submit business 200': (r) => json(r).code === 200,
  });
}
