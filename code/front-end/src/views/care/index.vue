<template>
  <div class="care-page">
    <div class="page-header">
      <h1 class="page-title">客户维系</h1>
    </div>
    
    <div class="stat-cards">
      <div class="stat-card"><div class="stat-value">{{ stats.today }}</div><div class="stat-label">今日待办</div></div>
      <div class="stat-card"><div class="stat-value">{{ stats.birthday }}</div><div class="stat-label">生日关怀</div></div>
      <div class="stat-card"><div class="stat-value">{{ stats.sleep }}</div><div class="stat-label">沉睡客户</div></div>
    </div>
    
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="careType" label="关怀类型" width="120">
          <template #default="{ row }"><el-tag size="small">{{ row.careType }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="content" label="关怀内容" min-width="200" />
        <el-table-column prop="planDate" label="计划日期" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button v-if="row.status === '待执行'" type="primary" link @click="handleExecute(row)">执行</el-button>
            <el-button v-if="row.status === '待执行'" type="info" link @click="handleCancel(row)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTodayTasks, executeCare, cancelCare } from '@/api/care'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const stats = reactive({ today: 0, birthday: 0, sleep: 0 })

async function fetchData() {
  loading.value = true
  try { const res = await getTodayTasks(); tableData.value = res.data || []; stats.today = tableData.value.length } finally { loading.value = false }
}

async function handleExecute(row) {
  try {
    await executeCare(row.id)
    ElMessage.success('已执行')
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

async function handleCancel(row) {
  try {
    await cancelCare(row.id)
    ElMessage.info('已取消')
    fetchData()
  } catch (e) {
    console.error(e)
  }
}

function getStatusType(status) { const types = { '待执行': 'warning', '已执行': 'success', '已取消': 'info' }; return types[status] || '' }

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
.care-page {
  .page-header { margin-bottom: 20px; .page-title { font-size: 24px; font-weight: 600; } }
  .stat-cards { display: flex; gap: 20px; margin-bottom: 20px;
    .stat-card { background: #fff; border-radius: 12px; padding: 20px; min-width: 120px; text-align: center; box-shadow: 0 4px 12px rgba(0,0,0,0.04);
      .stat-value { font-size: 28px; font-weight: 600; color: #F86C9A; }
      .stat-label { font-size: 14px; color: #666; margin-top: 4px; }
    }
  }
  .table-section { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
}
</style>
