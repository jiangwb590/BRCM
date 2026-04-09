<template>
  <div class="member-card-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">会员卡管理</h1>
        <p class="page-desc">管理客户次卡和储值卡</p>
      </div>
      <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon>开卡</el-button>
    </div>
    
    <div class="filter-section">
      <el-form :model="queryParams" inline>
        <el-form-item label="客户">
          <el-input v-model="queryParams.customerName" placeholder="客户姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="卡类型">
          <el-select v-model="queryParams.cardType" placeholder="全部" clearable style="width: 120px">
            <el-option label="次卡" value="次卡" />
            <el-option label="储值卡" value="储值卡" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="已过期" :value="2" />
            <el-option label="已用完" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="cardNo" label="卡号" width="180" />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="cardType" label="卡类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.cardType === '次卡' ? 'warning' : 'success'" size="small">{{ row.cardType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="关联项目" width="150" />
        <el-table-column label="剩余" width="120">
          <template #default="{ row }">
            <span v-if="row.cardType === '次卡'" class="remain">{{ row.remainCount }}次</span>
            <span v-else class="remain">¥{{ row.remainAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="validEndDate" label="有效期" width="120">
          <template #default="{ row }">
            <span :class="{ 'expired': isExpired(row.validEndDate) }">{{ row.validEndDate || '永久' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="openRechargeDialog(row)" :disabled="row.cardType !== '储值卡'">充值</el-button>
            <el-button type="danger" link @click="handleDisable(row)" :disabled="row.status !== 1">作废</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>
    
    <el-dialog v-model="dialogVisible" title="开卡" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="formData.customerId" filterable placeholder="选择客户" style="width: 100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="卡类型" prop="cardType">
          <el-radio-group v-model="formData.cardType">
            <el-radio value="次卡">次卡</el-radio>
            <el-radio value="储值卡">储值卡</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="formData.cardType === '次卡'" label="关联项目" prop="projectId">
          <el-select v-model="formData.projectId" placeholder="选择项目" style="width: 100%">
            <el-option v-for="p in projectOptions" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formData.cardType === '次卡'" label="总次数" prop="totalCount">
          <el-input-number v-model="formData.totalCount" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item v-if="formData.cardType === '储值卡'" label="储值金额" prop="totalAmount">
          <el-input-number v-model="formData.totalAmount" :min="1" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="购卡金额" prop="purchaseAmount">
          <el-input-number v-model="formData.purchaseAmount" :min="0" :precision="2" style="width: 100%" placeholder="客户实际支付金额" />
        </el-form-item>
        <el-form-item label="有效期"><el-date-picker v-model="formData.validEndDate" type="date" placeholder="选择日期" style="width: 100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="rechargeVisible" title="充值" width="400px">
      <el-form label-width="80px">
        <el-form-item label="当前余额"><span class="amount">¥{{ currentCard.remainAmount }}</span></el-form-item>
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeAmount" :min="1" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" :loading="rechargeLoading" @click="handleRecharge">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMemberCardPage, createMemberCard, rechargeMemberCard, disableMemberCard } from '@/api/memberCard'
import { getCustomerPage } from '@/api/customer'
import { getActiveProjects } from '@/api/project'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const loading = ref(false)
const submitLoading = ref(false)
const rechargeLoading = ref(false)
const dialogVisible = ref(false)
const rechargeVisible = ref(false)
const formRef = ref(null)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, customerName: '', cardType: '', status: '' })
const formData = reactive({ customerId: null, cardType: '次卡', projectId: null, totalCount: 10, totalAmount: 1000, purchaseAmount: 0, validEndDate: '' })
const formRules = { customerId: [{ required: true, message: '请选择客户', trigger: 'change' }], cardType: [{ required: true, message: '请选择卡类型', trigger: 'change' }] }
const customerOptions = ref([])
const projectOptions = ref([])
const currentCard = ref({})
const rechargeAmount = ref(100)

async function fetchData() {
  loading.value = true
  try { const res = await getMemberCardPage(queryParams); tableData.value = res.data?.records || []; total.value = res.data?.total || 0 }
  finally { loading.value = false }
}

function handleSearch() { queryParams.pageNum = 1; fetchData() }
function handleReset() { queryParams.customerName = ''; queryParams.cardType = ''; queryParams.status = ''; handleSearch() }

function openAddDialog() { Object.assign(formData, { customerId: null, cardType: '次卡', projectId: null, totalCount: 10, totalAmount: 1000, purchaseAmount: 0, validEndDate: '' }); dialogVisible.value = true }

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    await createMemberCard(formData)
    ElMessage.success('开卡成功')
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

function openRechargeDialog(row) { currentCard.value = row; rechargeAmount.value = 100; rechargeVisible.value = true }

async function handleRecharge() {
  rechargeLoading.value = true
  try {
    await rechargeMemberCard(currentCard.value.id, rechargeAmount.value)
    ElMessage.success('充值成功')
    rechargeVisible.value = false; fetchData()
  } finally { rechargeLoading.value = false }
}

async function handleDisable(row) {
  try { await ElMessageBox.confirm('确定作废该会员卡？', '提示', { type: 'warning' }); await disableMemberCard(row.id); ElMessage.success('已作废'); fetchData() } catch (e) {}
}

function isExpired(date) { return date && dayjs(date).isBefore(dayjs()) }
function getStatusType(status) { const types = { 1: 'success', 2: 'danger', 3: 'info' }; return types[status] || '' }
function getStatusName(status) { const names = { 1: '正常', 2: '已过期', 3: '已用完' }; return names[status] || '-' }

onMounted(() => {
  fetchData()
  getCustomerPage({ pageSize: 100 }).then(res => customerOptions.value = res.data?.records || [])
  getActiveProjects().then(res => projectOptions.value = res.data || [])
})
</script>

<style lang="scss" scoped>
.member-card-page {
  .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;
    .page-title { font-size: 24px; font-weight: 600; margin-bottom: 4px; }
    .page-desc { font-size: 14px; color: #666; }
  }
  .filter-section { background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
  .table-section { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
  .remain { color: #F86C9A; font-weight: 600; }
  .expired { color: #ff4d4f; }
  .amount { color: #F86C9A; font-weight: 600; font-size: 18px; }
  .pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
}
</style>
