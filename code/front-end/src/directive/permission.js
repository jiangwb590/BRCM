import { useUserStore } from '@/stores/user'

/**
 * 权限指令
 * 用法：
 * v-permission="'customer:add'" - 单个权限
 * v-permission="['customer:add', 'customer:edit']" - 多个权限（默认OR）
 * v-permission.and="['customer:add', 'customer:edit']" - 多个权限（AND）
 */
export const permission = {
  mounted(el, binding) {
    const userStore = useUserStore()
    const { value, modifiers } = binding
    
    if (!value) {
      return
    }
    
    const logical = modifiers.and ? 'and' : 'or'
    const hasPermission = userStore.hasPermission(value, logical)
    
    if (!hasPermission) {
      // 移除元素
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}

/**
 * 注册权限指令
 */
export function setupPermissionDirective(app) {
  app.directive('permission', permission)
}

export default permission
