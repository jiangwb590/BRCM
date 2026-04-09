<template>
  <div class="detail-page">
    <div class="page-header">
      <div class="header-left">
        <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
        <h1 class="page-title">客户来源明细</h1>
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
        <div class="summary-value">{{ totalCustomers }}</div>
        <div class="summary-label">客户总数</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">{{ tableData.length }}</div>
        <div class="summary-label">来源渠道数</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">{{ topSource?.name || '-' }}</div>
        <div class="summary-label">主要来源</div>
      </div>
      <div class="summary-card">
        <div class="summary-value">{{ topSourcePercent }}%</div>
        <div class="summary-label">主要来源占比</div>
      </div>
    </div>
    
    <!-- 客户来源趋势图 -->
    <div class="trend-section">
      <div class="trend-header">
        <h3>客户来源趋势</h3>
        <div class="trend-filters">
          <el-radio-group v-model="trendDays" size="small" @change="fetchTrendData">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
          <el-select v-model="selectedChannel" placeholder="全部渠道" clearable style="width: 150px; margin-left: 12px" @change="fetchTrendData">
            <el-option v-for="item in tableData" :key="item.name" :label="item.name" :value="item.name" />
          </el-select>
        </div>
      </div>
      <div ref="trendChartRef" class="trend-chart" v-loading="trendLoading"></div>
    </div>
    
    <div class="content-wrapper">
      <div class="chart-section">
        <div ref="chartRef" class="chart-container" v-loading="loading"></div>
      </div>
      
      <div class="table-section">
        <el-table :data="tableData" v-loading="loading" border>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="name" label="来源渠道" min-width="120" />
          <el-table-column prop="value" label="客户数" width="100">
            <template #default="{ row }">
              <span class="customer-count">{{ row.value }}人</span>
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
import { getCustomerSource } from '@/api/dashboard'
import { getCustomerSourceTrend } from '@/api/analysis'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const trendLoading = ref(false)
const exportLoading = ref(false)
const tableData = ref([])
const dateRange = ref([])
const chartRef = ref(null)
const trendChartRef = ref(null)
let chart = null
let trendChart = null

// 趋势图数据
const trendDays = ref(7)
const selectedChannel = ref('')

const totalCustomers = computed(() => tableData.value.reduce((sum, item) => sum + (item.value || 0), 0))
const topSource = computed(() => {
  if (tableData.value.length === 0) return null
  return tableData.value.reduce((max, item) => (item.value > (max?.value || 0) ? item : max), tableData.value[0])
})
const topSourcePercent = computed(() => {
  if (!topSource.value || totalCustomers.value === 0) return '0.00'
  return ((topSource.value.value / totalCustomers.value) * 100).toFixed(2)
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
    
    const res = await getCustomerSource(startDate, endDate)
    const data = res.data?.data || {}
    tableData.value = Object.entries(data).map(([name, value]) => ({ name, value }))
    
    await nextTick()
    initChart()
  } catch (e) {
    console.error('获取客户来源失败', e)
  } finally {
    loading.value = false
  }
}

async function fetchTrendData() {
  trendLoading.value = true
  try {
    const res = await getCustomerSourceTrend(trendDays.value, selectedChannel.value)
    const data = res.data || {}
    
    await nextTick()
    initTrendChart(data.dates || [], data.data || {})
  } catch (e) {
    console.error('获取客户来源趋势失败', e)
  } finally {
    trendLoading.value = false
  }
}

function initChart() {
  if (chart) chart.dispose()
  chart = echarts.init(chartRef.value)
  
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { orient: 'vertical', right: '5%', top: 'center', itemGap: 12, textStyle: { color: '#666' } },
    series: [{ 
      type: 'pie', 
      radius: ['40%', '70%'], 
      center: ['40%', '50%'],
      data: tableData.value, 
      color: ['#F86C9A', '#FFB8D1', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      labelLine: { show: false }
    }]
  })
}

function initTrendChart(dates, data) {
  if (trendChart) trendChart.dispose()
  trendChart = echarts.init(trendChartRef.value)
  
  // 构建series数据
  const colors = ['#F86C9A', '#FFB8D1', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD']
  const sources = Object.keys(data)
  const series = sources.map((source, index) => ({
    name: source,
    type: 'bar',
    stack: selectedChannel.value ? '' : 'total',
    data: data[source],
    itemStyle: {
      color: colors[index % colors.length],
      borderRadius: selectedChannel.value ? [4, 4, 0, 0] : 0
    }
  }))
  
  trendChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: sources,
      top: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#E5E7EB' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#666' },
      splitLine: { lineStyle: { color: '#F0F0F0' } }
    },
    series
  })
}

function getPercent(value) {
  if (totalCustomers.value === 0) return '0.00'
  return ((value / totalCustomers.value) * 100).toFixed(2)
}

async function handleExport() {
  exportLoading.value = true
  try {
    const exportData = [
      ['客户来源明细'],
      ['序号', '来源渠道', '客户数', '占比(%)'],
      ...tableData.value.map((item, index) => [
        index + 1,
        item.name,
        item.value,
        getPercent(item.value)
      ]),
      [],
      ['合计', '', totalCustomers.value, '100.00']
    ]
    
    const ws = XLSX.utils.aoa_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '客户来源明细')
    
    ws['!cols'] = [{ wch: 8 }, { wch: 15 }, { wch: 10 }, { wch: 10 }]
    
    XLSX.writeFile(wb, `客户来源明细_${new Date().toISOString().split('T')[0]}.xlsx`)
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
  fetchTrendData()
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
      .summary-value { font-size: 24px; font-weight: 600; color: #45B7D1; }
      .summary-label { font-size: 14px; color: #666; margin-top: 4px; }
    }
  }
  
  .trend-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.04);
    
    .trend-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h3 {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        margin: 0;
      }
      
      .trend-filters {
        display: flex;
        align-items: center;
      }
    }
    
    .trend-chart {
      height: 300px;
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
  
  .customer-count { font-weight: 600; color: #45B7D1; }
}

@media (max-width: 1200px) {
  .detail-page .content-wrapper { grid-template-columns: 1fr; }
}
</style>
