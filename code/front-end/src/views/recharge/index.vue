<template>
  <div class="recharge-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">客户充值</h1>
        <p class="page-desc">为客户储值卡充值</p>
      </div>
    </div>

    <!-- 充值表单 -->
    <div class="form-section">
      <el-card>
        <template #header>
          <span>充值信息</span>
        </template>
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" style="max-width: 500px">
          <el-form-item label="客户" prop="customerId">
            <el-select v-model="formData.customerId" filterable placeholder="选择客户" style="width: 100%" @change="handleCustomerChange">
              <el-option v-for="c in customerOptions" :key="c.id" :label="`${c.name} (${c.phone})`" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="selectedCustomer" label="储值余额">
            <span class="balance-info">{{ formatMoney(selectedCustomer.balance || 0) }}</span>
          </el-form-item>
          <el-form-item label="充值金额" prop="amount">
            <el-input-number v-model="formData.amount" :min="1" :precision="2" :step="100" style="width: 100%" />
          </el-form-item>
          <el-form-item label="赠送金额">
            <el-input-number v-model="formData.giftAmount" :min="0" :precision="2" :step="50" style="width: 100%" />
          </el-form-item>
          <el-form-item label="支付方式" prop="payMethod">
            <el-select v-model="formData.payMethod" placeholder="选择支付方式" style="width: 100%">
              <el-option label="现金" value="cash" />
              <el-option label="微信" value="wechat" />
              <el-option label="支付宝" value="alipay" />
              <el-option label="银行卡" value="bank" />
            </el-select>
          </el-form-item>
          <el-form-item label="充值后余额">
            <span class="balance-info highlight">
              {{ formatMoney((selectedCustomer?.balance || 0) + formData.amount + formData.giftAmount) }}
            </span>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确认充值</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 充值记录 -->
    <div class="record-section">
      <el-card>
        <template #header>
          <div style="display: flex; justify-content: space-between; align-items: center">
            <span>充值记录</span>
          </div>
        </template>
        <el-table :data="recordList" v-loading="recordLoading">
          <el-table-column prop="rechargeNo" label="充值单号" width="180" />
          <el-table-column prop="customerName" label="客户" width="120" />
          <el-table-column prop="rechargeAmount" label="充值金额" width="120">
            <template #default="{ row }">{{ formatMoney(row.rechargeAmount) }}</template>
          </el-table-column>
          <el-table-column prop="giftAmount" label="赠送金额" width="120">
            <template #default="{ row }">{{ formatMoney(row.giftAmount) }}</template>
          </el-table-column>
          <el-table-column prop="balanceAfter" label="充值后余额" width="120">
            <template #default="{ row }">{{ formatMoney(row.balanceAfter) }}</template>
          </el-table-column>
          <el-table-column prop="payMethod" label="支付方式" width="100">
            <template #default="{ row }">{{ getPayMethodLabel(row.payMethod) }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="充值时间" width="180">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" />
          <template #empty>
            <el-empty description="请先选择客户" />
          </template>
        </el-table>
        <div v-if="total > 0" class="pagination">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :total="total"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchRecords(formData.customerId)"
            @current-change="fetchRecords(formData.customerId)"
          />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getRechargePage, recharge } from '@/api/recharge'
import { getCustomerPage } from '@/api/customer'
import { ElMessage } from 'element-plus'

const formRef = ref(null)
const submitLoading = ref(false)
const recordLoading = ref(false)
const customerOptions = ref([])
const selectedCustomer = ref(null)
const recordList = ref([])
const total = ref(0)

const queryParams = reactive({ pageNum: 1, pageSize: 10 })

const formData = reactive({
  customerId: null,
  amount: 100,
  giftAmount: 0,
  payMethod: 'wechat',
  remark: ''
})

const formRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  amount: [{ required: true, message: '请输入充值金额', trigger: 'blur' }],
  payMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

async function fetchCustomers() {
  try {
    const res = await getCustomerPage({ pageSize: 1000 })
    customerOptions.value = res.data?.records || []
  } catch (e) {
    console.error('获取客户列表失败', e)
  }
}

async function fetchRecords(customerId) {
  if (!customerId) {
    recordList.value = []
    total.value = 0
    return
  }
  recordLoading.value = true
  try {
    const res = await getRechargePage({ ...queryParams, customerId })
    recordList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('获取充值记录失败', e)
  } finally {
    recordLoading.value = false
  }
}

function handleCustomerChange(customerId) {
  selectedCustomer.value = customerOptions.value.find(c => c.id === customerId) || null
  // 选择客户后查询该客户的充值记录
  queryParams.pageNum = 1
  fetchRecords(customerId)
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    // 转换字段名：amount -> rechargeAmount
    const submitData = {
      customerId: formData.customerId,
      rechargeAmount: formData.amount,
      giftAmount: formData.giftAmount,
      payMethod: formData.payMethod,
      remark: formData.remark
    }
    await recharge(submitData)
    ElMessage.success('充值成功')
    // 刷新当前客户的充值记录
    fetchRecords(formData.customerId)
    // 先刷新客户列表，再更新选中的客户对象
    await fetchCustomers()
    selectedCustomer.value = customerOptions.value.find(c => c.id === formData.customerId) || null
  } catch (e) {
    console.error(e)
  } finally {
    submitLoading.value = false
  }
}

function resetForm() {
  const currentCustomerId = formData.customerId
  formData.customerId = null
  formData.amount = 100
  formData.giftAmount = 0
  formData.payMethod = 'wechat'
  formData.remark = ''
  selectedCustomer.value = null
  // 清空充值记录
  recordList.value = []
  total.value = 0
  queryParams.pageNum = 1
  // 清除表单校验状态
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

function formatMoney(amount) {
  return '¥' + (amount || 0).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

function getPayMethodLabel(method) {
  const labels = { cash: '现金', wechat: '微信', alipay: '支付宝', bank: '银行卡' }
  return labels[method] || method
}

onMounted(() => {
  fetchCustomers()
})
</script>

<style lang="scss" scoped>
.recharge-page {
  .page-header {
    margin-bottom: 20px;
    .page-title { font-size: 24px; font-weight: 600; color: #333; margin-bottom: 4px; }
    .page-desc { font-size: 14px; color: #666; }
  }

  .form-section, .record-section { margin-bottom: 20px; }

  .balance-info {
    font-size: 18px;
    font-weight: 600;
    color: #333;
    &.highlight { color: #F86C9A; }
  }

  .pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
}
</style>
