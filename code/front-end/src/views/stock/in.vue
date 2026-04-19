<template>
  <div class="stock-page">
    <div class="page-header">
      <h1 class="page-title">入库管理</h1>
      <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon>新增入库</el-button>
    </div>
    
    <div class="filter-section">
      <el-form :model="queryParams" inline>
        <el-form-item label="产品名称">
          <el-input v-model="queryParams.productName" placeholder="产品名称" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="stockInNo" label="入库单号" width="180" />
        <el-table-column prop="productName" label="产品名称" min-width="150" />
        <el-table-column prop="quantity" label="数量" width="100" />
        <el-table-column prop="unitPrice" label="单价" width="100">
          <template #default="{ row }">¥{{ row.unitPrice }}</template>
        </el-table-column>
        <el-table-column prop="totalPrice" label="总价" width="100">
          <template #default="{ row }">¥{{ row.totalPrice }}</template>
        </el-table-column>
        <el-table-column prop="supplier" label="供应商" width="150" />
        <el-table-column prop="stockInTime" label="入库时间" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '正常' : '已作废' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" type="danger" link @click="handleCancel(row)"><el-icon><Delete /></el-icon>作废</el-button>
            <span v-else style="color: #999;">-</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>
    
    <!-- 新增入库对话框 -->
    <el-dialog v-model="dialogVisible" title="新增入库" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="产品" prop="productId">
          <el-select v-model="formData.productId" filterable placeholder="选择产品" style="width: 100%">
            <el-option v-for="p in productOptions" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="formData.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number v-model="formData.unitPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="入库类型">
          <el-select v-model="formData.stockInType" style="width: 100%">
            <el-option label="采购入库" :value="1" />
            <el-option label="退货入库" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="formData.supplier" placeholder="供应商名称" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
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
import { getStockInPage, addStockIn, cancelStockIn, getAllProducts, exportStockIn } from '@/api/product'
import { ElMessage, ElMessageBox } from 'element-plus'

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
  unitPrice: 0,
  stockInType: 1,
  supplier: '',
  remark: ''
})

const formRules = {
  productId: [{ required: true, message: '请选择产品', trigger: 'change' }],
  quantity: [{ required: true, message: '请输入数量', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getStockInPage(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() { queryParams.pageNum = 1; fetchData() }
function handleReset() { queryParams.productName = ''; handleSearch() }

async function handleExport() {
  try {
    const res = await exportStockIn()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '入库记录.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
    console.error(e)
  }
}

function openAddDialog() {
  Object.assign(formData, { productId: null, quantity: 1, unitPrice: 0, stockInType: 1, supplier: '', remark: '' })
  dialogVisible.value = true
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm(
      `确定要作废入库单 "${row.stockInNo}" 吗？作废后将回滚库存。`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await cancelStockIn(row.id)
    ElMessage.success('作废成功')
    fetchData()
    getAllProducts().then(res => productOptions.value = res.data || [])
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    const product = productOptions.value.find(p => p.id === formData.productId)
    await addStockIn({
      productId: formData.productId,
      productName: product?.name || '',
      productCode: product?.productCode || '',
      quantity: formData.quantity,
      unitPrice: formData.unitPrice,
      totalPrice: formData.quantity * formData.unitPrice,
      stockInType: formData.stockInType,
      supplier: formData.supplier,
      remark: formData.remark
    })
    
    ElMessage.success('入库成功')
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