<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
    </div>
    
    <!-- 登录卡片 -->
    <div class="login-card">
      <!-- Logo区域 -->
      <div class="logo-section">
        <div class="logo-icon">
          <img src="@/assets/logo-icon.svg" alt="初龄科技美肤" />
        </div>
        <div class="logo-text">
          <h1>初龄</h1>
          <p>科技美肤客户管理系统</p>
        </div>
      </div>
      
      <!-- 欢迎语 -->
      <h2 class="welcome-text">欢迎回来</h2>
      
      <!-- 登录表单 -->
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <label class="form-label">账号</label>
          <el-input
            v-model="loginForm.username"
            placeholder="请输入账号"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <label class="form-label">密码</label>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        
        <div class="form-options">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <a href="javascript:;" class="forgot-link">忘记密码？</a>
        </div>
        
        <el-button
          type="primary"
          size="large"
          class="login-btn"
          :loading="loading"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: '123456'
})

const loginRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

async function handleLogin() {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    await userStore.loginAction(loginForm)
    
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error('Login failed:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFF9FB 0%, #FDF7FA 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
  
  .circle {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.15;
  }
  
  .circle-1 {
    width: 280px;
    height: 280px;
    background-color: #FFD6E7;
    top: -50px;
    left: -50px;
  }
  
  .circle-2 {
    width: 320px;
    height: 320px;
    background-color: #FFE6F0;
    bottom: -80px;
    right: -80px;
  }
}

.login-card {
  width: 400px;
  padding: 48px 40px;
  background: #FFFFFF;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1;
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  
  .logo-icon {
    width: 72px;
    height: 72px;
    margin-right: 16px;
    
    img {
      width: 100%;
      height: 100%;
      object-fit: contain;
    }
  }
  
  .logo-text {
    text-align: left;
    
    h1 {
      font-size: 28px;
      font-weight: 700;
      color: #333;
      margin: 0;
      letter-spacing: 2px;
    }
    
    p {
      font-size: 14px;
      color: #666;
      margin: 4px 0 0;
    }
  }
}

.welcome-text {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin-bottom: 32px;
}

.login-form {
  .form-label {
    display: block;
    font-size: 14px;
    color: #555;
    margin-bottom: 8px;
  }
  
  :deep(.el-input__wrapper) {
    border-radius: 8px;
    height: 48px;
    
    &.is-focus {
      box-shadow: 0 0 0 1px #F86C9A inset;
    }
  }
  
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  
  :deep(.el-checkbox__label) {
    color: #333;
  }
  
  :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    background-color: #F86C9A;
    border-color: #F86C9A;
  }
  
  .forgot-link {
    font-size: 14px;
    color: #F86C9A;
    text-decoration: none;
    
    &:hover {
      color: #E84A85;
      text-decoration: underline;
    }
  }
}

.login-btn {
  width: 100%;
  height: 52px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 500;
  background: linear-gradient(135deg, #F86C9A 0%, #E84A85 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(248, 108, 154, 0.3);
  transition: all 0.3s ease;
  
  &:hover {
    background: linear-gradient(135deg, #E84A85 0%, #D63878 100%);
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(248, 108, 154, 0.4);
  }
  
  &:active {
    transform: translateY(0);
  }
}
</style>
