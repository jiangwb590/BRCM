<template>
  <div class="purchase-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">套餐购买</h1>
        <p class="page-desc">使用储值余额购买套餐次卡</p>
      </div>
    </div>

    <!-- 购买表单 -->
    <div class="form-section">
      <el-card>
        <template #header>
          <span>购买信息</span>
        </template>
        <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px" style="max-width: 600px">
          <el-form-item label="客户" prop="customerId">
            <el-select v-model="formData.customerId" filterable placeholder="选择客户" style="width: 100%" @change="handleCustomerChange">
              <el-option v-for="c in customerOptions" :key="c.id" :label="`${c.name} (${c.phone})`" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="selectedCustomer" label="储值余额">
            <span class="balance-info" :class="{ 'low-balance': selectedCustomer.balance < totalAmount }">
              {{ formatMoney(selectedCustomer.balance || 0) }}
            </span>
          </el-form-item>
          <el-form-item label="选择套餐" prop="packageId">
            <el-select v-model="formData.packageId" placeholder="选择套餐" style="width: 100%" @change="handlePackageChange">
              <el-option v-for="p in packageOptions" :key="p.id" :label="`${p.packageName} - ${formatMoney(p.originalPrice || p.price)}/${p.times}次`" :value="p.id" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="selectedPackage" label="套餐详情">
            <div class="package-detail">
              <div class="detail-item"><span class="label">套餐名称：</span>{{ selectedPackage.packageName }}</div>
              <div class="detail-item"><span class="label">原价：</span>{{ formatMoney(selectedPackage.originalPrice || selectedPackage.price) }} / {{ selectedPackage.times }}次</div>
              <div class="detail-item"><span class="label">卡扣价：</span>{{ formatMoney(selectedPackage.price) }} / {{ selectedPackage.times }}次</div>
              <div class="detail-item"><span class="label">有效期：</span>{{ selectedPackage.validDays }}天</div>
              <div class="detail-item" v-if="selectedPackage.description"><span class="label">描述：</span>{{ selectedPackage.description }}</div>
            </div>
          </el-form-item>
          <el-form-item label="购买数量" prop="quantity">
            <el-input-number v-model="formData.quantity" :min="1" :max="99" style="width: 200px" />
          </el-form-item>
          <el-form-item label="应付金额">
            <span class="amount-info">{{ formatMoney(totalAmount) }}</span>
            <span class="times-info">（共 {{ totalTimes }} 次）</span>
            <span v-if="selectedCustomer && selectedCustomer.balance === 0" class="price-tip">（原价购买，储值余额为0）</span>
            <span v-else-if="selectedCustomer && selectedCustomer.balance > 0" class="price-tip">（卡扣价购买）</span>
          </el-form-item>
          <el-form-item label="支付方式" prop="payMethod">
            <el-radio-group v-model="formData.payMethod">
              <el-radio value="balance">储值卡支付</el-radio>
              <el-radio value="cash">现金</el-radio>
              <el-radio value="wechat">微信</el-radio>
              <el-radio value="alipay">支付宝</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="formData.payMethod === 'balance'" label="支付后余额">
            <span class="balance-info" :class="{ 'insufficient': balanceAfter < 0 }">
              {{ formatMoney(balanceAfter) }}
            </span>
            <span v-if="balanceAfter < 0" class="error-tip">（余额不足，请先充值）</span>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitLoading" :disabled="!canSubmit" @click="handleSubmit">确认购买</el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 购买记录 -->
    <div class="record-section">
      <el-card>
        <template #header>
          <span>购买记录</span>
        </template>
        <el-table :data="recordList" v-loading="recordLoading">
          <el-table-column prop="purchaseNo" label="购买单号" width="180" />
          <el-table-column prop="customerName" label="客户" width="120" />
          <el-table-column prop="packageName" label="套餐" width="150" />
          <el-table-column prop="times" label="次数" width="80" />
          <el-table-column prop="totalAmount" label="金额" width="120">
            <template #default="{ row }">{{ formatMoney(row.totalAmount) }}</template>
          </el-table-column>
          <el-table-column prop="payMethod" label="支付方式" width="100">
            <template #default="{ row }">{{ getPayMethodLabel(row.payMethod) }}</template>
          </el-table-column>
          <el-table-column prop="balanceAfter" label="余额" width="120">
            <template #default="{ row }">{{ row.balanceAfter ? formatMoney(row.balanceAfter) : '-' }}</template>
          </el-table-column>
          <el-table-column prop="validEndDate" label="到期日期" width="120" />
          <el-table-column prop="createTime" label="购买时间" width="180">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
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
import { getActivePackages, purchasePackage, getPurchasePage } from '@/api/package'
import { getCustomerPage } from '@/api/customer'
import { ElMessage, ElMessageBox } from 'element-plus'

