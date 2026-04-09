<template>
  <div class="appointment-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">预约管理</h1>
        <p class="page-desc">预约登记、日历排程、状态流转</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          新增预约
        </el-button>
      </div>
    </div>
    
    <!-- 视图切换 -->
    <div class="view-tabs">
      <el-radio-group v-model="viewMode" @change="handleViewChange">
        <el-radio-button value="calendar">日历视图</el-radio-button>
        <el-radio-button value="list">列表视图</el-radio-button>
      </el-radio-group>
    </div>
    
    <!-- 日历视图 -->
    <div v-show="viewMode === 'calendar'" class="calendar-section">
      <div class="calendar-header">
        <div class="nav-group">
          <el-button @click="prevMonth">
            <el-icon><ArrowLeft /></el-icon>
          </el-button>
          <el-button @click="goToday">今天</el-button>
          <el-button @click="nextMonth">
            <el-icon><ArrowRight /></el-icon>
          </el-button>
        </div>
        <h2 class="current-month">{{ currentYear }}年{{ currentMonth }}月</h2>
      </div>
      
      <div class="calendar-grid">
        <!-- 星期表头 -->
        <div class="calendar-weekdays">
          <div v-for="day in weekdays" :key="day" class="weekday">{{ day }}</div>
        </div>
        
        <!-- 日期网格 -->
        <div class="calendar-days">
          <div
            v-for="(day, index) in calendarDays"
            :key="index"
            class="calendar-day"
            :class="{ 'other-month': !day.currentMonth, 'today': day.isToday }"
            @click="selectDate(day)"
          >
            <div class="day-header">
              <span class="day-number">{{ day.day }}</span>
            </div>
            <div class="day-appointments">
              <div
                v-for="apt in day.appointments.slice(0, 3)"
                :key="apt.id"
                class="appointment-item"
                :style="{ backgroundColor: getServiceColor(apt.projectName) }"
                @click.stop="viewAppointment(apt)"
              >
                <span class="apt-time">{{ apt.startTime }}</span>
                <span class="apt-customer">{{ apt.customerName }}</span>
              </div>
              <div v-if="day.appointments.length > 3" class="more-btn">
                +{{ day.appointments.length - 3 }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 列表视图 -->
    <div v-show="viewMode === 'list'" class="list-section">
      <el-table :data="appointmentList" v-loading="loading">
        <el-table-column prop="appointmentNo" label="预约单号" width="180" />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="customerPhone" label="联系电话" width="130" />
        <el-table-column prop="projectName" label="服务项目" width="150" />
        <el-table-column prop="appointmentDate" label="预约日期" width="120" />
        <el-table-column label="预约时间" width="120">
          <template #default="{ row }">
            {{ row.startTime }} - {{ row.endTime }}
          </template>
        </el-table-column>
        <el-table-column prop="beauticianName" label="美容师" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewAppointment(row)">详情</el-button>
            <el-button v-if="row.status === '待确认'" type="success" link @click="confirmAppointment(row)">确认</el-button>
            <el-button v-if="row.status === '已确认'" type="warning" link @click="startService(row)">开始</el-button>
            <el-button v-if="row.status === '服务中'" type="success" link @click="completeService(row)">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchListData"
          @current-change="fetchListData"
        />
      </div>
    </div>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" title="新增预约" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="formData.customerId" filterable placeholder="选择客户" style="width: 100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="服务项目" prop="projectId">
          <el-select v-model="formData.projectId" placeholder="选择项目" style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="预约日期" prop="appointmentDate">
          <el-date-picker v-model="formData.appointmentDate" type="date" placeholder="选择日期" style="width: 100%" />
        </el-form-item>
        <el-form-item label="预约时间" prop="startTime">
          <el-time-picker v-model="formData.startTime" placeholder="开始时间" style="width: 48%" />
          <span style="margin: 0 2%">-</span>
          <el-time-picker v-model="formData.endTime" placeholder="结束时间" style="width: 48%" />
        </el-form-item>
        <el-form-item label="美容师" prop="beauticianId">
          <el-select v-model="formData.beauticianId" placeholder="选择美容师" style="width: 100%">
            <el-option v-for="s in staffOptions" :key="s.id" :label="s.realName" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 预约详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="预约详情" size="400px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="预约单号">{{ currentAppointment.appointmentNo }}</el-descriptions-item>
        <el-descriptions-item label="客户">{{ currentAppointment.customerName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentAppointment.customerPhone }}</el-descriptions-item>
        <el-descriptions-item label="服务项目">{{ currentAppointment.projectName }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ currentAppointment.appointmentDate }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ currentAppointment.startTime }} - {{ currentAppointment.endTime }}</el-descriptions-item>
        <el-descriptions-item label="美容师">{{ currentAppointment.beauticianName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentAppointment.status)">{{ currentAppointment.status }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentAppointment.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      
      <div class="drawer-actions">
        <el-button v-if="currentAppointment.status === '待确认'" type="success" @click="confirmAppointment(currentAppointment)">确认预约</el-button>
        <el-button v-if="currentAppointment.status === '已确认'" type="primary" @click="startService(currentAppointment)">开始服务</el-button>
        <el-button v-if="currentAppointment.status === '服务中'" type="success" @click="completeService(currentAppointment)">完成服务</el-button>
        <el-button v-if="['待确认', '已确认'].includes(currentAppointment.status)" type="danger" @click="cancelAppointment(currentAppointment)">取消预约</el-button>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getAppointmentPage, getAppointmentByDate, addAppointment, confirmAppointment as confirmApi, startService as startApi, completeService as completeApi, cancelAppointment as cancelApi } from '@/api/appointment'
