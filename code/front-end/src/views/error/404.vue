<template>
  <div class="not-found-page">
    <div class="content">
      <div class="error-code">404</div>
      <h1 class="error-title">页面未找到</h1>
      <p class="error-desc">抱歉，您访问的页面不存在</p>
      <el-button type="primary" @click="router.push('/')">返回首页</el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style lang="scss" scoped>
.not-found-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFF9FB 0%, #FDF7FA 100%);
  
  .content {
    text-align: center;
    
    .error-code {
      font-size: 120px;
      font-weight: 700;
      color: #F86C9A;
      line-height: 1;
      margin-bottom: 16px;
    }
    
    .error-title {
      font-size: 24px;
      color: #333;
      margin-bottom: 8px;
    }
    
    .error-desc {
      font-size: 14px;
      color: #666;
      margin-bottom: 24px;
    }
  }
}
</style>
