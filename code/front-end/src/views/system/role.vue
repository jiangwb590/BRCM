<template>
  <div class="system-page">
    <div class="page-header">
      <h1 class="page-title">角色管理</h1>
      <el-button type="primary" @click="openAddDialog" v-permission="'role:add'"><el-icon><Plus /></el-icon>新增</el-button>
    </div>
    
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="remark" label="备注" min-width="200" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)" v-permission="'role:edit'">编辑</el-button>
            <el-button type="primary" link @click="openPermissionDialog(row)" v-permission="'role:edit'">权限</el-button>
            <el-button type="danger" link @click="handleDelete(row)" v-permission="'role:delete'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" />
      </div>
    </div>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
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
    
    <!-- 权限配置对话框 -->
    <el-dialog v-model="permissionVisible" title="权限配置" width="500px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'menuName', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
        v-loading="treeLoading"
      >
        <template #default="{ data }">
          <span class="tree-node">
            <span>{{ data.menuName }}</span>
            <el-tag v-if="data.menuType === 3" type="info" size="small" class="perm-tag">{{ data.perms }}</el-tag>
            <el-tag v-else-if="data.menuType === 2" type="success" size="small" class="type-tag">菜单</el-tag>
            <el-tag v-else type="warning" size="small" class="type-tag">目录</el-tag>
          </span>
        </template>
      </el-tree>
      <template #footer>
        <el-button @click="permissionVisible = false">取消</el-button>
        <el-button type="primary" :loading="permissionLoading" @click="savePermission">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getRolePage, addRole, updateRole, deleteRole, getMenuTree, assignRoleMenus, getRoleMenuIds } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitLoading = ref(false)
const permissionLoading = ref(false)
const treeLoading = ref(false)
const dialogVisible = ref(false)
const permissionVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const menuTreeRef = ref(null)
const tableData = ref([])
const menuTree = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10 })
const currentRoleId = ref(null)

const formData = reactive({
  id: null,
  roleName: '',
  roleCode: '',
  status: 1,
  remark: ''
})

const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getRolePage(queryParams)
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  isEdit.value = false
  Object.assign(formData, { id: null, roleName: '', roleCode: '', status: 1, remark: '' })
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  Object.assign(formData, { id: row.id, roleName: row.roleName, roleCode: row.roleCode, status: row.status, remark: row.remark })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
      await updateRole(formData)
      ElMessage.success('修改成功')
    } else {
      await addRole(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function openPermissionDialog(row) {
  currentRoleId.value = row.id
  permissionVisible.value = true
  treeLoading.value = true
  
  try {
    const [treeRes, menuRes] = await Promise.all([
      getMenuTree(),
      getRoleMenuIds(row.id)
    ])
    menuTree.value = treeRes.data || []
    
    setTimeout(() => {
      if (menuTreeRef.value && menuRes.data) {
        menuTreeRef.value.setCheckedKeys(menuRes.data)
      }
    }, 100)
  } finally {
    treeLoading.value = false
  }
}

async function savePermission() {
  if (!menuTreeRef.value) return
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  permissionLoading.value = true
  try {
    await assignRoleMenus(currentRoleId.value, checkedKeys)
    ElMessage.success('权限配置成功')
    permissionVisible.value = false
  } finally {
    permissionLoading.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该角色吗？', '提示', { type: 'warning' })
    await deleteRole(row.id)
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
  
  .tree-node {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .perm-tag {
      font-size: 12px;
      margin-left: 4px;
    }
    
    .type-tag {
      font-size: 12px;
      margin-left: 4px;
    }
  }
}
</style>