import { getActiveProjects } from '@/api/project'
import { getCustomerPage } from '@/api/customer'
import { getBeauticians } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const viewMode = ref('calendar')
const loading = ref(false)
const submitLoading = ref(false)

// 日历相关
const currentYear = ref(dayjs().year())
const currentMonth = ref(dayjs().month() + 1)
const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
const calendarDays = ref([])

// 列表相关
const queryParams = reactive({ pageNum: 1, pageSize: 10 })
const appointmentList = ref([])
const total = ref(0)

// 对话框
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const formRef = ref(null)
const formData = reactive({
  customerId: null,
  projectId: null,
  appointmentDate: '',
  startTime: '',
  endTime: '',
  beauticianId: null,
  remark: ''
})
const formRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  appointmentDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}
const currentAppointment = ref({})

// 下拉选项
const customerOptions = ref([])
const projectOptions = ref([])
const staffOptions = ref([])

// 计算日历天数
async function generateCalendar() {
  const firstDay = dayjs(`${currentYear.value}-${currentMonth.value}-01`)
  const lastDay = firstDay.endOf('month')
  const days = []
  
  // 上月天数
  const prevMonthDays = firstDay.day()
  const prevMonth = firstDay.subtract(1, 'month')
  for (let i = prevMonthDays - 1; i >= 0; i--) {
    const date = prevMonth.endOf('month').subtract(i, 'day')
    days.push({ date: date.format('YYYY-MM-DD'), day: date.date(), currentMonth: false, isToday: false, appointments: [] })
  }
  
  // 当月天数
  const today = dayjs().format('YYYY-MM-DD')
  for (let i = 1; i <= lastDay.date(); i++) {
    const date = firstDay.date(i).format('YYYY-MM-DD')
    days.push({ date, day: i, currentMonth: true, isToday: date === today, appointments: [] })
  }
  
  // 下月天数
  const nextMonth = lastDay.add(1, 'day')
  const remaining = 42 - days.length
  for (let i = 1; i <= remaining; i++) {
    const date = nextMonth.date(i).format('YYYY-MM-DD')
    days.push({ date, day: i, currentMonth: false, isToday: false, appointments: [] })
  }
  
  calendarDays.value = days
  await fetchCalendarData()
}

// 获取日历数据
async function fetchCalendarData() {
  try {
    const firstDate = calendarDays.value[0]?.date
    const lastDate = calendarDays.value[calendarDays.value.length - 1]?.date
    
    const res = await getAppointmentPage({ startDate: firstDate, endDate: lastDate, pageSize: 100 })
    const data = res.data?.records || []
    
    // 按日期分组
    calendarDays.value.forEach(day => {
      day.appointments = data.filter(apt => apt.appointmentDate === day.date)
    })
  } catch (e) {
    console.error('获取日历数据失败', e)
  }
}

// 获取列表数据
async function fetchListData() {
  loading.value = true
  try {
    const res = await getAppointmentPage(queryParams)
    appointmentList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('获取列表失败', e)
  } finally {
    loading.value = false
  }
}

// 视图切换
function handleViewChange() {
  if (viewMode.value === 'list') {
    fetchListData()
  }
}

// 上月/下月
function prevMonth() {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
  generateCalendar()
}

function nextMonth() {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
  generateCalendar()
}

function goToday() {
  currentYear.value = dayjs().year()
  currentMonth.value = dayjs().month() + 1
  generateCalendar()
}

// 选择日期
function selectDate(day) {
  console.log('Selected date:', day.date)
}

// 打开新增对话框
function openAddDialog() {
  dialogVisible.value = true
}

// 查看预约
function viewAppointment(apt) {
  currentAppointment.value = apt
  drawerVisible.value = true
}

