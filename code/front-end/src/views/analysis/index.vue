<template>
  <div class="analysis-page">
    <div class="page-header">
      <h1 class="page-title">数据分析</h1>
      <el-date-picker v-model="dateRange" type="daterange" start-placeholder="开始日期" end-placeholder="结束日期" @change="fetchData" />
    </div>
    
    <div class="chart-grid">
      <!-- 营收趋势 -->
      <div class="chart-card">
        <div class="card-header">
          <h3>营收趋势</h3>
          <el-button type="primary" link @click="goDetail('revenue')">
            查看明细
          </el-button>
        </div>
        <div ref="revenueChartRef" class="chart-container" v-loading="revenueLoading"></div>
      </div>
      
      <!-- 项目销量 -->
      <div class="chart-card">
        <div class="card-header">
          <h3>项目销量</h3>
          <el-button type="primary" link @click="goDetail('project')">
            查看明细
          </el-button>
        </div>
        <div ref="projectChartRef" class="chart-container" v-loading="projectLoading"></div>
      </div>
      
      <!-- 客户来源 -->
      <div class="chart-card">
        <div class="card-header">
          <h3>客户来源</h3>
          <el-button type="primary" link @click="goDetail('source')">
            查看明细
          </el-button>
        </div>
        <div ref="sourceChartRef" class="chart-container" v-loading="sourceLoading"></div>
      </div>
      
      <!-- 员工业绩 -->
      <div class="chart-card">
        <div class="card-header">
          <h3>员工业绩</h3>
          <el-button type="primary" link @click="goDetail('staff')">
            查看明细
          </el-button>
        </div>
        <div ref="staffChartRef" class="chart-container" v-loading="staffLoading"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getRevenueTrend, getCustomerSource, getEmployeePerformance, getProjectSales } from '@/api/dashboard'

const router = useRouter()
const dateRange = ref([])
const revenueChartRef = ref(null)
const projectChartRef = ref(null)
const sourceChartRef = ref(null)
const staffChartRef = ref(null)
let charts = []

const revenueLoading = ref(false)
const projectLoading = ref(false)
const sourceLoading = ref(false)
const staffLoading = ref(false)

// 跳转到明细页面
function goDetail(type) {
  let startDate = ''
  let endDate = ''
  if (dateRange.value && dateRange.value.length === 2) {
    startDate = dateRange.value[0].toISOString().split('T')[0]
    endDate = dateRange.value[1].toISOString().split('T')[0]
  }
  const query = startDate ? { startDate, endDate } : {}
  const routeMap = {
    revenue: '/analysis/revenue-detail',
    project: '/analysis/project-detail',
    source: '/analysis/source-detail',
    staff: '/analysis/staff-detail'
  }
  router.push({ path: routeMap[type], query })
}

async function fetchData() {
  await nextTick()
  let startDate = null
  let endDate = null
  if (dateRange.value && dateRange.value.length === 2) {
    startDate = dateRange.value[0].toISOString().split('T')[0]
    endDate = dateRange.value[1].toISOString().split('T')[0]
  }
  fetchRevenueTrend(startDate, endDate)
  fetchProjectSales(startDate, endDate)
  fetchCustomerSource(startDate, endDate)
  fetchEmployeePerformance(startDate, endDate)
}

async function fetchRevenueTrend(startDate, endDate) {
  revenueLoading.value = true
  try {
    const days = startDate ? null : 30
    const res = await getRevenueTrend(days, startDate, endDate)
    const data = res.data
    initRevenueChart(data.dates || [], data.revenues || [])
  } catch (e) {
    console.error('获取营收趋势失败', e)
    initRevenueChart([], [])
  } finally {
    revenueLoading.value = false
  }
}

async function fetchProjectSales(startDate, endDate) {
  projectLoading.value = true
  try {
    const res = await getProjectSales(10, startDate, endDate)
    const data = res.data?.data || []
    initProjectChart(data)
  } catch (e) {
    console.error('获取项目销量失败', e)
    initProjectChart([])
  } finally {
    projectLoading.value = false
  }
}

