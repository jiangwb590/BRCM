import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)
  const menus = ref([])
  const permissions = ref([])
  
  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const username = computed(() => userInfo.value?.realName || userInfo.value?.username || '')
  const roleName = computed(() => userInfo.value?.roleName || '用户')
  
  // 方法
  async function loginAction(loginForm) {
    try {
      const res = await login(loginForm)
      token.value = res.data.token
      userInfo.value = res.data.user || res.data
      permissions.value = res.data.permissions || []
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('permissions', JSON.stringify(res.data.permissions || []))
      
      return res
    } catch (error) {
      throw error
    }
  }
  
  async function getUserInfoAction() {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      permissions.value = res.data.permissions || []
      localStorage.setItem('permissions', JSON.stringify(res.data.permissions || []))
      return res.data
    } catch (error) {
      throw error
    }
  }
  
  async function logoutAction() {
    try {
      await logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      clearUser()
    }
  }
  
  function clearUser() {
    token.value = ''
    userInfo.value = null
    menus.value = []
    permissions.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('permissions')
    router.push('/login')
  }
  
  function checkLogin() {
    const savedToken = localStorage.getItem('token')
    const savedPermissions = localStorage.getItem('permissions')
    if (savedToken && !token.value) {
      token.value = savedToken
    }
    if (savedPermissions && permissions.value.length === 0) {
      try {
        permissions.value = JSON.parse(savedPermissions)
      } catch (e) {
        permissions.value = []
      }
    }
  }
  
  /**
   * 检查是否有权限
   * @param {string|string[]} perms 权限标识或权限标识数组
   * @param {string} logical 逻辑关系 'and' | 'or'
   * @returns {boolean}
   */
  function hasPermission(perms, logical = 'or') {
    // 超级管理员拥有所有权限
    if (permissions.value.includes('*:*:*')) {
      return true
    }
    
    if (!perms) {
      return true
    }
    
    const permList = Array.isArray(perms) ? perms : [perms]
    
    if (logical === 'and') {
      return permList.every(p => permissions.value.includes(p))
    } else {
      return permList.some(p => permissions.value.includes(p))
    }
  }
  
  return {
    token,
    userInfo,
    menus,
    permissions,
    isLoggedIn,
    username,
    roleName,
    loginAction,
    getUserInfoAction,
    logoutAction,
    clearUser,
    checkLogin,
    hasPermission
  }
})