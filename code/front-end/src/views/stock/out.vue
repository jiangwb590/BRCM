<template>
  <div class="stock-page">
    <div class="page-header">
      <h1 class="page-title">出库管理</h1>
      <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon>新增出库</el-button>
    </div>
    
    <div class="filter-section">
      <el-form :model="queryParams" inline>
        <el-form-item label="产品名称">
          <el-input v-model="queryParams.productName" placeholder="产品名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="stockOutNo" label="出库单号" width="180" />
        <el-table-column prop="productName" label="产品名称" min-width="150" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="unitPrice" label="单价" width="100">
          <template #default="{ row }">¥{{ row.unitPrice }}</template>
        </el-table-column>
        <el-table-column prop="stockOutType" label="出库类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getStockOutType(row.stockOutType)" size="small">{{ getStockOutTypeName(row.stockOutType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="stockOutTime" label="出库时间" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '已作废' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>
    
    <!-- 新增出库对话框 -->
    <el-dialog v-model="dialogVisible" title="新增出库" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="产品" prop="productId">
          <el-select v-model="formData.productId" filterable placeholder="选择产品" style="width: 100%" @change="handleProductChange">
            <el-option v-for="p in productOptions" :key="p.id" :label="`${p.name} (库存: ${p.stock})`" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="当前库存">
          <span v-if="currentStock > 0">{{ currentStock }}</span>
          <span v-else style="color: #ff4d4f;">库存不足，无法出库</span>
        </el-form-item>
        <el-form-item label="出库数量" prop="quantity">
          <el-input-number v-model="formData.quantity" :min="1" :max="Math.max(1, currentStock)" style="width: 100%" :disabled="currentStock <= 0" />
        </el-form-item>
        <el-form-item label="出库类型">
          <el-select v-model="formData.stockOutType" style="width: 100%">
            <el-option label="服务消耗" :value="1" />
            <el-option label="报废" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" :disabled="currentStock <= 0" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getStockOutPage, addStockOut, getAllProducts } from '@/api/product'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const formRef = ref(null)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, productName: '' })
const productOptions = ref([])

const formData = reactive({
  productId: null,
  quantity: 1,
  stockOutType: 1,
  remark: ''
})

const formRules = {
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

const currentStock = computed(() => {
  const product = productOptions.value.find(p => p.id === formData.productId)
  return product?.stock || 0
})

function getStockOutType(type) {
  const types = { 1: 'primary', 2: 'danger', 3: 'info' }
  return types[type] || ''
}

function getStockOutTypeName(type) {
  const names = { 1: '服务消耗', 2: '报废', 3: '其他' }
  return names[type] || '-'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getStockOutPage(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() { queryParams.pageNum = 1; fetchData() }
function handleReset() { queryParams.productName = ''; handleSearch() }

function openAddDialog() {
  Object.assign(formData, { productId: null, quantity: 1, stockOutType: 1, remark: '' })
  dialogVisible.value = true
}

function handleProductChange(productId) {
  const product = productOptions.value.find(p => p.id === productId)
  if (product && formData.quantity > product.stock) {
    formData.quantity = product.stock
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    
    if (formData.quantity > currentStock.value) {
      ElMessage.error('出库数量不能超过库存数量')
      return
    }
    
    submitLoading.value = true
    
    const product = productOptions.value.find(p => p.id === formData.productId)
    await addStockOut({
      productId: formData.productId,
      productName: product?.name || '',
      productCode: product?.productCode || '',
      quantity: formData.quantity,
      unitPrice: product?.salePrice || 0,
      totalPrice: formData.quantity * (product?.salePrice || 0),
      stockOutType: formData.stockOutType,
      remark: formData.remark
    })
    
    ElMessage.success('出库成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  getAllProducts().then(res => productOptions.value = res.data || [])
})
</script>

<style lang="scss" scoped>
.stock-page {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; .page-title { font-size: 24px; font-weight: 600; } }
  .filter-section { background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
  .table-section { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
  .pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
}
</style>