<template>
  <div class="system-page">
    <div class="page-header">
      <h1 class="page-title">字典管理</h1>
      <el-button type="primary" @click="openAddDialog"><el-icon><Plus /></el-icon>新增</el-button>
    </div>
    
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="dictName" label="字典名称" width="150" />
        <el-table-column prop="dictCode" label="字典编码" width="150" />
        <el-table-column prop="dictValue" label="值" width="100" />
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
            <el-button type="primary" link @click="openItemDialog(row)">字典项</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑字典' : '新增字典'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="formData.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="formData.dictCode" placeholder="请输入字典编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="formData.dictValue" placeholder="配置项的值（如每日预约上限数）" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 字典项对话框 -->
    <el-dialog v-model="itemVisible" :title="`${currentDict.dictName} - 字典项`" width="700px">
      <div style="margin-bottom: 16px;">
        <el-button type="primary" size="small" @click="addItem"><el-icon><Plus /></el-icon>新增</el-button>
      </div>
      <el-table :data="dictItems" border size="small" v-loading="itemLoading">
        <el-table-column prop="dictLabel" label="标签" width="120" />
        <el-table-column prop="dictValue" label="值" width="100" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="editItem(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="deleteItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="itemVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    
    <!-- 字典项编辑对话框 -->
    <el-dialog v-model="itemFormVisible" :title="itemIsEdit ? '编辑字典项' : '新增字典项'" width="400px">
      <el-form :model="itemFormData" label-width="60px">
        <el-form-item label="标签"><el-input v-model="itemFormData.dictLabel" /></el-form-item>
        <el-form-item label="值"><el-input v-model="itemFormData.dictValue" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="itemFormData.sort" :min="0" style="width: 100%" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="itemFormData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemFormVisible = false">取消</el-button>
        <el-button type="primary" :loading="itemSubmitLoading" @click="saveItem">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getDictPage, addDict, updateDict, deleteDict, getDictItems, addDictItem, updateDictItem, deleteDictItem } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const itemLoading = ref(false)
const itemSubmitLoading = ref(false)
const dialogVisible = ref(false)
const itemVisible = ref(false)
const itemFormVisible = ref(false)
const isEdit = ref(false)
const itemIsEdit = ref(false)
const formRef = ref(null)
const tableData = ref([])
const dictItems = ref([])
const currentDict = ref({})
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10 })

const formData = reactive({
  id: null,
  dictName: '',
  dictCode: '',
  dictValue: '',
  status: 1,
  remark: ''
})

const itemFormData = reactive({
  id: null,
  parentId: null,
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: 1
})

const formRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictCode: [{ required: true, message: '请输入字典编码', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getDictPage(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  isEdit.value = false
  Object.assign(formData, { id: null, dictName: '', dictCode: '', status: 1, remark: '' })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  Object.assign(formData, { id: row.id, dictName: row.dictName, dictCode: row.dictCode, status: row.status, remark: row.remark })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
      await updateDict(formData)
      ElMessage.success('修改成功')
    } else {
      await addDict(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function openItemDialog(row) {
  currentDict.value = row
  itemVisible.value = true
  itemLoading.value = true
  try {
    const res = await getDictItems(row.id)
    dictItems.value = res.data || []
  } finally {
    itemLoading.value = false
  }
}

function addItem() {
  itemIsEdit.value = false
  Object.assign(itemFormData, { id: null, parentId: currentDict.value.id, dictLabel: '', dictValue: '', sort: 0, status: 1 })
  itemFormVisible.value = true
}

function editItem(row) {
  itemIsEdit.value = true
  Object.assign(itemFormData, { id: row.id, parentId: row.parentId, dictLabel: row.dictLabel, dictValue: row.dictValue, sort: row.sort, status: row.status })
  itemFormVisible.value = true
}

async function saveItem() {
  itemSubmitLoading.value = true
  try {
    if (itemIsEdit.value) {
      await updateDictItem(itemFormData)
      ElMessage.success('修改成功')
    } else {
      await addDictItem(itemFormData)
      ElMessage.success('新增成功')
    }
    itemFormVisible.value = false
    const res = await getDictItems(currentDict.value.id)
    dictItems.value = res.data || []
  } finally {
    itemSubmitLoading.value = false
  }
}

async function deleteItem(row) {
  try {
    await ElMessageBox.confirm('确定删除该字典项？', '提示', { type: 'warning' })
    await deleteDictItem(row.id)
    ElMessage.success('删除成功')
    const res = await getDictItems(currentDict.value.id)
    dictItems.value = res.data || []
  } catch (e) {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该字典吗？', '提示', { type: 'warning' })
    await deleteDict(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (e) {}
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
.system-page {
  .page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; .page-title { font-size: 24px; font-weight: 600; } }
  .table-section { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
  .pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
}
</style>
