const BASE_URL = '/api/v1'

function getHeaders() {
  const token = localStorage.getItem('token')
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`
  return headers
}

async function request(method, path, body) {
  const options = { method, headers: getHeaders() }
  if (body !== undefined) options.body = JSON.stringify(body)

  const res = await fetch(`${BASE_URL}${path}`, options)

  if (res.status === 204) return null

  const data = await res.json().catch(() => null)

  if (!res.ok) {
    const message = data?.message || `エラーが発生しました (${res.status})`
    throw new Error(message)
  }

  return data
}

export const api = {
  auth: {
    register: (email, password) =>
      request('POST', '/auth/register', { email, password }),
    login: (email, password) =>
      request('POST', '/auth/login', { email, password }),
  },

  categories: {
    getAll: () => request('GET', '/categories'),
  },

  recipes: {
    getAll: (params = {}) => {
      const query = new URLSearchParams()
      if (params.name) query.set('name', params.name)
      if (params.categoryId) query.set('categoryId', params.categoryId)
      const qs = query.toString()
      return request('GET', `/recipes${qs ? '?' + qs : ''}`)
    },
    create: (data) => request('POST', '/recipes', data),
    update: (id, data) => request('PUT', `/recipes/${id}`, data),
    delete: (id) => request('DELETE', `/recipes/${id}`),
  },
}
