import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)
  
  // 设备类型
  const device = ref('desktop')
  
  // 切换侧边栏
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }
  
  // 设置设备类型
  function setDevice(type) {
    device.value = type
  }
  
  return {
    sidebarCollapsed,
    device,
    toggleSidebar,
    setDevice
  }
})
