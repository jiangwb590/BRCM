<template>
  <div class="project-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">项目管理</h1>
        <p class="page-desc">管理服务项目和价格</p>
      </div>
      <el-button type="primary" @click="openAddDialog" v-permission="'project:add'"><el-icon><Plus /></el-icon>新增</el-button>
    </div>
    
    <div class="filter-section">
      <el-form :model="queryParams" inline>
        <el-form-item label="项目名称">
          <el-input v-model="queryParams.name" placeholder="请输入" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
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
        <el-table-column prop="name" label="项目名称" min-width="100" />
        <el-table-column prop="categoryName" label="分类" width="150" />
        <el-table-column label="价格" width="120">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
            <span v-if="row.originalPrice" class="original-price">¥{{ row.originalPrice }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长(分钟)" width="100" />
<!--        <el-table-column prop="serviceTimes" label="服务次数" width="100" />-->
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row, $event)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)" v-permission="'project:edit'">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-permission="'project:delete'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>
    
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="项目名称" prop="name"><el-input v-model="formData.name" /></el-form-item>
        <el-form-item label="分类" prop="categoryName"><el-input v-model="formData.categoryName" /></el-form-item>
        <el-form-item label="价格" prop="price"><el-input-number v-model="formData.price" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="formData.originalPrice" :min="0" :precision="2" style="width: 100%" /></el-form-item>
        <el-form-item label="时长"><el-input-number v-model="formData.duration" :min="0" style="width: 100%" /></el-form-item>
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
import { getProjectPage, addProject, updateProject, deleteProject, updateProjectStatus } from '@/api/project'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增项目')
const formRef = ref(null)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, name: '', status: '' })
const formData = reactive({ id: null, name: '', categoryName: '', price: 0, originalPrice: null, duration: 60, description: '' })
const formRules = { name: [{ required: true, message: '请输入项目名称', trigger: 'blur' }], price: [{ required: true, message: '请输入价格', trigger: 'blur' }] }

async function fetchData() {
  loading.value = true
  try {
    const res = await getProjectPage(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally { loading.value = false }
}

function handleSearch() { queryParams.pageNum = 1; fetchData() }
function handleReset() { queryParams.name = ''; queryParams.status = ''; handleSearch() }

function openAddDialog() {
  dialogTitle.value = '新增项目'
  Object.assign(formData, { id: null, name: '', categoryName: '', price: 0, originalPrice: null, duration: 60, description: '' })
  dialogVisible.value = true
}

function openEditDialog(row) {
  dialogTitle.value = '编辑项目'
  Object.assign(formData, row)
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (formData.id) { await updateProject(formData); ElMessage.success('修改成功') }
    else { await addProject(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false
    fetchData()
  } finally { submitLoading.value = false }
}

async function handleStatusChange(row, status) {
  try { await updateProjectStatus(row.id, status); row.status = status; ElMessage.success('状态更新成功') }
  catch (e) { row.status = status === 1 ? 0 : 1 }
}

async function handleDelete(row) {
  try { await ElMessageBox.confirm('确定删除该项目？', '提示', { type: 'warning' }); await deleteProject(row.id); ElMessage.success('删除成功'); fetchData() } catch (e) {}
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
.project-page {
  .page-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 20px;
    .page-title { font-size: 24px; font-weight: 600; margin-bottom: 4px; }
    .page-desc { font-size: 14px; color: #666; }
  }
  .filter-section { background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 16px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
  .table-section { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
  .price { color: #F86C9A; font-weight: 600; }
  .original-price { color: #999; text-decoration: line-through; font-size: 12px; margin-left: 8px; }
  .pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
}
</style>
