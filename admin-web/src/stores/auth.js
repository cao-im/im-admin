import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('admin_token') || '',
    profile: JSON.parse(localStorage.getItem('admin_profile') || 'null')
  }),
  actions: {
    setSession(token, profile) {
      this.token = token
      this.profile = profile
      localStorage.setItem('admin_token', token)
      localStorage.setItem('admin_profile', JSON.stringify(profile))
    },
    logout() {
      this.token = ''
      this.profile = null
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_profile')
    },
    has(permission) {
      if (!permission) return true
      if (!this.profile) return false
      if (this.profile.superAdmin) return true
      return (this.profile.permissions || []).includes(permission)
    }
  }
})
