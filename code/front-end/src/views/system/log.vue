<template>
  <div class="system-page">
    <div class="page-header">
      <h1 class="page-title">操作日志</h1>
    </div>
    
    <div class="filter-section">
      <el-form inline>
        <el-form-item label="操作人">
          <el-input v-model="queryParams.operName" placeholder="操作人姓名" style="width: 150px" clearable />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="queryParams.businessType" placeholder="请选择" style="width: 120px" clearable>
            <el-option label="新增" :value="1" />
            <el-option label="修改" :value="2" />
            <el-option label="删除" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="title" label="操作标题" width="120" />
        <el-table-column label="业务类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getBusinessTypeTag(row.businessType)" size="small">
              {{ getBusinessTypeLabel(row.businessType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operName" label="操作人" width="100" />
        <el-table-column prop="targetName" label="操作对象" width="150">
          <template #default="{ row }">
            <span v-if="row.targetName">{{ row.targetName }}</span>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="targetType" label="对象类型" width="80">
          <template #default="{ row }">
            {{ getTargetTypeLabel(row.targetType) }}
          </template>
        </el-table-column>
        <el-table-column prop="operIp" label="IP地址" width="130" />
        <el-table-column prop="status" label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.operTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="80">
          <template #default="{ row }">
            <el-button type="primary" link @click="showDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
    
    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="操作日志详情" width="700px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="操作标题">{{ currentLog.title }}</el-descriptions-item>
        <el-descriptions-item label="业务类型">{{ getBusinessTypeLabel(currentLog.businessType) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operName }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ formatTime(currentLog.operTime) }}</el-descriptions-item>
        <el-descriptions-item label="操作对象">{{ currentLog.targetName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="对象类型">{{ getTargetTypeLabel(currentLog.targetType) }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.operIp }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentLog.status === 1 ? 'success' : 'danger'" size="small">
            {{ currentLog.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">{{ currentLog.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ currentLog.costTime }}ms</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <div class="code-block">{{ currentLog.operParam || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <div class="code-block">{{ currentLog.jsonResult || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMsg">
          <div class="error-text">{{ currentLog.errorMsg }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getOperLogPage } from '@/api/system'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dateRange = ref([])
const detailVisible = ref(false)
const currentLog = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  operName: '',
  businessType: null,
  startTime: '',
  endTime: ''
})

async function fetchData() {
  loading.value = true
  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.startTime = dateRange.value[0] + ' 00:00:00'
      queryParams.endTime = dateRange.value[1] + ' 23:59:59'
    } else {
      queryParams.startTime = ''
      queryParams.endTime = ''
    }
    
    const res = await getOperLogPage(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryParams.operName = ''
  queryParams.businessType = null
  queryParams.startTime = ''
  queryParams.endTime = ''
  dateRange.value = []
  queryParams.pageNum = 1
  fetchData()
}

function showDetail(row) {
  currentLog.value = row
  detailVisible.value = true
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

function getBusinessTypeLabel(type) {
  const labels = { 0: '其他', 1: '新增', 2: '修改', 3: '删除', 4: '查询' }
  return labels[type] || '未知'
}

function getBusinessTypeTag(type) {
  const tags = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger', 4: '' }
  return tags[type] || 'info'
}

function getTargetTypeLabel(type) {
  const labels = { 
    customer: '客户', 
    project: '项目', 
    package: '套餐',
    product: '产品',
    user: '用户',
    role: '角色'
  }
  return labels[type] || type || '-'
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
.system-page {
  .page-header { 
    margin-bottom: 20px; 
    .page-title { font-size: 24px; font-weight: 600; } 
  }
  .filter-section { 
    background: #fff; 
    border-radius: 12px; 
    padding: 20px; 
    margin-bottom: 16px; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.04); 
  }
  .table-section { 
    background: #fff; 
    border-radius: 12px; 
    padding: 20px; 
    box-shadow: 0 4px 12px rgba(0,0,0,0.04); 
  }
  .pagination { 
    margin-top: 16px; 
    display: flex; 
    justify-content: flex-end; 
  }
  .code-block {
    background: #f5f7fa;
    padding: 8px 12px;
    border-radius: 4px;
    font-family: monospace;
    font-size: 12px;
    max-height: 200px;
    overflow-y: auto;
    word-break: break-all;
  }
  .error-text {
    color: #f56c6c;
    font-size: 13px;
  }
}
</style>