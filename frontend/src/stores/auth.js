import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { api } from '../api/index.js'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const email = ref(localStorage.getItem('email') || null)

  const isAuthenticated = computed(() => !!token.value)

  async function register(emailVal, password) {
    const res = await api.auth.register(emailVal, password)
    _setAuth(res.token, res.email)
  }

  async function login(emailVal, password) {
    const res = await api.auth.login(emailVal, password)
    _setAuth(res.token, res.email)
  }

  function logout() {
    token.value = null
    email.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('email')
  }

  function _setAuth(t, e) {
    token.value = t
    email.value = e
    localStorage.setItem('token', t)
    localStorage.setItem('email', e)
  }

  return { token, email, isAuthenticated, register, login, logout }
})
