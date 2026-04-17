import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const STUDENT_USERNAME = __ENV.STUDENT_USERNAME || '20224001';
const STUDENT_PASSWORD = __ENV.STUDENT_PASSWORD || 'Student@123';
const TEACHER_USERNAME = __ENV.TEACHER_USERNAME || '20081001';
const TEACHER_PASSWORD = __ENV.TEACHER_PASSWORD || 'Teacher@123';
const EXAM_CODE = __ENV.EXAM_CODE || '20230001';
const QUESTION_BANK_PAGE = __ENV.QUESTION_BANK_PAGE || '1';
const QUESTION_BANK_SIZE = __ENV.QUESTION_BANK_SIZE || '10';
const SMOKE_ONLY = String(__ENV.SMOKE_ONLY || 'false').toLowerCase() === 'true';

const shortProfile = {
  executor: 'constant-vus',
  vus: 1,
  duration: '20s',
};

const baselineProfile = {
  executor: 'constant-vus',
  vus: 5,
  duration: '1m',
};

export const options = {
  scenarios: {
    auth_login: {
      ...selectProfile(),
      exec: 'authLoginScenario',
    },
    student_exam_list: {
      ...selectProfile(),
      exec: 'studentExamListScenario',
      startTime: '5s',
    },
    student_attempt_start: {
      ...selectProfile(),
      exec: 'studentAttemptStartScenario',
      startTime: '10s',
    },
    student_answer_save: {
      ...selectProfile(),
      exec: 'studentAnswerSaveScenario',
      startTime: '15s',
    },
    student_attempt_submit: {
      ...selectProfile(),
      exec: 'studentAttemptSubmitScenario',
      startTime: '20s',
    },
    question_bank_page: {
      ...selectProfile(),
      exec: 'questionBankPageScenario',
      startTime: '25s',
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<3000', 'p(99)<5000'],
  },
};

function selectProfile() {
  return SMOKE_ONLY ? shortProfile : baselineProfile;
}

function login(username, password, role) {
  const response = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
    username,
    password,
    role,
  }), {
    headers: {
      'Content-Type': 'application/json',
    },
  });

  const ok = check(response, {
    'login http 200': (r) => r.status === 200,
    'login business 200': (r) => safeJson(r).code === 200,
    'login returns access token': (r) => Boolean(safeJson(r).data?.accessToken),
  });

  if (!ok) {
    return null;
  }

  return safeJson(response).data.accessToken;
}

function authHeaders(token) {
  return {
    Authorization: `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
}

function safeJson(response) {
  try {
    return response.json();
  } catch (error) {
    return {};
  }
}

export function authLoginScenario() {
  login(STUDENT_USERNAME, STUDENT_PASSWORD, 'STUDENT');
  sleep(1);
}

export function studentExamListScenario() {
  const token = login(STUDENT_USERNAME, STUDENT_PASSWORD, 'STUDENT');
  if (!token) {
    return;
  }

  const response = http.get(`${BASE_URL}/student/exams`, {
    headers: authHeaders(token),
  });

  check(response, {
    'student exams http 200': (r) => r.status === 200,
    'student exams business 200': (r) => safeJson(r).code === 200,
  });
  sleep(1);
}

export function studentAttemptStartScenario() {
  const token = login(STUDENT_USERNAME, STUDENT_PASSWORD, 'STUDENT');
  if (!token) {
    return;
  }

  const response = http.post(`${BASE_URL}/student/exam/${EXAM_CODE}/attempt/start`, null, {
    headers: authHeaders(token),
  });

  check(response, {
    'attempt start http 200': (r) => r.status === 200,
    'attempt start business 200/400': (r) => [200, 400].includes(safeJson(r).code),
  });
  sleep(1);
}

export function studentAnswerSaveScenario() {
  const token = login(STUDENT_USERNAME, STUDENT_PASSWORD, 'STUDENT');
  if (!token) {
    return;
  }

  http.post(`${BASE_URL}/student/exam/${EXAM_CODE}/attempt/start`, null, {
    headers: authHeaders(token),
  });

  const response = http.put(`${BASE_URL}/student/exam/${EXAM_CODE}/attempt/answers`, JSON.stringify({
    answers: {
      '1_10001': 'A',
      '2_10001': '示例答案',
    },
  }), {
    headers: authHeaders(token),
  });

  check(response, {
    'answer save http 200': (r) => r.status === 200,
    'answer save business 200': (r) => safeJson(r).code === 200,
  });
  sleep(1);
}

export function studentAttemptSubmitScenario() {
  const token = login(STUDENT_USERNAME, STUDENT_PASSWORD, 'STUDENT');
  if (!token) {
    return;
  }

  http.post(`${BASE_URL}/student/exam/${EXAM_CODE}/attempt/start`, null, {
    headers: authHeaders(token),
  });

  http.put(`${BASE_URL}/student/exam/${EXAM_CODE}/attempt/answers`, JSON.stringify({
    answers: {
      '1_10001': 'A',
      '2_10001': '示例答案',
      '3_10001': 'true',
    },
  }), {
    headers: authHeaders(token),
  });

  const response = http.post(`${BASE_URL}/student/exam/${EXAM_CODE}/attempt/submit`, null, {
    headers: authHeaders(token),
  });

  check(response, {
    'attempt submit http 200': (r) => r.status === 200,
    'attempt submit business 200/400': (r) => [200, 400].includes(safeJson(r).code),
  });
  sleep(1);
}

export function questionBankPageScenario() {
  const token = login(TEACHER_USERNAME, TEACHER_PASSWORD, 'TEACHER');
  if (!token) {
    return;
  }

  const response = http.get(`${BASE_URL}/question-bank/${QUESTION_BANK_PAGE}/${QUESTION_BANK_SIZE}`, {
    headers: authHeaders(token),
  });

  check(response, {
    'question bank page http 200': (r) => r.status === 200,
    'question bank page business 200': (r) => safeJson(r).code === 200,
  });
  sleep(1);
}