async function fetchCustomerSource(startDate, endDate) {
  sourceLoading.value = true
  try {
    const res = await getCustomerSource(startDate, endDate)
    const data = res.data?.data || {}
    const chartData = Object.entries(data).map(([name, value]) => ({ name, value }))
    initSourceChart(chartData)
  } catch (e) {
    console.error('获取客户来源失败', e)
    initSourceChart([])
  } finally {
    sourceLoading.value = false
  }
}

async function fetchEmployeePerformance(startDate, endDate) {
  staffLoading.value = true
  try {
    const res = await getEmployeePerformance(10, startDate, endDate)
    const data = res.data?.data || []
    initStaffChart(data)
  } catch (e) {
    console.error('获取员工业绩失败', e)
    initStaffChart([])
  } finally {
    staffLoading.value = false
  }
}

function initRevenueChart(dates, values) {
  const chart = echarts.init(revenueChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>营收: ¥{c}' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#E5E7EB' } }, axisLabel: { color: '#666' } },
    yAxis: { type: 'value', axisLine: { show: false }, axisLabel: { color: '#666', formatter: '¥{value}' }, splitLine: { lineStyle: { color: '#F0F0F0' } } },
    series: [{ 
      type: 'line', 
      smooth: true, 
      data: values, 
      areaStyle: { opacity: 0.3, color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#F86C9A' }, { offset: 1, color: '#fff' }]) }, 
      lineStyle: { color: '#F86C9A' }, 
      itemStyle: { color: '#F86C9A' } 
    }]
  })
  charts.push(chart)
}

function initProjectChart(data) {
  const chart = echarts.init(projectChartRef.value)
  const names = data.map(item => item.name || item.key || '')
  const values = data.map(item => item.value || 0)
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>销量: {c}次' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: names, axisLine: { lineStyle: { color: '#E5E7EB' } }, axisLabel: { color: '#666', rotate: names.length > 5 ? 30 : 0 } },
    yAxis: { type: 'value', axisLine: { show: false }, axisLabel: { color: '#666' }, splitLine: { lineStyle: { color: '#F0F0F0' } } },
    series: [{ type: 'bar', data: values, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#F86C9A' }, { offset: 1, color: '#FFB8D1' }]), borderRadius: [4, 4, 0, 0] } }]
  })
  charts.push(chart)
}

function initSourceChart(data) {
  const chart = echarts.init(sourceChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}人 ({d}%)' },
    legend: { orient: 'vertical', right: '5%', top: 'center', itemGap: 12, textStyle: { color: '#666' } },
    series: [{ 
      type: 'pie', 
      radius: ['40%', '70%'], 
      center: ['40%', '50%'],
      data: data, 
      color: ['#F86C9A', '#FFB8D1', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      labelLine: { show: false }
    }]
  })
  charts.push(chart)
}

function initStaffChart(data) {
  const chart = echarts.init(staffChartRef.value)
  const names = data.map(item => item.name || item.key || '').reverse()
  const values = data.map(item => item.value || 0).reverse()
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>业绩: ¥{c}' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value', axisLine: { show: false }, axisLabel: { color: '#666', formatter: '¥{value}' }, splitLine: { lineStyle: { color: '#F0F0F0' } } },
    yAxis: { type: 'category', data: names, axisLine: { lineStyle: { color: '#E5E7EB' } }, axisLabel: { color: '#666' } },
    series: [{ type: 'bar', data: values, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#FFB8D1' }, { offset: 1, color: '#F86C9A' }]), borderRadius: [0, 4, 4, 0] }, barWidth: '60%' }]
  })
  charts.push(chart)
}

function handleResize() { charts.forEach(c => c?.resize()) }

onMounted(() => { fetchData(); window.addEventListener('resize', handleResize) })
onUnmounted(() => { window.removeEventListener('resize', handleResize); charts.forEach(c => c?.dispose()) })
</script>

<style lang="scss" scoped>
.analysis-page {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; .page-title { font-size: 24px; font-weight: 600; } }
  .chart-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; }
  .chart-card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.04);
    .card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
      h3 { font-size: 16px; font-weight: 600; margin: 0; }
    }
    .chart-container { height: 300px; }
  }
}

@media (max-width: 1200px) {
  .analysis-page .chart-grid { grid-template-columns: 1fr; }
}
</style>
