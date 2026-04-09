<template>
  <div class="dashboard-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">门店经营数据</h1>
      <p class="page-desc">实时掌握门店运营数据</p>
    </div>
    
    <!-- 核心指标卡片 -->
    <div class="core-stats">
      <div class="core-stat-card total-revenue">
        <div class="stat-main">
          <span class="stat-label">总业绩</span>
          <span class="stat-value">{{ formatMoney(overviewData.monthRevenue) }}</span>
        </div>
        <div class="stat-sub">
          <span>营业收入 {{ formatMoney(overviewData.totalRevenue) }}</span>
        </div>
      </div>
      <div class="core-stat-card visit-data">
        <div class="stat-main">
          <span class="stat-label">到店人次</span>
          <span class="stat-value">{{ overviewData.visitTimes || 0 }}</span>
        </div>
        <div class="stat-sub">
          <span>今日 {{ overviewData.todayVisitTimes || 0 }} 人次</span>
        </div>
      </div>
      <div class="core-stat-card member-data">
        <div class="stat-main">
          <span class="stat-label">会员数量</span>
          <span class="stat-value">{{ overviewData.totalMembers || 0 }}</span>
        </div>
        <div class="stat-sub">
          <span>客户总数 {{ overviewData.totalCustomers || 0 }} 人</span>
        </div>
      </div>
    </div>

    <!-- 业绩明细 -->
    <div class="section-card">
      <div class="section-header">
        <h3>业绩明细</h3>
        <el-tag type="info" size="small">本月</el-tag>
      </div>
      <div class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">项目业绩</span>
          <span class="detail-value primary">{{ formatMoney(overviewData.projectRevenue) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">卡业绩</span>
          <span class="detail-value primary">{{ formatMoney(overviewData.cardRevenue) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">产品业绩</span>
          <span class="detail-value primary">{{ formatMoney(overviewData.productRevenue) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">今日营收</span>
          <span class="detail-value">{{ formatMoney(overviewData.todayRevenue) }}</span>
        </div>
      </div>
    </div>

    <!-- 卡耗分析 -->
    <div class="section-card">
      <div class="section-header">
        <h3>卡耗分析</h3>
        <el-tag type="info" size="small">本月</el-tag>
      </div>
      <div class="detail-grid">
        <div class="detail-item">
          <span class="detail-label">总卡耗</span>
          <span class="detail-value">{{ formatMoney(overviewData.cardConsume) }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">卡耗业绩比</span>
          <span class="detail-value highlight">{{ overviewData.cardConsumeRatio || 0 }}</span>
        </div>
        <div class="detail-item tip-item" colspan="2">
          <el-icon><InfoFilled /></el-icon>
          <span>卡耗业绩比>1表示卡使用效率高，客户粘性好</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-section">
      <div class="chart-card">
        <div class="chart-header">
          <h3>营收趋势</h3>
          <el-radio-group v-model="revenueDays" size="small" @change="fetchRevenueTrend">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="revenueChartRef" class="chart-container"></div>
      </div>
      
      <div class="chart-card">
        <div class="chart-header">
          <h3>产品/耗材消耗比例趋势</h3>
          <el-radio-group v-model="consumptionDays" size="small" @change="fetchConsumptionTrend">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
        <div ref="consumptionChartRef" class="chart-container"></div>
      </div>
      
      <div class="chart-row">
        <div class="chart-card small">
          <div class="chart-header">
            <h3>客户来源分布</h3>
          </div>
          <div ref="sourceChartRef" class="chart-container small"></div>
        </div>
        
        <div class="chart-card small">
          <div class="chart-header">
            <h3>客户分类</h3>
          </div>
          <div ref="categoryChartRef" class="chart-container small"></div>
        </div>
      </div>
    </div>

    <!-- 排行榜区域 -->
    <div class="rank-section">
      <div class="rank-card">
        <div class="rank-header">
          <h3>员工业绩排名</h3>
          <el-button type="primary" link size="small" @click="showEmployeeDetail">查看详情</el-button>
        </div>
        <div class="rank-list">
          <div class="rank-item" v-for="(item, index) in employeeRank" :key="index">
            <div class="rank-no" :class="{'top': index < 3}">{{ index + 1 }}</div>
            <div class="rank-name">{{ item.name }}</div>
            <div class="rank-value">{{ formatMoney(item.value) }}</div>
          </div>
          <el-empty v-if="!employeeRank.length" description="暂无数据" :image-size="60" />
        </div>
      </div>
      
      <div class="rank-card">
        <div class="rank-header">
          <h3>热门项目排名</h3>
        </div>
        <div class="rank-list">
          <div class="rank-item" v-for="(item, index) in projectRank" :key="index">
            <div class="rank-no" :class="{'top': index < 3}">{{ index + 1 }}</div>
            <div class="rank-name">{{ item.name }}</div>
            <div class="rank-value">{{ item.value }} 次</div>
          </div>
          <el-empty v-if="!projectRank.length" description="暂无数据" :image-size="60" />
        </div>
      </div>
    </div>

    <!-- 列表区域 -->
    <div class="list-section">
      <div class="list-card">
        <div class="list-header">
          <h3>今日预约</h3>
          <el-button type="primary" link @click="router.push('/appointment')">
            查看全部
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <el-table :data="todayAppointments" style="width: 100%">
          <el-table-column prop="customerName" label="客户" width="120" />
          <el-table-column prop="projectName" label="项目" width="150" />
          <el-table-column prop="appointmentTime" label="时间" width="100">
            <template #default="{ row }">
              {{ row.startTime }} - {{ row.endTime }}
            </template>
          </el-table-column>
          <el-table-column prop="beauticianName" label="美容师" width="100" />
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!todayAppointments.length" description="暂无今日预约" />
      </div>
      
      <div class="list-card">
        <div class="list-header">
          <h3>待办事项</h3>
          <el-button type="primary" link @click="router.push('/customer-care')">
            查看全部
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="todo-list">
          <div class="todo-item" v-for="item in todoList" :key="item.id">
            <div class="todo-icon" :class="item.type">
              <el-icon>
                <component :is="getTodoIcon(item.type)" />
              </el-icon>
            </div>
            <div class="todo-content">
              <div class="todo-title">{{ item.title }}</div>
              <div class="todo-customer">{{ item.customerName }}</div>
            </div>
            <div class="todo-time">{{ item.time }}</div>
          </div>
          <el-empty v-if="!todoList.length" description="暂无待办事项" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { getOverview, getRevenueTrend, getCustomerSource, getCustomerCategory, getEmployeePerformance, getProjectSales, getConsumptionRatioTrend } from '@/api/dashboard'
import { getAppointmentByDate } from '@/api/appointment'
import { getTodayTasks } from '@/api/care'

const router = useRouter()

// 概览数据
const overviewData = ref({
  totalCustomers: 0,
  totalMembers: 0,
  todayNewCustomers: 0,
  todayAppointments: 0,
  todayRevenue: 0,
  monthRevenue: 0,
  projectRevenue: 0,
  cardRevenue: 0,
  productRevenue: 0,
  totalRevenue: 0,
  cardConsume: 0,
  cardConsumeRatio: 0,
  visitTimes: 0,
  visitCustomers: 0,
  oldCustomerVisits: 0,
  customerTrend: 0,
  revenueTrend: 0
})

// 营收趋势天数
const revenueDays = ref(7)

// 消耗比例趋势天数
const consumptionDays = ref(7)

// 图表引用
const revenueChartRef = ref(null)
const sourceChartRef = ref(null)
const categoryChartRef = ref(null)
const consumptionChartRef = ref(null)
let revenueChart = null
let sourceChart = null
let categoryChart = null
let consumptionChart = null

// 排名数据
const employeeRank = ref([])
const projectRank = ref([])

// 今日预约
const todayAppointments = ref([])

// 待办事项
const todoList = ref([])

// 获取概览数据
async function fetchOverview() {
  try {
    const res = await getOverview()
    const data = res.data
    overviewData.value = {
      totalCustomers: data.totalCustomers || 0,
      totalMembers: data.totalMembers || 0,
      todayNewCustomers: data.todayNewCustomers || 0,
      todayAppointments: data.todayAppointments || 0,
      todayRevenue: data.todayRevenue || 0,
      monthRevenue: data.monthRevenue || 0,
      projectRevenue: data.projectRevenue || 0,
      cardRevenue: data.cardRevenue || 0,
      productRevenue: data.productRevenue || 0,
      totalRevenue: data.totalRevenue || 0,
      cardConsume: data.cardConsume || 0,
      cardConsumeRatio: data.cardConsumeRatio || 0,
      visitTimes: data.visitTimes || 0,
      todayVisitTimes: data.todayVisitTimes || 0,
      visitCustomers: data.visitCustomers || 0,
      oldCustomerVisits: data.oldCustomerVisits || 0,
      customerTrend: data.customerTrend || 0,
      revenueTrend: data.revenueTrend || 0
    }
  } catch (e) {
    console.error('获取概览数据失败', e)
  }
}

// 获取营收趋势
async function fetchRevenueTrend() {
  try {
    const res = await getRevenueTrend(revenueDays.value)
    const data = res.data
    
    await nextTick()
    initRevenueChart(data.dates || [], data.revenues || [])
  } catch (e) {
    console.error('获取营收趋势失败', e)
  }
}

// 获取客户来源
async function fetchCustomerSource() {
  try {
    const res = await getCustomerSource()
    const data = res.data
    
    let chartData = []
    if (data.data) {
      chartData = Object.entries(data.data).map(([name, value]) => ({
        name,
        value
      }))
    }
    
    await nextTick()
    initSourceChart(chartData)
  } catch (e) {
    console.error('获取客户来源失败', e)
  }
}

// 获取客户分类
async function fetchCustomerCategory() {
  try {
    const res = await getCustomerCategory()
    const data = res.data
    
    let chartData = []
    if (data.data) {
      const categoryLabels = {
        'potential': '潜在客户',
        'new': '新客户',
        'old': '老客户',
        'vip': 'VIP客户',
        'sleep': '沉睡客户'
      }
      chartData = Object.entries(data.data).map(([name, value]) => ({
        name: categoryLabels[name] || name,
        value
      }))
    }
    
    await nextTick()
    initCategoryChart(chartData)
  } catch (e) {
    console.error('获取客户分类失败', e)
  }
}

// 获取消耗比例趋势
async function fetchConsumptionTrend() {
  try {
    const res = await getConsumptionRatioTrend(consumptionDays.value)
    const data = res.data
    
    await nextTick()
    initConsumptionChart(data.dates || [], data.productRatios || [], data.consumableRatios || [])
  } catch (e) {
    console.error('获取消耗比例趋势失败', e)
  }
}

// 获取员工业绩排名
async function fetchEmployeePerformance() {
  try {
    const res = await getEmployeePerformance(5)
    employeeRank.value = res.data?.data || []
  } catch (e) {
    console.error('获取员工业绩排名失败', e)
  }
}

// 获取项目销量排名
async function fetchProjectSales() {
  try {
    const res = await getProjectSales(5)
    projectRank.value = res.data?.data || []
  } catch (e) {
    console.error('获取项目销量排名失败', e)
  }
}

// 获取今日预约
async function fetchTodayAppointments() {
  try {
    const today = new Date().toISOString().split('T')[0]
    const res = await getAppointmentByDate(today)
    todayAppointments.value = res.data || []
  } catch (e) {
    console.error('获取今日预约失败', e)
  }
}

// 获取待办事项
async function fetchTodoList() {
  try {
    const res = await getTodayTasks()
    todoList.value = res.data || []
  } catch (e) {
    console.error('获取待办事项失败', e)
  }
}

// 格式化金额
function formatMoney(value) {
  if (!value) return '¥0'
  const num = typeof value === 'string' ? parseFloat(value) : value
  if (num >= 10000) {
    return (num / 10000).toFixed(2) + '万'
  }
  return '¥' + num.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 初始化营收图表
function initRevenueChart(dates, values) {
  if (!revenueChartRef.value) return
  
  if (revenueChart) {
    revenueChart.dispose()
  }
  
  revenueChart = echarts.init(revenueChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}<br/>营收: ¥{c}'
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
      axisLine: {
        lineStyle: {
          color: '#E5E7EB'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#666',
        formatter: '¥{value}'
      },
      splitLine: {
        lineStyle: {
          color: '#F0F0F0'
        }
      }
    },
    series: [{
      data: values,
      type: 'bar',
      barWidth: '50%',
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#F86C9A' },
          { offset: 1, color: '#FFB8D1' }
        ]),
        borderRadius: [4, 4, 0, 0]
      }
    }]
  }
  
  revenueChart.setOption(option)
}

// 初始化来源图表
function initSourceChart(data) {
  if (!sourceChartRef.value) return
  
  if (sourceChart) {
    sourceChart.dispose()
  }
  
  sourceChart = echarts.init(sourceChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      itemGap: 8,
      textStyle: {
        color: '#666',
        fontSize: 12
      }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      data: data,
      color: ['#F86C9A', '#FFB8D1', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7']
    }]
  }
  
  sourceChart.setOption(option)
}

// 初始化分类图表
function initCategoryChart(data) {
  if (!categoryChartRef.value) return
  
  if (categoryChart) {
    categoryChart.dispose()
  }
  
  categoryChart = echarts.init(categoryChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      itemGap: 8,
      textStyle: {
        color: '#666',
        fontSize: 12
      }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 6,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      data: data,
      color: ['#FFB8D1', '#4ECDC4', '#45B7D1', '#F86C9A', '#96CEB4']
    }]
  }
  
  categoryChart.setOption(option)
}

// 初始化消耗比例趋势图表
function initConsumptionChart(dates, productRatios, consumableRatios) {
  if (!consumptionChartRef.value) return
  
  if (consumptionChart) {
    consumptionChart.dispose()
  }
  
  consumptionChart = echarts.init(consumptionChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    legend: {
      data: ['产品消耗', '耗材消耗'],
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
      axisLine: {
        lineStyle: {
          color: '#E5E7EB'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: '#666',
        formatter: '{value}'
      },
      splitLine: {
        lineStyle: {
          color: '#F0F0F0'
        }
      }
    },
    series: [
      {
        name: '产品消耗',
        type: 'bar',
        data: productRatios,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#F86C9A' },
            { offset: 1, color: '#FFB8D1' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      },
      {
        name: '耗材消耗',
        type: 'bar',
        data: consumableRatios,
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#4ECDC4' },
            { offset: 1, color: '#7BE0D9' }
          ]),
          borderRadius: [4, 4, 0, 0]
        }
      }
    ]
  }
  
  consumptionChart.setOption(option)
}

