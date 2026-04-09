<template>
  <div class="detail-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
        <h1 class="page-title">员工业绩明细</h1>
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
        <div class="summary-value">¥{{ totalPerformance.toFixed(2) }}</div>
        <div class="summary-label">总业绩</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">¥{{ avgPerformance.toFixed(2) }}</div>
        <div class="summary-label">人均业绩</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">{{ topStaff?.name || '-' }}</div>
        <div class="summary-label">业绩冠军</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">¥{{ (topStaff?.value || 0).toFixed(2) }}</div>
        <div class="summary-label">最高业绩</div>
      </div>
    </div>
    
    <div class="content-wrapper">
      <div class="chart-section">
        <div ref="chartRef" class="chart-container" v-loading="loading"></div>
      </div>
      
      <div class="table-section">
        <el-table :data="tableData" v-loading="loading" border>
          <el-table-column type="index" label="排名" width="60" />
          <el-table-column prop="name" label="员工姓名" min-width="120" />
          <el-table-column prop="value" label="业绩金额">
            <template #default="{ row }">
              <span class="performance-amount">¥{{ row.value?.toFixed(2) || '0.00' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="占比" width="100">
            <template #default="{ row }">
              {{ getPercent(row.value) }}%
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Download } from '@element-plus/icons-vue'
import { getEmployeePerformance } from '@/api/dashboard'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const exportLoading = ref(false)
const tableData = ref([])
const dateRange = ref([])
const chartRef = ref(null)
let chart = null

const totalPerformance = computed(() => tableData.value.reduce((sum, item) => sum + (item.value || 0), 0))
const avgPerformance = computed(() => tableData.value.length > 0 ? totalPerformance.value / tableData.value.length : 0)
const topStaff = computed(() => {
  if (tableData.value.length === 0) return null
  return tableData.value.reduce((max, item) => (item.value > (max?.value || 0) ? item : max), tableData.value[0])
})

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
    
    const res = await getEmployeePerformance(100, startDate, endDate)
    const data = res.data?.data || []
    // 按业绩降序排列
    tableData.value = [...data].sort((a, b) => (b.value || 0) - (a.value || 0))
    
    await nextTick()
    initChart()
  } catch (e) {
    console.error('获取员工业绩失败', e)
  } finally {
    loading.value = false
  }
}

function initChart() {
  if (chart) chart.dispose()
  chart = echarts.init(chartRef.value)
  
  const names = tableData.value.map(item => item.name).reverse()
  const values = tableData.value.map(item => item.value || 0).reverse()
  
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>业绩: ¥{c}' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { 
      type: 'value', 
      axisLine: { show: false }, 
      axisLabel: { color: '#666', formatter: '¥{value}' }, 
      splitLine: { lineStyle: { color: '#F0F0F0' } } 
    },
    yAxis: { 
      type: 'category', 
      data: names, 
      axisLine: { lineStyle: { color: '#E5E7EB' } }, 
      axisLabel: { color: '#666' } 
    },
    series: [{ 
      type: 'bar', 
      data: values, 
      itemStyle: { 
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#FFB8D1' }, 
          { offset: 1, color: '#F86C9A' }
        ]), 
        borderRadius: [0, 4, 4, 0] 
      }, 
      barWidth: '60%' 
    }]
  })
}

function getPercent(value) {
  if (totalPerformance.value === 0) return '0.00'
  return ((value / totalPerformance.value) * 100).toFixed(2)
}

async function handleExport() {
  exportLoading.value = true
  try {
    const exportData = [
      ['员工业绩明细'],
      ['排名', '员工姓名', '业绩金额', '占比(%)'],
      ...tableData.value.map((item, index) => [
        index + 1,
        item.name,
        item.value?.toFixed(2) || '0.00',
        getPercent(item.value)
      ]),
      [],
      ['合计', '', totalPerformance.value.toFixed(2), '100.00']
    ]
    
    const ws = XLSX.utils.aoa_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '员工业绩明细')
    
    ws['!cols'] = [{ wch: 8 }, { wch: 15 }, { wch: 15 }, { wch: 10 }]
    
    XLSX.writeFile(wb, `员工业绩明细_${new Date().toISOString().split('T')[0]}.xlsx`)
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
      .summary-value { font-size: 24px; font-weight: 600; color: #F86C9A; }
      .summary-label { font-size: 14px; color: #666; margin-top: 4px; }
    }
  }
  
  .content-wrapper {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20px;
  }
  
  .chart-section, .table-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.04);
  }
  
  .chart-container { height: 400px; }
  
  .performance-amount { font-weight: 600; color: #F86C9A; }
}

@media (max-width: 1200px) {
  .detail-page .content-wrapper { grid-template-columns: 1fr; }
}
</style>
