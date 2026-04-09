<template>
  <div class="consume-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">消费记录</h1>
        <p class="page-desc">管理客户消费记录</p>
      </div>
      <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon>开单消费</el-button>
    </div>
    
    <div class="filter-section">
      <el-form :model="queryParams" inline>
        <el-form-item label="客户">
          <el-input v-model="queryParams.customerName" placeholder="客户姓名" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item label="消费类型">
          <el-select v-model="queryParams.consumeType" placeholder="全部" clearable style="width: 120px">
            <el-option label="现金消费" value="cash" />
            <el-option label="次卡消费" value="times" />
            <el-option label="储值消费" value="stored" />
            <el-option label="购买产品" value="product" />
          </el-select>
        </el-form-item>
        <el-form-item label="支付方式">
          <el-select v-model="queryParams.payType" placeholder="全部" clearable style="width: 120px">
            <el-option label="现金" value="cash" />
            <el-option label="微信" value="wechat" />
            <el-option label="支付宝" value="alipay" />
            <el-option label="储值卡" value="stored" />
            <el-option label="次卡" value="times" />
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
        <el-table-column prop="consumeNo" label="消费单号" width="180" />
        <el-table-column prop="customerName" label="客户" width="120" />
        <el-table-column prop="projectName" label="服务项目/产品" width="150">
          <template #default="{ row }">
            <span>{{ row.projectName || row.productName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="consumeType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getConsumeTypeTag(row.consumeType)" size="small">{{ getConsumeTypeName(row.consumeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">
            <span v-if="row.consumeType === 'times'" class="times">{{ row.consumeTimes || 1 }}次</span>
            <span v-else class="amount">¥{{ row.amount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="100">
          <template #default="{ row }">{{ getPayTypeName(row.payMethod) }}</template>
        </el-table-column>
        <el-table-column prop="beauticianName" label="服务人员" width="100" />
        <el-table-column prop="consumeTime" label="消费时间" width="180">
          <template #default="{ row }">{{ formatTime(row.consumeTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="(row.status === null || row.status === 1) ? 'success' : 'danger'" size="small">{{ (row.status === null || row.status === 1) ? '正常' : '已退款' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>
    
    <el-dialog v-model="dialogVisible" title="开单消费" width="550px" destroy-on-close @closed="resetForm">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px">
        <el-form-item label="客户" prop="customerId">
          <el-select v-model="formData.customerId" filterable placeholder="选择客户" style="width: 100%" @change="handleCustomerChange">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="消费模式" prop="consumeMode">
          <el-radio-group v-model="formData.consumeMode" @change="handleConsumeModeChange">
            <el-radio value="project">单独项目</el-radio>
            <el-radio value="package">已购买套餐</el-radio>
            <el-radio value="product">购买产品</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 单独项目模式 -->
        <el-form-item v-if="formData.consumeMode === 'project'" label="服务项目" prop="projectId">
          <el-select v-model="formData.projectId" placeholder="选择项目" style="width: 100%" @change="handleProjectChange">
            <el-option v-for="p in projectOptions" :key="p.id" :label="`${p.name} - ¥${p.price}`" :value="p.id" />
          </el-select>
        </el-form-item>
        
        <!-- 已购买套餐模式 -->
        <el-form-item v-if="formData.consumeMode === 'package'" label="已购套餐" prop="purchaseId">
          <el-select v-model="formData.purchaseId" placeholder="选择套餐" style="width: 100%" @change="handlePurchaseChange">
            <el-option v-for="p in purchaseOptions" :key="p.id" :label="`${p.packageName} (剩余${p.remainTimes}次)`" :value="p.id">
              <div style="display: flex; justify-content: space-between;">
                <span>{{ p.packageName }}</span>
                <span style="color: #999;">剩余 {{ p.remainTimes }} 次</span>
              </div>
            </el-option>
          </el-select>
          <div v-if="selectedPurchase" class="purchase-info">
            <span>有效期至: {{ selectedPurchase.validEndDate }}</span>
          </div>
        </el-form-item>
        
        <!-- 购买产品模式 -->
        <el-form-item v-if="formData.consumeMode === 'product'" label="选择产品" prop="productId">
          <el-select v-model="formData.productId" placeholder="选择产品" style="width: 100%" @change="handleProductChange">
            <el-option v-for="p in productOptions" :key="p.id" :label="`${p.name} - ¥${p.salePrice} (库存: ${p.stock})`" :value="p.id">
              <div style="display: flex; justify-content: space-between;">
                <span>{{ p.name }}</span>
                <span style="color: #F86C9A;">¥{{ p.salePrice }} | 库存: {{ p.stock }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item v-if="formData.consumeMode === 'product'" label="产品单价">
          <el-input v-model="productUnitPrice" disabled style="width: 100%">
            <template #prepend>¥</template>
          </el-input>
        </el-form-item>
        
        <el-form-item v-if="formData.consumeMode === 'product'" label="购买数量" prop="quantity">
          <el-input-number v-model="formData.quantity" :min="1" :max="maxQuantity" style="width: 100%" @change="calculateTotal" />
          <div v-if="selectedProduct" class="stock-info">
            <span>库存: {{ selectedProduct.stock }} 件</span>
          </div>
        </el-form-item>
        
        <el-form-item v-if="formData.consumeMode === 'product'" label="总计金额">
          <el-input v-model="productTotalAmount" disabled style="width: 100%">
            <template #prepend>¥</template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="消费类型" prop="consumeType">
          <el-select v-model="formData.consumeType" style="width: 100%" :disabled="formData.consumeMode === 'package' || formData.consumeMode === 'product'">
            <el-option v-if="formData.consumeMode === 'project'" label="现金消费" value="cash" />
            <el-option v-if="formData.consumeMode === 'project'" label="储值消费" value="stored" />
            <el-option v-if="formData.consumeMode === 'package'" label="次卡消费" value="times" />
            <el-option v-if="formData.consumeMode === 'product'" label="购买产品" value="product" />
          </el-select>
        </el-form-item>
        
        <!-- 消费金额（单独项目模式） -->
        <el-form-item v-if="formData.consumeMode === 'project'" label="消费金额" prop="amount">
          <el-input-number v-model="formData.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        
        <!-- 消费次数（套餐模式） -->
        <el-form-item v-if="formData.consumeMode === 'package'" label="消费次数" prop="consumeTimes">
          <el-input-number v-model="formData.consumeTimes" :min="1" :max="maxConsumeTimes" style="width: 100%" />
          <div v-if="selectedPurchase" class="times-info">
            <span>套餐剩余: {{ selectedPurchase.remainTimes }} 次</span>
          </div>
        </el-form-item>
        
        <el-form-item label="支付方式" prop="payMethod">
          <el-select v-model="formData.payMethod" style="width: 100%" :disabled="formData.consumeMode === 'package'">
            <el-option v-if="formData.consumeMode === 'project' || formData.consumeMode === 'product'" label="现金" value="cash" />
            <el-option v-if="formData.consumeMode === 'project' || formData.consumeMode === 'product'" label="微信" value="wechat" />
            <el-option v-if="formData.consumeMode === 'project' || formData.consumeMode === 'product'" label="支付宝" value="alipay" />
            <el-option v-if="formData.consumeMode === 'project' || formData.consumeMode === 'product'" label="银行卡" value="card" />
            <el-option v-if="formData.consumeMode === 'package'" label="次卡" value="times" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="美容师">
          <el-select v-model="formData.beauticianId" placeholder="选择美容师" style="width: 100%" clearable>
            <el-option v-for="b in beauticianOptions" :key="b.id" :label="b.realName" :value="b.id" />
          </el-select>
        </el-form-item>
        
        <!-- 产品/耗材消耗（单独项目和套餐模式） -->
        <template v-if="formData.consumeMode === 'project' || formData.consumeMode === 'package'">
          <el-form-item label="产品消耗">
            <div class="consumption-list">
              <div v-for="(item, index) in productConsumptions" :key="'product-' + index" class="consumption-row">
                <el-select v-model="item.productId" placeholder="选择产品" style="width: 200px" @change="handleConsumptionProductChange(item)">
                  <el-option v-for="p in productOptions" :key="p.id" :label="`${p.name} (库存: ${p.stock})`" :value="p.id" />
                </el-select>
                <el-input-number v-model="item.quantity" :min="1" :max="getMaxStock(item.productId)" style="width: 120px; margin-left: 8px" />
                <el-button type="danger" link @click="removeProductConsumption(index)" style="margin-left: 8px"><el-icon><Delete /></el-icon></el-button>
              </div>
              <el-button type="primary" link @click="addProductConsumption"><el-icon><Plus /></el-icon>添加产品</el-button>
            </div>
          </el-form-item>
          
          <el-form-item label="耗材消耗">
            <div class="consumption-list">
              <div v-for="(item, index) in consumableConsumptions" :key="'consumable-' + index" class="consumption-row">
                <el-select v-model="item.productId" placeholder="选择耗材" style="width: 200px" @change="handleConsumptionProductChange(item)">
                  <el-option v-for="p in productOptions" :key="p.id" :label="`${p.name} (库存: ${p.stock})`" :value="p.id" />
                </el-select>
                <el-input-number v-model="item.quantity" :min="1" :max="getMaxStock(item.productId)" style="width: 120px; margin-left: 8px" />
                <el-button type="danger" link @click="removeConsumableConsumption(index)" style="margin-left: 8px"><el-icon><Delete /></el-icon></el-button>
              </div>
              <el-button type="primary" link @click="addConsumableConsumption"><el-icon><Plus /></el-icon>添加耗材</el-button>
            </div>
          </el-form-item>
        </template>
        
        <el-form-item label="备注"><el-input v-model="formData.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getConsumePage, createConsume } from '@/api/consume'
import { getCustomerPage } from '@/api/customer'
import { getActiveProjects } from '@/api/project'
import { getCustomerPurchases } from '@/api/package'
import { getAllProducts } from '@/api/product'
import { getBeauticians } from '@/api/system'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, customerName: '', consumeType: '', payType: '' })
const formData = reactive({ 
  customerId: null, 
  consumeMode: 'project',
  projectId: null, 
  purchaseId: null,
  productId: null,
  quantity: 1,
  consumeType: 'cash', 
  amount: 0, 
  consumeTimes: 1,
  payMethod: 'cash', 
  beauticianId: null,
  remark: '' 
})
const formRules = { 
  customerId: [{ required: true, message: '请选择客户', trigger: 'change' }], 
  consumeMode: [{ required: true, message: '请选择消费模式', trigger: 'change' }],
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  purchaseId: [{ required: true, message: '请选择套餐', trigger: 'change' }],
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  consumeType: [{ required: true, message: '请选择消费类型', trigger: 'change' }],
  payMethod: [{ required: true, message: '请选择支付方式', trigger: 'change' }] 
}
const customerOptions = ref([])
const projectOptions = ref([])
const purchaseOptions = ref([])
const productOptions = ref([])
const beauticianOptions = ref([])
const selectedPurchase = ref(null)
const selectedProduct = ref(null)

// 产品消耗列表
const productConsumptions = ref([])
// 耗材消耗列表
const consumableConsumptions = ref([])

// 最大可消费次数
const maxConsumeTimes = computed(() => {
  return selectedPurchase.value?.remainTimes || 1
})

// 最大购买数量（库存）
const maxQuantity = computed(() => {
  return selectedProduct.value?.stock || 999
})

// 产品单价
const productUnitPrice = computed(() => {
  return selectedProduct.value?.salePrice || 0
})

// 产品总金额
const productTotalAmount = computed(() => {
  if (!selectedProduct.value || !formData.quantity) return '0.00'
  return (selectedProduct.value.salePrice * formData.quantity).toFixed(2)
})

async function fetchData() {
  loading.value = true
  try { 
    const res = await getConsumePage(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { 
    loading.value = false 
  }
}

function handleSearch() { queryParams.pageNum = 1; fetchData() }
function handleReset() { 
  queryParams.customerName = ''
  queryParams.consumeType = ''
  queryParams.payType = ''
  handleSearch() 
}

function openAddDialog() {
  Object.assign(formData, { 
    customerId: null, 
    consumeMode: 'project',
    projectId: null, 
    purchaseId: null,
    productId: null,
    quantity: 1,
    consumeType: 'cash', 
    amount: 0, 
    consumeTimes: 1,
    payMethod: 'cash', 
    beauticianId: null,
    remark: '' 
  })
  purchaseOptions.value = []
  selectedPurchase.value = null
  selectedProduct.value = null
  productConsumptions.value = []
  consumableConsumptions.value = []
  dialogVisible.value = true
}

function resetForm() {
  formRef.value?.clearValidate()
}

// 客户选择变化
async function handleCustomerChange(customerId) {
  purchaseOptions.value = []
  selectedPurchase.value = null
  formData.purchaseId = null
  
  if (customerId && formData.consumeMode === 'package') {
    try {
      const res = await getCustomerPurchases(customerId)
      purchaseOptions.value = res.data || []
      if (purchaseOptions.value.length === 0) {
        ElMessage.warning('该客户没有可用的套餐，请先购买套餐')
      }
    } catch (e) {
      console.error(e)
    }
  }
}

// 消费模式变化
async function handleConsumeModeChange(mode) {
  formData.projectId = null
  formData.purchaseId = null
  formData.productId = null
  selectedPurchase.value = null
  selectedProduct.value = null
  formData.quantity = 1
  
  if (mode === 'package') {
    formData.consumeType = 'times'
    formData.payMethod = 'times'
    formData.amount = 0
    formData.consumeTimes = 1
    
    // 加载客户已购买的套餐
    if (formData.customerId) {
      try {
        const res = await getCustomerPurchases(formData.customerId)
        purchaseOptions.value = res.data || []
        if (purchaseOptions.value.length === 0) {
          ElMessage.warning('该客户没有可用的套餐，请先购买套餐')
        }
      } catch (e) {
        console.error(e)
      }
    }
  } else if (mode === 'product') {
    formData.consumeType = 'product'
    formData.payMethod = 'cash'
    formData.amount = 0
    formData.consumeTimes = 1
    formData.quantity = 1
  } else {
    formData.consumeType = 'cash'
    formData.payMethod = 'cash'
    formData.consumeTimes = 1
  }
}

// 项目选择变化
function handleProjectChange(projectId) {
  const project = projectOptions.value.find(p => p.id === projectId)
  if (project) {
    formData.amount = project.price || 0
  }
}

// 套餐选择变化
function handlePurchaseChange(purchaseId) {
  selectedPurchase.value = purchaseOptions.value.find(p => p.id === purchaseId) || null
  if (selectedPurchase.value) {
    formData.consumeTimes = 1
  }
}

// 产品选择变化
function handleProductChange(productId) {
  selectedProduct.value = productOptions.value.find(p => p.id === productId) || null
  formData.quantity = 1
}

// 计算总金额
function calculateTotal() {
  // productTotalAmount 是计算属性，会自动更新
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    
    // 套餐模式下检查是否选择了套餐
    if (formData.consumeMode === 'package' && !formData.purchaseId) {
      ElMessage.warning('请选择套餐')
      return
    }
    
    // 单独项目模式下检查是否选择了项目
    if (formData.consumeMode === 'project' && !formData.projectId) {
      ElMessage.warning('请选择服务项目')
      return
    }
    
    // 购买产品模式下检查是否选择了产品
    if (formData.consumeMode === 'product' && !formData.productId) {
      ElMessage.warning('请选择产品')
      return
    }
    
    // 构建提交数据
    const submitData = { ...formData }
    
    // 购买产品模式特殊处理
    if (formData.consumeMode === 'product' && selectedProduct.value) {
      submitData.amount = selectedProduct.value.salePrice * formData.quantity
      submitData.productName = selectedProduct.value.name
    }
    
    // 处理产品/耗材消耗数据
    if (formData.consumeMode === 'project' || formData.consumeMode === 'package') {
      const allConsumptions = [
        ...productConsumptions.value.filter(item => item.productId).map(item => ({
          ...item,
          productName: getProductName(item.productId),
          type: 'product'
        })),
        ...consumableConsumptions.value.filter(item => item.productId).map(item => ({
          ...item,
          productName: getProductName(item.productId),
          type: 'consumable'
        }))
      ]
      if (allConsumptions.length > 0) {
        submitData.productConsumptions = JSON.stringify(allConsumptions)
      }
    }
    
    submitLoading.value = true
    await createConsume(submitData)
    ElMessage.success('开单成功')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  } finally { 
    submitLoading.value = false 
  }
}

// 产品/耗材消耗相关方法
function addProductConsumption() {
  productConsumptions.value.push({ productId: null, quantity: 1 })
}

function removeProductConsumption(index) {
  productConsumptions.value.splice(index, 1)
}

function addConsumableConsumption() {
  consumableConsumptions.value.push({ productId: null, quantity: 1 })
}

function removeConsumableConsumption(index) {
  consumableConsumptions.value.splice(index, 1)
}

function handleConsumptionProductChange(item) {
  item.quantity = 1
}

function getMaxStock(productId) {
  const product = productOptions.value.find(p => p.id === productId)
  return product ? product.stock : 999
}

function getProductName(productId) {
  const product = productOptions.value.find(p => p.id === productId)
  return product ? product.name : ''
}

function getConsumeTypeTag(type) { 
  const types = { cash: 'primary', times: 'success', stored: 'warning', product: 'info' }
  return types[type] || '' 
}

function getConsumeTypeName(type) { 
  const names = { cash: '现金消费', times: '次卡消费', stored: '储值消费', product: '购买产品' }
  return names[type] || '-' 
}

function getPayTypeName(type) { 
  const names = { cash: '现金', wechat: '微信', alipay: '支付宝', card: '银行卡', stored: '储值卡', times: '次卡' }
  return names[type] || '-' 
}

function formatTime(time) {
  if (!time) return '-'
  if (typeof time === 'string' && time.includes('T')) {
    return time.replace('T', ' ').substring(0, 19)
  }
  return time
}

onMounted(() => {
  fetchData()
  getCustomerPage({ pageSize: 100 }).then(res => customerOptions.value = res.data?.records || [])
  getActiveProjects().then(res => projectOptions.value = res.data || [])
  getAllProducts().then(res => productOptions.value = (res.data || []).filter(p => p.stock > 0 && p.status === 1))
  getBeauticians().then(res => beauticianOptions.value = res.data || [])
})
</script>

<style lang="scss" scoped>
.consume-page {
  .page-header { 
    display: flex; 
    justify-content: space-between; 
    align-items: flex-start; 
    margin-bottom: 20px;
    .page-title { font-size: 24px; font-weight: 600; margin-bottom: 4px; }
    .page-desc { font-size: 14px; color: #666; }
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
  .amount { color: #F86C9A; font-weight: 600; }
  .times { color: #52c41a; font-weight: 600; }
  .pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
  
  .purchase-info {
    margin-top: 4px;
    font-size: 12px;
    color: #999;
  }
  
  .times-info {
    margin-top: 4px;
    font-size: 12px;
    color: #52c41a;
  }
  
  .stock-info {
    margin-top: 4px;
    font-size: 12px;
    color: #F86C9A;
  }
  
  .consumption-list {
    display: flex;
    flex-direction: column;
  }
  
  .consumption-row {
    display: flex;
    align-items: center;
    margin-bottom: 8px;
  }
}
</style>