// 获取状态类型
function getStatusType(status) {
  const types = {
    '待确认': 'warning',
    '已确认': 'primary',
    '服务中': '',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return types[status] || ''
}

// 获取待办图标
function getTodoIcon(type) {
  const icons = {
    '生日关怀': 'BirthdayCake',
    '消费关怀': 'ChatDotRound',
    '沉睡唤醒': 'AlarmClock',
    '回访提醒': 'Phone'
  }
  return icons[type] || 'Bell'
}

// 显示员工详情
function showEmployeeDetail() {
  router.push('/analysis')
}

// 窗口大小变化时重绘图表
function handleResize() {
  revenueChart?.resize()
  sourceChart?.resize()
  categoryChart?.resize()
  consumptionChart?.resize()
}

onMounted(() => {
  fetchOverview()
  fetchRevenueTrend()
  fetchCustomerSource()
  fetchCustomerCategory()
  fetchEmployeePerformance()
  fetchProjectSales()
  fetchTodayAppointments()
  fetchTodoList()
  fetchConsumptionTrend()
  
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  revenueChart?.dispose()
  sourceChart?.dispose()
  categoryChart?.dispose()
  consumptionChart?.dispose()
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  .page-header {
    margin-bottom: 24px;
    
    .page-title {
      font-size: 24px;
      font-weight: 600;
      color: #333;
      margin-bottom: 4px;
    }
    
    .page-desc {
      font-size: 14px;
      color: #666;
    }
  }
  
  // 核心指标卡片
  .core-stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
    margin-bottom: 20px;
  }
  
  .core-stat-card {
    background: linear-gradient(135deg, #F86C9A 0%, #FFB8D1 100%);
    border-radius: 16px;
    padding: 20px 24px;
    color: #fff;
    box-shadow: 0 8px 24px rgba(248, 108, 154, 0.25);
    
    &.total-revenue {
      background: linear-gradient(135deg, #F86C9A 0%, #FFB8D1 100%);
    }
    
    &.visit-data {
      background: linear-gradient(135deg, #4ECDC4 0%, #7BE0D9 100%);
      box-shadow: 0 8px 24px rgba(78, 205, 196, 0.25);
    }
    
    &.member-data {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      box-shadow: 0 8px 24px rgba(102, 126, 234, 0.25);
    }
    
    .stat-main {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      
      .stat-label {
        font-size: 14px;
        opacity: 0.9;
      }
      
      .stat-value {
        font-size: 28px;
        font-weight: 700;
      }
    }
    
    .stat-sub {
      font-size: 12px;
      opacity: 0.85;
      
      .divider {
        margin: 0 8px;
      }
    }
  }
  
  // 业绩明细卡片
  .section-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px 24px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    
    .section-header {
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
    }
  }
  
  .detail-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
  }
  
  .detail-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #F8F9FA;
    border-radius: 8px;
    
    .detail-label {
      font-size: 14px;
      color: #666;
    }
    
    .detail-value {
      font-size: 16px;
      font-weight: 600;
      color: #333;
      
      &.primary {
        color: #F86C9A;
      }
      
      &.highlight {
        color: #4ECDC4;
        font-size: 18px;
      }
    }
    
    &.tip-item {
      grid-column: span 2;
      background: #FFF8E1;
      color: #F9A825;
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
  
  // 图表区域
  .chart-section {
    margin-bottom: 20px;
  }
  
  .chart-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    
    .chart-header {
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
    }
    
    .chart-container {
      height: 300px;
      
      &.small {
        height: 200px;
      }
    }
  }
  
  .chart-row {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
  
  // 排行榜区域
  .rank-section {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 20px;
  }
  
  .rank-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    
    .rank-header {
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
    }
    
    .rank-list {
      .rank-item {
        display: flex;
        align-items: center;
        padding: 10px 0;
        border-bottom: 1px solid #F5F5F5;
        
        &:last-child {
          border-bottom: none;
        }
        
        .rank-no {
          width: 24px;
          height: 24px;
          border-radius: 50%;
          background: #F0F0F0;
          color: #999;
          font-size: 12px;
          font-weight: 600;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 12px;
          
          &.top {
            background: linear-gradient(135deg, #F86C9A 0%, #FFB8D1 100%);
            color: #fff;
          }
        }
        
        .rank-name {
          flex: 1;
          font-size: 14px;
          color: #333;
        }
        
        .rank-value {
          font-size: 14px;
          font-weight: 600;
          color: #F86C9A;
        }
      }
    }
  }
  
  // 列表区域
  .list-section {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
  
  .list-card {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    
    .list-header {
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
    }
  }
  
  .todo-list {
    .todo-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 0;
      border-bottom: 1px solid #F5F5F5;
      
      &:last-child {
        border-bottom: none;
      }
      
      .todo-icon {
        width: 36px;
        height: 36px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        
        &.生日关怀 {
          background: #FFB8D1;
        }
        
        &.消费关怀 {
          background: #4ECDC4;
        }
        
        &.沉睡唤醒 {
          background: #45B7D1;
        }
        
        &.回访提醒 {
          background: #96CEB4;
        }
      }
      
      .todo-content {
        flex: 1;
        
        .todo-title {
          font-size: 14px;
          color: #333;
          margin-bottom: 2px;
        }
        
        .todo-customer {
          font-size: 12px;
          color: #999;
        }
      }
      
      .todo-time {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

@media (max-width: 1200px) {
  .dashboard-page {
    .core-stats {
      grid-template-columns: 1fr;
    }
    
    .detail-grid {
      grid-template-columns: repeat(2, 1fr);
    }
    
    .chart-row {
      grid-template-columns: 1fr;
    }
    
    .rank-section {
      grid-template-columns: 1fr;
    }
    
    .list-section {
      grid-template-columns: 1fr;
    }
  }
}
</style>