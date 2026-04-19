<template>
  <div class="product-page">
    <div class="page-header">
      <h1 class="page-title">产品管理</h1>
      <el-button type="primary" @click="openAddDialog" v-permission="'product:add'"><el-icon><Plus /></el-icon>新增</el-button>
    </div>
    
    <div class="stat-cards">
      <div class="stat-card"><div class="stat-value">{{ stats.total }}</div><div class="stat-label">产品总数</div></div>
      <div class="stat-card warning"><div class="stat-value">{{ stats.warning }}</div><div class="stat-label">库存预警</div></div>
    </div>
    
    <div class="filter-section">
      <el-form :model="queryParams" inline>
        <el-form-item label="分类">
          <el-select v-model="queryParams.categoryName" placeholder="全部" clearable style="width: 150px">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.dictLabel" :value="c.dictLabel" />
          </el-select>
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="queryParams.name" placeholder="请输入产品名称" clearable style="width: 180px" />
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
        <el-table-column prop="name" label="产品名称" min-width="150" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="specification" label="规格" width="100" />
        <el-table-column prop="stock" label="库存" width="100">
          <template #default="{ row }">
            <span :class="{ 'low-stock': row.stock <= row.stockWarning }">{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stockWarning" label="预警值" width="80" />
        <el-table-column prop="salePrice" label="售价" width="100">
          <template #default="{ row }">¥{{ row.salePrice }}</template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="240">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleStockIn(row)" v-permission="'stock:in'">入库</el-button>
            <el-button type="primary" link @click="handleStockOut(row)" v-permission="'stock:out'">出库</el-button>
            <el-button type="primary" link @click="openEditDialog(row)" v-permission="'product:edit'">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-permission="'product:delete'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑产品' : '新增产品'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="产品名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入产品名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%" @change="handleCategoryChange">
            <el-option v-for="c in categoryOptions" :key="c.id" :label="c.dictLabel" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="formData.specification" placeholder="请输入规格" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="formData.unit" placeholder="请输入单位" style="width: 100%" />
        </el-form-item>
        <el-form-item label="进货价" prop="purchasePrice">
          <el-input-number v-model="formData.purchasePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="售价" prop="salePrice">
          <el-input-number v-model="formData.salePrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <!-- 库存通过入库功能添加，新增产品时默认为0 -->
        <el-form-item label="预警值" prop="stockWarning">
          <el-input-number v-model="formData.stockWarning" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 入库对话框 -->
    <el-dialog v-model="stockInVisible" title="入库" width="400px">
      <el-form :model="stockInData" label-width="80px">
        <el-form-item label="产品"><span>{{ currentProduct.name }}</span></el-form-item>
        <el-form-item label="当前库存"><span>{{ currentProduct.stock }}</span></el-form-item>
        <el-form-item label="入库数量">
          <el-input-number v-model="stockInData.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="stockInData.unitPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="stockInData.supplier" placeholder="供应商名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockInVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockInLoading" @click="submitStockIn">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 出库对话框 -->
    <el-dialog v-model="stockOutVisible" title="出库" width="400px">
      <el-form :model="stockOutData" label-width="80px">
        <el-form-item label="产品"><span>{{ currentProduct.name }}</span></el-form-item>
        <el-form-item label="当前库存">
          <span v-if="currentProduct.stock > 0">{{ currentProduct.stock }}</span>
          <span v-else style="color: #ff4d4f;">库存不足，无法出库</span>
        </el-form-item>
        <el-form-item label="出库数量">
          <el-input-number v-model="stockOutData.quantity" :min="1" :max="Math.max(1, currentProduct.stock || 0)" style="width: 100%" :disabled="!currentProduct.stock || currentProduct.stock <= 0" />
        </el-form-item>
        <el-form-item label="出库类型">
          <el-select v-model="stockOutData.stockOutType" style="width: 100%">
            <el-option label="服务消耗" :value="1" />
            <el-option label="报废" :value="2" />
            <el-option label="其他" :value="3" />
            <el-option label="客户消费" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="stockOutData.stockOutType === 4" label="客户">
          <el-select v-model="stockOutData.customerId" filterable placeholder="请选择客户" style="width: 100%">
            <el-option v-for="c in customerOptions" :key="c.id" :label="c.name + (c.phone ? ' (' + c.phone + ')' : '')" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="stockOutData.remark" type="textarea" :rows="2" placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockOutVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockOutLoading" :disabled="!currentProduct.stock || currentProduct.stock <= 0" @click="submitStockOut">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProductPage, addProduct, updateProduct, deleteProduct, addStockIn, addStockOut, getStockWarningProducts, exportProducts } from '@/api/product'
import { getDictByCode } from '@/api/system'
import { getCustomerAll } from '@/api/customer'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const stockInLoading = ref(false)
const stockOutLoading = ref(false)
const dialogVisible = ref(false)
const stockInVisible = ref(false)
const stockOutVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const categoryOptions = ref([])
const stats = reactive({ total: 0, warning: 0 })

const formData = reactive({
  id: null,
  name: '',
  categoryId: null,
  categoryName: '',
  specification: '',
  unit: '件',
  purchasePrice: 0,
  salePrice: 0,
  stock: 0,
  stockWarning: 10
})

const formRules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  salePrice: [{ required: true, message: '请输入售价', trigger: 'blur' }]
}