const formRef = ref(null)
const submitLoading = ref(false)
const recordLoading = ref(false)
const customerOptions = ref([])
const packageOptions = ref([])
const selectedCustomer = ref(null)
const selectedPackage = ref(null)
const recordList = ref([])
const total = ref(0)

const queryParams = reactive({ pageNum: 1, pageSize: 10 })

const formData = reactive({
  customerId: null,
  packageId: null,
  quantity: 1,
  payMethod: 'balance',
  remark: ''
})

const formRules = {
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }],
  packageId: [{ required: true, message: '请选择套餐', trigger: 'change' }],
  payMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }]
}

const totalAmount = computed(() => {
  if (!selectedPackage.value) return 0
  // 储值余额为0时使用原价，否则使用卡扣价
  const unitPrice = (selectedCustomer.value && selectedCustomer.value.balance > 0) 
    ? selectedPackage.value.price 
    : (selectedPackage.value.originalPrice || selectedPackage.value.price)
  return unitPrice * formData.quantity
})

const totalTimes = computed(() => {
  if (!selectedPackage.value) return 0
  return selectedPackage.value.times * formData.quantity
})

// 判断是否使用卡扣价
const useCardPrice = computed(() => {
  return selectedCustomer.value && selectedCustomer.value.balance > 0
})

const balanceAfter = computed(() => {
  if (!selectedCustomer.value) return 0
  return selectedCustomer.value.balance - totalAmount.value
})

const canSubmit = computed(() => {
  if (formData.payMethod === 'balance') {
    return selectedCustomer.value && selectedCustomer.value.balance >= totalAmount.value
  }
  return true
})

async function fetchCustomers() {
  try {
    const res = await getCustomerPage({ pageSize: 1000 })
    customerOptions.value = res.data?.records || []
  } catch (e) {
    console.error('获取客户列表失败', e)
  }
}

async function fetchPackages() {
  try {
    const res = await getActivePackages()
    packageOptions.value = res.data || []
  } catch (e) {
    console.error('获取套餐列表失败', e)
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
    const res = await getPurchasePage({ ...queryParams, customerId })
    recordList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error('获取购买记录失败', e)
  } finally {
    recordLoading.value = false
  }
}

function handleCustomerChange(customerId) {
  selectedCustomer.value = customerOptions.value.find(c => c.id === customerId) || null
  // 选择客户后查询该客户的购买记录
  queryParams.pageNum = 1
  fetchRecords(customerId)
}

function handlePackageChange(packageId) {
  selectedPackage.value = packageOptions.value.find(p => p.id === packageId) || null
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    
    if (formData.payMethod === 'balance' && balanceAfter.value < 0) {
      ElMessage.error('储值卡余额不足')
      return
    }

    await ElMessageBox.confirm(
      `确认购买「${selectedPackage.value.packageName}」x ${formData.quantity}，共 ${formatMoney(totalAmount.value)}？`,
      '确认购买',
      { type: 'warning' }
    )

    submitLoading.value = true
    await purchasePackage(formData)
    ElMessage.success('购买成功')
    // 刷新当前客户的购买记录和余额
    fetchRecords(formData.customerId)
    // 先刷新客户列表，再更新选中的客户对象
    await fetchCustomers()
    selectedCustomer.value = customerOptions.value.find(c => c.id === formData.customerId) || null
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  } finally {
    submitLoading.value = false
  }
}

function resetForm() {
  formData.customerId = null
  formData.packageId = null
  formData.quantity = 1
  formData.payMethod = 'balance'
  formData.remark = ''
  selectedCustomer.value = null
  selectedPackage.value = null
  // 清空购买记录
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
  const labels = { balance: '储值卡', cash: '现金', wechat: '微信', alipay: '支付宝' }
  return labels[method] || method
}

onMounted(() => {
  fetchCustomers()
  fetchPackages()
})
</script>

<style lang="scss" scoped>
.purchase-page {
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
    &.low-balance { color: #E6A23C; }
    &.insufficient { color: #F56C6C; }
  }

  .amount-info {
    font-size: 20px;
    font-weight: 600;
    color: #F86C9A;
  }

  .times-info { color: #666; margin-left: 8px; }

  .price-tip { color: #999; margin-left: 8px; font-size: 12px; }

  .error-tip { color: #F56C6C; margin-left: 8px; }

  .package-detail {
    background: #F5F7FA;
    padding: 12px 16px;
    border-radius: 8px;
    .detail-item { margin-bottom: 8px; &:last-child { margin-bottom: 0; } }
    .label { color: #666; }
  }

  .pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
}
</style>