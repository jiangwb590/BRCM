<template>
  <div class="package-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">套餐管理</h1>
        <p class="page-desc">管理服务套餐组合</p>
      </div>
      <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon>新增</el-button>
    </div>
    
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="packageName" label="套餐名称" min-width="180" />
<!--        <el-table-column prop="packageCode" label="套餐编码" width="120" />-->
        <el-table-column label="疗程价" width="120">
          <template #default="{ row }">
            <span class="originalPrice">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column label="卡扣价" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="validDays" label="有效期(天)" width="100" />
        <el-table-column prop="times" label="包含次数" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="套餐名称" prop="packageName"><el-input v-model="formData.packageName" /></el-form-item>
<!--        <el-form-item label="套餐编码"><el-input v-model="formData.packageCode" /></el-form-item>-->
        <el-form-item label="疗程价" prop="originalPrice"><el-input-number v-model="formData.originalPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="卡扣价" prop="price"><el-input-number v-model="formData.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="次数" prop="times"><el-input-number v-model="formData.times" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="有效期"><el-input-number v-model="formData.validDays" :min="1" style="width: 100%" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="formData.description" type="textarea" :rows="3" /></el-form-item>
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
import { getPackagePage, addPackage, updatePackage, deletePackage } from '@/api/package'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增套餐')
const formRef = ref(null)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10 })
const formData = reactive({ id: null, packageName: '', packageCode: '', price: 0, originalPrice: null, times: 10, validDays: 365, description: '' })
const formRules = { packageName: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }], price: [{ required: true, message: '请输入价格', trigger: 'blur' }], times: [{ required: true, message: '请输入次数', trigger: 'blur' }] }

async function fetchData() {
  loading.value = true
  try { const res = await getPackagePage(queryParams); tableData.value = res.data?.records || []; total.value = res.data?.total || 0 }
  finally { loading.value = false }
}

function openAddDialog() {
  dialogTitle.value = '新增套餐'
  Object.assign(formData, { id: null, packageName: '', packageCode: '', price: 0, originalPrice: null, times: 10, validDays: 365, description: '' })
  dialogVisible.value = true
}

function openEditDialog(row) { dialogTitle.value = '编辑套餐'; Object.assign(formData, row); dialogVisible.value = true }

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (formData.id) { await updatePackage(formData); ElMessage.success('修改成功') }
    else { await addPackage(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

async function handleDelete(row) {
  try { await ElMessageBox.confirm('确定删除该套餐？', '提示', { type: 'warning' }); await deletePackage(row.id); ElMessage.success('删除成功'); fetchData() } catch (e) {}
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
.package-page {
  .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;
    .page-title { font-size: 24px; font-weight: 600; margin-bottom: 4px; }
    .page-desc { font-size: 14px; color: #666; }
  }
  .table-section { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
  .price { color: #F86C9A; font-weight: 600; }
  .pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
}
</style>
