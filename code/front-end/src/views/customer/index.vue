<template>
  <div class="customer-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">客户管理</h1>
        <p class="page-desc">管理客户档案、来源追踪、标签分类</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="openAddDialog" v-permission="'customer:add'">
          <el-icon><Plus /></el-icon>
          新增
        </el-button>
        <el-button @click="handleExport" :loading="exportLoading">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>
    
    <!-- 筛选区域 -->
    <div class="filter-section">
      <el-form :model="queryParams" inline class="filter-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="姓名/手机号/卡号"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        
        <el-form-item label="客户类型">
          <el-select v-model="queryParams.category" placeholder="全部类型" clearable style="width: 140px">
            <el-option label="潜在客户" value="潜在客户" />
            <el-option label="新客户" value="新客户" />
            <el-option label="老客户" value="老客户" />
            <el-option label="VIP客户" value="VIP客户" />
            <el-option label="沉睡客户" value="沉睡客户" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="客户来源">
          <el-select v-model="queryParams.source" placeholder="全部来源" clearable style="width: 140px">
            <el-option label="抖音" value="抖音" />
            <el-option label="美团" value="美团" />
            <el-option label="小红书" value="小红书" />
            <el-option label="微信" value="微信" />
            <el-option label="朋友介绍" value="朋友介绍" />
            <el-option label="自然进店" value="自然进店" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        