// 确认预约
async function confirmAppointment(apt) {
  try {
    await confirmApi(apt.id)
    ElMessage.success('确认成功')
    drawerVisible.value = false
    generateCalendar()
    fetchListData()
  } catch (e) {
    console.error(e)
  }
}

// 开始服务
async function startService(apt) {
  try {
    await startApi(apt.id)
    ElMessage.success('已开始服务')
    drawerVisible.value = false
    generateCalendar()
    fetchListData()
  } catch (e) {
    console.error(e)
  }
}

// 完成服务
async function completeService(apt) {
  try {
    await completeApi(apt.id)
    ElMessage.success('服务完成')
    drawerVisible.value = false
    generateCalendar()
    fetchListData()
  } catch (e) {
    console.error(e)
  }
}

// 取消预约
async function cancelAppointment(apt) {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消原因', '取消预约', { inputPattern: /.+/, inputErrorMessage: '请输入取消原因' })
    await cancelApi(apt.id, value)
    ElMessage.success('已取消')
    drawerVisible.value = false
    generateCalendar()
    fetchListData()
  } catch (e) {
    // 取消操作
  }
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    // 格式化提交数据
    const submitData = {
      ...formData,
      appointmentDate: formData.appointmentDate ? dayjs(formData.appointmentDate).format('YYYY-MM-DD') : '',
      startTime: formData.startTime ? dayjs(formData.startTime).format('HH:mm:ss') : '',
      endTime: formData.endTime ? dayjs(formData.endTime).format('HH:mm:ss') : ''
    }
    
    await addAppointment(submitData)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    generateCalendar()
  } catch (e) {
    console.error(e)
    // 显示后端返回的错误信息
    const errorMsg = e.response?.data?.message || e.message || '新增失败'
    ElMessage.error(errorMsg)
  } finally {
    submitLoading.value = false
  }
}

// 获取服务颜色
function getServiceColor(name) {
  const colors = {
    '面部护理': '#FFD1D1',
    '美甲': '#FFE4B5',
    '美睫': '#FADADD',
    '瘦身': '#D1E7DD',
    '美白': '#E6F7FF'
  }
  return colors[name] || '#FFF0F5'
}

// 获取状态类型
function getStatusType(status) {
  const types = { '待确认': 'warning', '已确认': 'primary', '服务中': '', '已完成': 'success', '已取消': 'danger' }
  return types[status] || ''
}

onMounted(() => {
  generateCalendar()
  // 获取下拉选项
  getCustomerPage({ pageSize: 100 }).then(res => customerOptions.value = res.data?.records || [])
  getActiveProjects().then(res => projectOptions.value = res.data || [])
  getBeauticians().then(res => staffOptions.value = res.data || [])
})
</script>

<style lang="scss" scoped>
.appointment-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
    
    .page-title { font-size: 24px; font-weight: 600; color: #333; margin-bottom: 4px; }
    .page-desc { font-size: 14px; color: #666; }
  }
  
  .view-tabs { margin-bottom: 16px; }
  
  .calendar-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
    
    .calendar-header {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 20px;
      
      .nav-group { display: flex; gap: 8px; }
      .current-month { font-size: 18px; font-weight: 600; color: #333; margin: 0; }
    }
    
    .calendar-weekdays {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      background: #FAFAFA;
      border-radius: 8px;
      margin-bottom: 8px;
      
      .weekday {
        padding: 12px;
        text-align: center;
        font-size: 14px;
        color: #555;
        font-weight: 500;
      }
    }
    
    .calendar-days {
      display: grid;
      grid-template-columns: repeat(7, 1fr);
      gap: 1px;
      background: #E8E8E8;
      border-radius: 8px;
      overflow: hidden;
      
      .calendar-day {
        background: #fff;
        min-height: 100px;
        padding: 8px;
        cursor: pointer;
        transition: background 0.2s;
        
        &:hover { background: #F5F7FA; }
        
        &.other-month { opacity: 0.5; }
        &.today { background: #FFF9F9; }
        
        .day-header { margin-bottom: 4px; }
        .day-number { font-size: 14px; font-weight: 500; color: #333; }
        
        .appointment-item {
          font-size: 12px;
          padding: 2px 6px;
          border-radius: 4px;
          margin-bottom: 2px;
          cursor: pointer;
          
          .apt-time { margin-right: 4px; color: #666; }
          .apt-customer { font-weight: 500; }
        }
        
        .more-btn {
          font-size: 12px;
          color: #999;
          text-align: center;
          padding: 2px;
        }
      }
    }
  }
  
  .list-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  }
  
  .pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
  
  .drawer-actions { margin-top: 24px; display: flex; gap: 12px; flex-wrap: wrap; }
}
</style>
