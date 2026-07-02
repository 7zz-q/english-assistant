import { defineStore } from 'pinia'
import { authAPI } from '../api'

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token') || '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.user?.username || '',
  },
  actions: {
    async login(username, password) {
      const { data } = await authAPI.login({ username, password })
      this.token = data.token
      this.user = data.user
      localStorage.setItem('token', data.token)
    },
    async register(username, email, password) {
      const { data } = await authAPI.register({ username, email, password })
      this.token = data.token
      this.user = data.user
      localStorage.setItem('token', data.token)
    },
    async fetchUser() {
      try {
        const { data } = await authAPI.me()
        this.user = data.user
      } catch {
        this.logout()
      }
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
    }
  }
})
