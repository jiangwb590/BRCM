<template>
  <div class="detail-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
        <h1 class="page-title">项目销量明细</h1>
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
        <div class="summary-value">{{ totalSales }}</div>
        <div class="summary-label">总销售次数</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">{{ tableData.length }}</div>
        <div class="summary-label">项目数量</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">{{ topProject?.name || '-' }}</div>
        <div class="summary-label">最畅销项目</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">{{ topProject?.value || 0 }}次</div>
        <div class="summary-label">最高销量</div>
      </div>
    </div>
    
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading" border>
        <el-table-column type="index" label="排名" width="60" />
        <el-table-column prop="name" label="项目名称" min-width="200" />
        <el-table-column prop="value" label="销售次数" width="120">
          <template #default="{ row }">
            <span class="sales-count">{{ row.value }}次</span>
          </template>
        </el-table-column>
        <el-table-column label="占比" width="120">
          <template #default="{ row }">
            {{ getPercent(row.value) }}%
          </template>
        </el-table-column>
        <el-table-column label="销量图示" min-width="200">
          <template #default="{ row }">
            <div class="bar-container">
              <div class="bar-fill" :style="{ width: getBarWidth(row.value) }"></div>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Download } from '@element-plus/icons-vue'
import { getProjectSales } from '@/api/dashboard'
import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const exportLoading = ref(false)
const tableData = ref([])
const dateRange = ref([])

const totalSales = computed(() => tableData.value.reduce((sum, item) => sum + (item.value || 0), 0))
const maxSales = computed(() => Math.max(...tableData.value.map(item => item.value || 0), 1))
const topProject = computed(() => tableData.value[0])

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
    
    const res = await getProjectSales(100, startDate, endDate)
    tableData.value = res.data?.data || []
  } catch (e) {
    console.error('获取项目销量失败', e)
  } finally {
    loading.value = false
  }
}

function getPercent(value) {
  if (totalSales.value === 0) return '0.00'
  return ((value / totalSales.value) * 100).toFixed(2)
}

function getBarWidth(value) {
  return `${(value / maxSales.value) * 100}%`
}

async function handleExport() {
  exportLoading.value = true
  try {
    const exportData = [
      ['项目销量明细'],
      ['排名', '项目名称', '销售次数', '占比(%)'],
      ...tableData.value.map((item, index) => [
        index + 1,
        item.name,
        item.value,
        getPercent(item.value)
      ])
    ]
    
    const ws = XLSX.utils.aoa_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '项目销量明细')
    
    ws['!cols'] = [{ wch: 8 }, { wch: 30 }, { wch: 12 }, { wch: 12 }]
    
    XLSX.writeFile(wb, `项目销量明细_${new Date().toISOString().split('T')[0]}.xlsx`)
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败', e)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
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
      .summary-value { font-size: 24px; font-weight: 600; color: #4ECDC4; }
      .summary-label { font-size: 14px; color: #666; margin-top: 4px; }
    }
  }
  
  .table-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.04);
  }
  
  .sales-count { font-weight: 600; color: #4ECDC4; }
  
  .bar-container {
    width: 100%;
    height: 20px;
    background: #f0f0f0;
    border-radius: 4px;
    overflow: hidden;
    .bar-fill {
      height: 100%;
      background: linear-gradient(90deg, #4ECDC4, #45B7D1);
      border-radius: 4px;
      transition: width 0.3s ease;
    }
  }
}
</style>
