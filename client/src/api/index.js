import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

// 请求拦截：自动带 Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截：401 跳登录
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.hash = '#/login'
    }
    return Promise.reject(err)
  }
)

// ── 认证 ──
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  me: () => api.get('/auth/me'),
}

// ── 词汇 ──
export const wordAPI = {
  list: (params) => api.get('/words', { params }),
  reviewList: () => api.get('/words/review'),
  lookup: (word) => api.get(`/words/lookup/${word}`),
  analyze: (word) => api.get(`/words/analyze/${encodeURIComponent(word)}`),
  add: (data) => api.post('/words', data),
  review: (id, quality) => api.patch(`/words/${id}/review`, { quality }),
  remove: (id) => api.delete(`/words/${id}`),
  // 词库
  bank: (params) => api.get('/words/bank', { params }),
  bankLevels: () => api.get('/words/bank/levels'),
  bankWords: (params) => api.get('/words/bank/words', { params }),
  bankImport: (data) => api.post('/words/bank/import', data),
}

// ── 写作 ──
export const writingAPI = {
  correct: (data) => api.post('/writing/correct', data),
  topic: (data) => api.post('/writing/topic', data),
  generate: (data) => api.post('/writing/generate', data),
  history: (params) => api.get('/writing/history', { params }),
  detail: (id) => api.get(`/writing/${id}`),
}

// ── 统计 ──
export const statsAPI = {
  overview: () => api.get('/stats/overview'),
  daily: () => api.get('/stats/daily'),
  streak: () => api.get('/stats/streak'),
  byLevel: () => api.get('/stats/by-level'),
}

// ── 真题 ──
export const papersAPI = {
  list: (params) => api.get('/papers', { params }),
  stats: () => api.get('/papers/stats'),
  detail: (id) => api.get(`/papers/${id}`),
}

// ── 翻译 ──
export const translateAPI = {
  translate: (data) => api.post('/translate', data),
}

export default api
