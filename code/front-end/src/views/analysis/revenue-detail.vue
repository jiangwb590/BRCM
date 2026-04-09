<template>
  <div class="detail-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
        <h1 class="page-title">营收趋势明细</h1>
      </div>
      <div class="header-right">
        <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" @change="fetchData" />
        <el-button type="primary" @click="handleExport" :loading="exportLoading">
          <el-icon><Download /></el-icon>
          导出Excel
        </el-button>
      </div>
    </div>
    
    <div class="summary-cards">
      <div class="summary-card">
        <div class="summary-value">¥{{ totalRevenue.toFixed(2) }}</div>
        <div class="summary-label">总营收</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">¥{{ avgRevenue.toFixed(2) }}</div>
        <div class="summary-label">日均营收</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">¥{{ maxRevenue.toFixed(2) }}</div>
        <div class="summary-label">最高日营收</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">{{ tableData.length }}</div>
        <div class="summary-label">统计天数</div>
      </div>
    </div>
    
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="date" label="日期" width="150" />
        <el-table-column prop="revenue" label="营收金额">
          <template #default="{ row }">
            <span class="revenue-amount">¥{{ row.revenue?.toFixed(2) || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="较前日" width="120">
          <template #default="{ row, $index }">
            <span v-if="$index > 0" :class="getChangeClass(row.change)">
              {{ formatChange(row.change) }}
            </span>
            <span v-else style="color: #999">-</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="handlePageChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Download } from '@element-plus/icons-vue'
import { getRevenueTrend } from '@/api/dashboard'
import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const exportLoading = ref(false)
const tableData = ref([])
const allData = ref([])
const dateRange = ref([])

const queryParams = ref({ pageNum: 1, pageSize: 20 })
const total = ref(0)

const totalRevenue = computed(() => allData.value.reduce((sum, item) => sum + (item.revenue || 0), 0))
const avgRevenue = computed(() => allData.value.length > 0 ? totalRevenue.value / allData.value.length : 0)
const maxRevenue = computed(() => Math.max(...allData.value.map(item => item.revenue || 0), 0))

function goBack() {
  router.push('/analysis')
}

async function fetchData() {
  loading.value = true
  try {
    let startDate = null
    let endDate = null
    if (dateRange.value && dateRange.value.length === 2) {
      startDate = dateRange.value[0].toISOString().split('T')[0]
      endDate = dateRange.value[1].toISOString().split('T')[0]
    }
    
    const res = await getRevenueTrend(null, startDate, endDate)
    const data = res.data
    
    // 计算环比变化
    allData.value = (data.dates || []).map((date, index) => ({
      date,
      revenue: data.revenues?.[index] || 0,
      change: index > 0 ? (data.revenues?.[index] || 0) - (data.revenues?.[index - 1] || 0) : 0
    }))
    
    total.value = allData.value.length
    handlePageChange()
  } catch (e) {
    console.error('获取营收趋势失败', e)
  } finally {
    loading.value = false
  }
}

function handlePageChange() {
  const start = (queryParams.value.pageNum - 1) * queryParams.value.pageSize
  const end = start + queryParams.value.pageSize
  tableData.value = allData.value.slice(start, end)
}

function getChangeClass(change) {
  if (change > 0) return 'change-up'
  if (change < 0) return 'change-down'
  return ''
}

function formatChange(change) {
  if (change > 0) return `+¥${change.toFixed(2)}`
  if (change < 0) return `-¥${Math.abs(change).toFixed(2)}`
  return '¥0.00'
}

async function handleExport() {
  exportLoading.value = true
  try {
    const exportData = [
      ['营收趋势明细'],
      ['序号', '日期', '营收金额', '较前日变化'],
      ...allData.value.map((item, index) => [
        index + 1,
        item.date,
        item.revenue?.toFixed(2) || '0.00',
        item.change > 0 ? `+${item.change.toFixed(2)}` : item.change.toFixed(2)
      ])
    ]
    
    const ws = XLSX.utils.aoa_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '营收趋势明细')
    
    // 设置列宽
    ws['!cols'] = [{ wch: 8 }, { wch: 15 }, { wch: 15 }, { wch: 15 }]
    
    XLSX.writeFile(wb, `营收趋势明细_${new Date().toISOString().split('T')[0]}.xlsx`)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败', e)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  // 从路由参数获取日期范围
  if (route.query.startDate && route.query.endDate) {
    dateRange.value = [new Date(route.query.startDate), new Date(route.query.endDate)]
  }
  fetchData()
})
</script>

<style lang="scss" scoped>
.detail-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      .page-title { font-size: 24px; font-weight: 600; margin: 0; }
    }
    .header-right {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }
  
  .summary-cards {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
    .summary-card {
      background: #fff;
      border-radius: 12px;
      padding: 20px;
      min-width: 150px;
      text-align: center;
      box-shadow: 0 4px 12px rgba(0,0,0,0.04);
      .summary-value { font-size: 24px; font-weight: 600; color: #F86C9A; }
      .summary-label { font-size: 14px; color: #666; margin-top: 4px; }
    }
  }
  
  .table-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.04);
  }
  
  .revenue-amount { font-weight: 600; color: #F86C9A; }
  
  .change-up { color: #52c41a; }
  .change-down { color: #ff4d4f; }
  
  .pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
}
</style>