<!--        <el-form-item label="会员等级">-->
<!--          <el-select v-model="queryParams.memberLevelId" placeholder="全部等级" clearable style="width: 140px">-->
<!--            <el-option label="钻石会员" value="4" />-->
<!--            <el-option label="金卡会员" value="3" />-->
<!--            <el-option label="银卡会员" value="2" />-->
<!--            <el-option label="普通会员" value="1" />-->
<!--          </el-select>-->
<!--        </el-form-item>-->
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        :data="tableData"
        v-loading="loading"
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />
        
        <el-table-column label="客户信息" min-width="180">
          <template #default="{ row }">
            <div class="customer-info">
              <el-avatar :size="40" class="avatar">
                {{ row.name?.charAt(0) }}
              </el-avatar>
              <div class="info">
                <div class="name">{{ row.name }}</div>
                <div class="meta">
                  <span>{{ row.gender === 1 ? '男' : '女' }}</span>
                  <span v-if="row.birthday">· {{ row.birthday }}</span>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="联系方式" min-width="130">
          <template #default="{ row }">
            <span>{{ formatPhone(row.phone) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="客户类型" min-width="100">
          <template #default="{ row }">
            <el-tag :class="getCategoryTagClass(row.category)" size="small">
              {{ row.category }}
            </el-tag>
          </template>
        </el-table-column>
        
<!--        <el-table-column label="会员等级" min-width="100">-->
<!--          <template #default="{ row }">-->
<!--            <el-tag v-if="row.memberLevelName" :class="getMemberTagClass(row.memberLevelName)" size="small">-->
<!--              {{ row.memberLevelName }}-->
<!--            </el-tag>-->
<!--            <span v-else class="text-muted">-</span>-->
<!--          </template>-->
<!--        </el-table-column>-->
        
        <el-table-column label="累计消费" min-width="110">
          <template #default="{ row }">
            <span class="amount">¥{{ formatNumber(row.totalAmount || 0) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="来源" min-width="100">
          <template #default="{ row }">
            <span>{{ row.source || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="最近消费" min-width="110">
          <template #default="{ row }">
            <span>{{ row.lastConsumeTime ? formatDate(row.lastConsumeTime) : '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewDetail(row)">详情</el-button>
            <el-button type="primary" link @click="openEditDialog(row)" v-permission="'customer:edit'">编辑</el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, row)">
              <el-button type="primary" link>
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="consume">消费记录</el-dropdown-item>
                  <el-dropdown-item command="card">会员卡</el-dropdown-item>
                  <el-dropdown-item command="care">客户关怀</el-dropdown-item>
                  <el-dropdown-item command="delete" divided v-permission="'customer:delete'">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </div>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="80px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="formData.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生日" prop="birthday">
              <div style="display: flex; gap: 8px;">
                <el-select v-model="birthdayMonth" placeholder="月" style="width: 80px" @change="updateBirthday">
                  <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="String(m).padStart(2, '0')" />
                </el-select>
                <el-select v-model="birthdayDay" placeholder="日" style="width: 80px" @change="updateBirthday">
                  <el-option v-for="d in 31" :key="d" :label="d + '日'" :value="String(d).padStart(2, '0')" />
                </el-select>
              </div>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户来源" prop="source">
              <el-select v-model="formData.source" placeholder="请选择" style="width: 100%">
                <el-option label="抖音" value="抖音" />
                <el-option label="美团" value="美团" />
                <el-option label="小红书" value="小红书" />
                <el-option label="微信" value="微信" />
                <el-option label="朋友介绍" value="朋友介绍" />
                <el-option label="自然进店" value="自然进店" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="微信号" prop="wechat">
              <el-input v-model="formData.wechat" placeholder="请输入微信号" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="标签" prop="tags">
          <el-select v-model="formData.tags" multiple placeholder="选择标签" style="width: 100%">
            <el-option label="敏感肌" value="敏感肌" />
            <el-option label="油性皮肤" value="油性皮肤" />
            <el-option label="干性皮肤" value="干性皮肤" />
            <el-option label="高消费" value="高消费" />
            <el-option label="爱聊天" value="爱聊天" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCustomerPage, addCustomer, updateCustomer, deleteCustomer, exportCustomer } from '@/api/customer'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)
const exportLoading = ref(false)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  category: '',
  source: '',
  memberLevelId: ''
})

// 表格数据
const tableData = ref([])
const total = ref(0)
const selectedRows = ref([])

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增客户')
const formRef = ref(null)

// 生日月份和日期（用于UI选择）
const birthdayMonth = ref('')
const birthdayDay = ref('')

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  gender: 2,
  phone: '',
  birthday: '',
  source: '',
  wechat: '',
  tags: [],
  remark: ''
})

// 更新生日值
function updateBirthday() {
  if (birthdayMonth.value && birthdayDay.value) {
    formData.birthday = `${birthdayMonth.value}-${birthdayDay.value}`
  } else {
    formData.birthday = ''
  }
}

// 从生日字符串解析月份和日期
function parseBirthday(birthday) {
  if (birthday && birthday.includes('-')) {
    const parts = birthday.split('-')
    birthdayMonth.value = parts[0] || ''
    birthdayDay.value = parts[1] || ''
  } else {
    birthdayMonth.value = ''
    birthdayDay.value = ''
  }
}

// 表单验证规则
const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 获取数据
async function fetchData() {
  loading.value = true
  try {
    const res = await getCustomerPage(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('获取客户列表失败', e)
  } finally {
    loading.value = false
  }
}

// 搜索
function handleSearch() {
  queryParams.pageNum = 1
  fetchData()
}

// 重置
function handleReset() {
  queryParams.keyword = ''
  queryParams.category = ''
  queryParams.source = ''
  queryParams.memberLevelId = ''
  queryParams.pageNum = 1
  fetchData()
}

// 选择变化
function handleSelectionChange(rows) {
  selectedRows.value = rows
}

// 打开新增对话框
function openAddDialog() {
  dialogTitle.value = '新增客户'
  Object.assign(formData, {
    id: null,
    name: '',
    gender: 2,
    phone: '',
    birthday: '',
    source: '',
    wechat: '',
    tags: [],
    remark: ''
  })
  birthdayMonth.value = ''
  birthdayDay.value = ''
  dialogVisible.value = true
}

// 打开编辑对话框
function openEditDialog(row) {
  dialogTitle.value = '编辑客户'
  Object.assign(formData, {
    ...row,
    tags: row.tags ? row.tags.split(',') : []
  })
  parseBirthday(row.birthday)
  dialogVisible.value = true
}

// 提交表单
async function handleSubmit() {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    const data = {
      ...formData,
      tags: formData.tags?.join(',')
    }
    
    if (formData.id) {
      await updateCustomer(data)
      ElMessage.success('修改成功')
    } else {
      await addCustomer(data)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    console.error('提交失败', e)
  } finally {
    submitLoading.value = false
  }
}

// 查看详情
function viewDetail(row) {
  router.push(`/customer/detail/${row.id}`)
}

// 下拉菜单命令
async function handleCommand(command, row) {
  switch (command) {
    case 'consume':
      router.push(`/consume?customerId=${row.id}`)
      break
    case 'card':
      router.push(`/member-card?customerId=${row.id}`)
      break
    case 'care':
      router.push(`/customer-care?customerId=${row.id}`)
      break
    case 'delete':
      try {
        await ElMessageBox.confirm('确定要删除该客户吗？', '提示', { type: 'warning' })
        await deleteCustomer(row.id)
        ElMessage.success('删除成功')
        fetchData()
      } catch (e) {
        // 取消删除
      }
      break
  }
}

// 格式化手机号
function formatPhone(phone) {
  if (!phone) return '-'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 格式化数字
function formatNumber(num) {
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 格式化日期
function formatDate(date) {
  return dayjs(date).format('YYYY-MM-DD')
}

// 获取类型标签样式
function getCategoryTagClass(category) {
  const classes = {
    'VIP客户': 'tag-vip',
    '老客户': 'tag-gold',
    '新客户': 'tag-silver'
  }
  return classes[category] || ''
}

// 获取会员等级标签样式
function getMemberTagClass(level) {
  const classes = {
    '钻石会员': 'tag-diamond',
    '金卡会员': 'tag-gold',
    '银卡会员': 'tag-silver'
  }
  return classes[level] || ''
}

// 导出客户数据
async function handleExport() {
  exportLoading.value = true
  try {
    const params = {
      keyword: queryParams.keyword,
      category: queryParams.category,
      source: queryParams.source
    }
    const response = await exportCustomer(params)
    
    // 检查是否为错误响应
    if (response.data.type === 'application/json') {
      const text = await response.data.text()
      const error = JSON.parse(text)
      ElMessage.error(error.message || '导出失败')
      return
    }
    
    // 创建Blob对象并下载
    const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `客户数据_${dayjs().format('YYYY-MM-DD')}.xls`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (e) {
    console.error('导出失败', e)
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.customer-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
    
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
  
  .filter-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  }
  
  .table-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  }
  
  .customer-info {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .avatar {
      background-color: #F86C9A;
      color: #fff;
    }
    
    .info {
      .name {
        font-size: 14px;
        color: #333;
        font-weight: 500;
      }
      
      .meta {
        font-size: 12px;
        color: #999;
      }
    }
  }
  
  .amount {
    font-weight: 600;
    color: #F86C9A;
  }
  
  .text-muted {
    color: #999;
  }
  
  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;
  }
}

// 标签样式
.tag-vip {
  background-color: rgba(248, 108, 154, 0.1);
  border-color: rgba(248, 108, 154, 0.4);
  color: #F86C9A;
}

.tag-diamond {
  background-color: rgba(255, 193, 7, 0.1);
  border-color: rgba(255, 193, 7, 0.4);
  color: #FFC107;
}

.tag-gold {
  background-color: rgba(255, 152, 0, 0.1);
  border-color: rgba(255, 152, 0, 0.4);
  color: #FF9800;
}

.tag-silver {
  background-color: rgba(158, 158, 158, 0.1);
  border-color: rgba(158, 158, 158, 0.4);
  color: #9E9E9E;
}
</style>
