<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <div class="logo">
        <div class="logo-icon">
          <img src="@/assets/logo-icon.svg" alt="初龄" />
        </div>
        <div class="logo-text" v-show="!appStore.sidebarCollapsed">
          <span class="logo-title">初龄</span>
          <span class="logo-subtitle">科技美肤</span>
        </div>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        :collapse-transition="false"
        background-color="#2D3A4B"
        text-color="#BFCAE3"
        active-text-color="#F86C9A"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>数据概览</span>
        </el-menu-item>
        
        <el-sub-menu index="customer-module">
          <template #title>
            <el-icon><User /></el-icon>
            <span>客户管理</span>
          </template>
          <el-menu-item index="/customer">客户列表</el-menu-item>
          <el-menu-item index="/customer-care">客户维系</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="service-module">
          <template #title>
            <el-icon><Goods /></el-icon>
            <span>项目管理</span>
          </template>
          <el-menu-item index="/project">服务项目</el-menu-item>
          <el-menu-item index="/package">套餐管理</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/appointment">
          <el-icon><Calendar /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        
        <el-sub-menu index="consume-module">
          <template #title>
            <el-icon><List /></el-icon>
            <span>消费管理</span>
          </template>
          <el-menu-item index="/consume">消费记录</el-menu-item>
          <el-menu-item index="/recharge">客户充值</el-menu-item>
          <el-menu-item index="/purchase">套餐购买</el-menu-item>
          <el-menu-item index="/member-card">会员卡管理</el-menu-item>
          <el-menu-item index="/member-level">会员等级</el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="stock-module">
          <template #title>
            <el-icon><ShoppingBag /></el-icon>
            <span>进销存</span>
          </template>
          <el-menu-item index="/product">产品管理</el-menu-item>
          <el-menu-item index="/stock-in">入库管理</el-menu-item>
          <el-menu-item index="/stock-out">出库管理</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/message">
          <el-icon><Bell /></el-icon>
          <span>消息中心</span>
        </el-menu-item>
        
        <el-menu-item index="/analysis">
          <el-icon><TrendCharts /></el-icon>
          <span>数据分析</span>
        </el-menu-item>
        
        <el-sub-menu index="system-module">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user">用户管理</el-menu-item>
          <el-menu-item index="/system/role">角色管理</el-menu-item>
          <el-menu-item index="/system/menu">菜单管理</el-menu-item>
          <el-menu-item index="/system/dict">字典管理</el-menu-item>
          <el-menu-item index="/system/log">操作日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </aside>
    
    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="header-left">
          <el-icon 
            class="collapse-btn" 
            @click="appStore.toggleSidebar"
          >
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRoute.meta.title !== '数据概览'">
              {{ currentRoute.meta.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索客户、项目..."
              prefix-icon="Search"
              clearable
              @keyup.enter="handleSearch"
            />
          </div>
          
          <!-- 消息通知 -->
          <el-badge :value="unreadCount" :hidden="!unreadCount" class="notification-badge">
            <el-icon class="icon-btn" @click="router.push('/message')">
              <Bell />
            </el-icon>
          </el-badge>
          
          <!-- 用户下拉菜单 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.username?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ userStore.username }}</span>
                <span class="user-role">{{ userStore.roleName }}</span>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      
      <!-- 内容区域 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getUnreadCount } from '@/api/message'
import { ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const searchKeyword = ref('')
const unreadCount = ref(0)

const activeMenu = computed(() => route.path)
const currentRoute = computed(() => route)

// 搜索处理
function handleSearch() {
  if (searchKeyword.value.trim()) {
    // 可以跳转到搜索结果页或者触发搜索事件
    console.log('Search:', searchKeyword.value)
  }
}

// 下拉菜单命令处理
async function handleCommand(command) {
  switch (command) {
    case 'profile':
      // 跳转到个人中心
      break
    case 'password':
      // 打开修改密码对话框
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          type: 'warning'
        })
        userStore.logoutAction()
      } catch (e) {
        // 取消退出
      }
      break
  }
}

// 获取未读消息数
async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (e) {
    console.error('获取未读消息数失败', e)
  }
}

onMounted(() => {
  fetchUnreadCount()
})
</script>

<style lang="scss" scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 220px;
  height: 100%;
  background-color: #2D3A4B;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  
  &.collapsed {
    width: 64px;
  }
  
  .logo {
    height: 60px;
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 16px;
    background-color: #243447;
    
    .logo-icon {
      width: 36px;
      height: 36px;
      flex-shrink: 0;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }
    
    .logo-text {
      margin-left: 12px;
      display: flex;
      flex-direction: column;
      white-space: nowrap;
      
      .logo-title {
        font-size: 18px;
        font-weight: 600;
        color: #fff;
        line-height: 1.2;
      }
      
      .logo-subtitle {
        font-size: 10px;
        color: rgba(255, 255, 255, 0.6);
        line-height: 1.2;
      }
    }
  }
  
  .el-menu {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    border-right: none;
    padding-bottom: 20px;
    
    // 美化滚动条
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-track {
      background: #243447;
    }
    
    &::-webkit-scrollbar-thumb {
      background: #3D4F66;
      border-radius: 3px;
      
      &:hover {
        background: #4D6078;
      }
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #E5E7EB;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #666;
      
      &:hover {
        color: #F86C9A;
      }
    }
  }
  
  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .search-box {
      width: 300px;
      
      :deep(.el-input__wrapper) {
        border-radius: 20px;
        background-color: #F5F7FA;
      }
    }
    
    .notification-badge {
      .icon-btn {
        font-size: 20px;
        cursor: pointer;
        color: #666;
        
        &:hover {
          color: #F86C9A;
        }
      }
    }
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      
      .user-avatar {
        background-color: #F86C9A;
        color: #fff;
      }
      
      .user-detail {
        display: flex;
        flex-direction: column;
        
        .user-name {
          font-size: 14px;
          color: #333;
          font-weight: 500;
        }
        
        .user-role {
          font-size: 12px;
          color: #999;
        }
      }
    }
  }
}

.content {
  flex: 1;
  padding: 20px;
  overflow: auto;
  background-color: #F9F9F9;
}

// 页面切换动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 响应式布局适配
@media screen and (max-width: 1200px) {
  .header {
    .header-right {
      .search-box {
        width: 200px;
      }
    }
  }
}

@media screen and (max-width: 992px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 1000;
    
    &.collapsed {
      transform: translateX(-100%);
      width: 220px;
    }
  }
  
  .main-container {
    width: 100%;
    
    .header {
      .header-left {
        .collapse-btn {
          display: block;
        }
      }
      
      .header-right {
        .search-box {
          display: none;
        }
        
        .user-detail {
          display: none;
        }
      }
    }
  }
}

@media screen and (max-width: 768px) {
  .header {
    padding: 0 12px;
    
    .header-right {
      gap: 8px;
    }
  }
  
  .content {
    padding: 12px;
  }
}
</style>