const currentProduct = ref({})
const stockInData = reactive({ quantity: 1, unitPrice: 0, supplier: '' })
const stockOutData = reactive({ quantity: 1, stockOutType: 1, remark: '', customerId: null })
const customerOptions = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, categoryName: '', name: '' })

async function fetchData() {
  loading.value = true
  try {
    const res = await getProductPage(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
    stats.total = total.value
  } finally {
    loading.value = false
  }
}

async function fetchStockWarning() {
  try {
    const res = await getStockWarningProducts()
    stats.warning = res.data?.length || 0
  } catch (e) {
    console.error('获取库存预警失败', e)
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  fetchData()
}

function handleReset() {
  queryParams.categoryName = ''
  queryParams.name = ''
  queryParams.pageNum = 1
  fetchData()
}

async function handleExport() {
  try {
    const res = await exportProducts()
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '产品列表.xlsx'
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
    console.error(e)
  }
}

async function fetchCategories() {
  try {
    const res = await getDictByCode('product_category')
    categoryOptions.value = res.data || []
  } catch (e) {
    console.error('获取产品分类失败', e)
  }
}

function handleCategoryChange(categoryId) {
  const category = categoryOptions.value.find(c => c.id === categoryId)
  formData.categoryName = category ? category.dictLabel : ''
}

function openAddDialog() {
  isEdit.value = false
  Object.assign(formData, { id: null, name: '', categoryId: null, categoryName: '', specification: '', unit: '件', purchasePrice: 0, salePrice: 0, stock: 0, stockWarning: 10 })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
      await updateProduct(formData)
      ElMessage.success('修改成功')
    } else {
      await addProduct(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定要删除产品 "${row.name}" 吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

function handleStockIn(row) {
  currentProduct.value = row
  stockInData.quantity = 1
  stockInData.unitPrice = row.purchasePrice || 0
  stockInData.supplier = ''
  stockInVisible.value = true
}

async function submitStockIn() {
  stockInLoading.value = true
  try {
    await addStockIn({
      productId: currentProduct.value.id,
      productName: currentProduct.value.name,
      productCode: currentProduct.value.productCode,
      quantity: stockInData.quantity,
      unitPrice: stockInData.unitPrice,
      totalPrice: stockInData.quantity * stockInData.unitPrice,
      supplier: stockInData.supplier,
      stockInType: 1
    })
    ElMessage.success('入库成功')
    stockInVisible.value = false
    fetchData()
  } finally {
    stockInLoading.value = false
  }
}

function handleStockOut(row) {
  currentProduct.value = row
  stockOutData.quantity = 1
  stockOutData.stockOutType = 1
  stockOutData.remark = ''
  stockOutData.customerId = null
  stockOutVisible.value = true
  getCustomerAll().then(res => customerOptions.value = res.data || [])
}

async function submitStockOut() {
  stockOutLoading.value = true
  try {
    await addStockOut({
      productId: currentProduct.value.id,
      productName: currentProduct.value.name,
      productCode: currentProduct.value.productCode,
      quantity: stockOutData.quantity,
      unitPrice: currentProduct.value.salePrice,
      totalPrice: stockOutData.quantity * currentProduct.value.salePrice,
      stockOutType: stockOutData.stockOutType,
      remark: stockOutData.remark,
      customerId: stockOutData.stockOutType === 4 ? stockOutData.customerId : null,
      customerName: stockOutData.stockOutType === 4 ? (customerOptions.value.find(c => c.id === stockOutData.customerId)?.name || '') : null
    })
    ElMessage.success('出库成功')
    stockOutVisible.value = false
    fetchData()
  } finally {
    stockOutLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchCategories()
  fetchStockWarning()
})
</script>

<style lang="scss" scoped>
.product-page {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; .page-title { font-size: 24px; font-weight: 600; } }
  .stat-cards { display: flex; gap: 20px; margin-bottom: 20px;
    .stat-card { background: #fff; border-radius: 12px; padding: 20px; min-width: 150px; text-align: center; box-shadow: 0 4px 12px rgba(0,0,0,0.04);
      .stat-value { font-size: 28px; font-weight: 600; color: #333; }
      .stat-label { font-size: 14px; color: #666; margin-top: 4px; }
      &.warning .stat-value { color: #ff9900; }
    }
  }
  .filter-section { background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
  .table-section { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
  .pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
  .low-stock { color: #ff4d4f; font-weight: 600; }
}
</style>