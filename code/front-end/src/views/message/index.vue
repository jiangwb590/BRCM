<template>
  <div class="message-page">
    <div class="page-header">
      <h1 class="page-title">消息中心</h1>
      <el-button type="primary" @click="markAllRead">全部已读</el-button>
    </div>
    
    <div class="message-tabs">
      <el-radio-group v-model="activeType" @change="fetchData">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="system">系统消息</el-radio-button>
        <el-radio-button value="appointment">预约提醒</el-radio-button>
        <el-radio-button value="stock">库存预警</el-radio-button>
      </el-radio-group>
    </div>
    
    <div class="message-list">
      <div v-for="msg in tableData" :key="msg.id" class="message-item" :class="{ unread: msg.isRead === 0 }" @click="viewMessage(msg)">
        <div class="msg-icon"><el-icon><Bell /></el-icon></div>
        <div class="msg-content">
          <div class="msg-title">{{ msg.title }}</div>
          <div class="msg-text">{{ msg.content }}</div>
          <div class="msg-time">{{ msg.createTime }}</div>
        </div>
        <el-tag v-if="msg.isRead === 0" type="danger" size="small">未读</el-tag>
      </div>
      <el-empty v-if="!tableData.length" description="暂无消息" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMessagePage, markAsRead, markAllAsRead } from '@/api/message'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const activeType = ref('')

async function fetchData() {
  loading.value = true
  try { const res = await getMessagePage({ messageType: activeType.value, pageSize: 50 }); tableData.value = res.data?.records || [] } finally { loading.value = false }
}

async function viewMessage(msg) {
  if (msg.isRead === 0) { await markAsRead(msg.id); msg.isRead = 1 }
}

async function markAllRead() {
  await markAllAsRead()
  ElMessage.success('已全部标记为已读')
  fetchData()
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
.message-page {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; .page-title { font-size: 24px; font-weight: 600; } }
  .message-tabs { margin-bottom: 20px; }
  .message-list { background: #fff; border-radius: 12px; padding: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.04);
    .message-item { display: flex; align-items: flex-start; gap: 16px; padding: 16px; border-radius: 8px; cursor: pointer; transition: background 0.2s;
      &:hover { background: #F5F7FA; }
      &.unread { background: #FFF9F9; }
      .msg-icon { width: 40px; height: 40px; background: #F86C9A; border-radius: 8px; display: flex; align-items: center; justify-content: center; color: #fff; }
      .msg-content { flex: 1;
        .msg-title { font-size: 15px; font-weight: 500; color: #333; margin-bottom: 4px; }
        .msg-text { font-size: 13px; color: #666; margin-bottom: 4px; }
        .msg-time { font-size: 12px; color: #999; }
      }
    }
  }
}
</style>
