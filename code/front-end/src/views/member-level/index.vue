<template>
  <div class="member-level-page">
    <div class="page-header">
      <h1 class="page-title">会员等级</h1>
    </div>
    
    <div class="table-section">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="levelName" label="等级名称" width="150" />
        <el-table-column prop="levelCode" label="等级编码" width="120" />
        <el-table-column label="消费区间" width="200">
          <template #default="{ row }">¥{{ row.minAmount }} - ¥{{ row.maxAmount || '以上' }}</template>
        </el-table-column>
        <el-table-column prop="discount" label="折扣" width="100">
          <template #default="{ row }">{{ (row.discount * 10).toFixed(1) }}折</template>
        </el-table-column>
        <el-table-column prop="benefits" label="权益" min-width="200" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const loading = ref(false)
const tableData = ref([
  { id: 1, levelName: '普通会员', levelCode: 'normal', minAmount: 0, maxAmount: 999, discount: 0.95, benefits: '基础服务', status: 1 },
  { id: 2, levelName: '银卡会员', levelCode: 'silver', minAmount: 1000, maxAmount: 2999, discount: 0.9, benefits: '生日关怀、专属优惠', status: 1 },
  { id: 3, levelName: '金卡会员', levelCode: 'gold', minAmount: 3000, maxAmount: 5999, discount: 0.85, benefits: '生日礼物、专属客服、优先预约', status: 1 },
  { id: 4, levelName: '钻石会员', levelCode: 'diamond', minAmount: 6000, maxAmount: null, discount: 0.8, benefits: 'VIP专享、免费体验、年度礼包', status: 1 }
])

function openEditDialog(row) { console.log('Edit:', row) }

onMounted(() => {})
</script>

<style lang="scss" scoped>
.member-level-page {
  .page-header { margin-bottom: 20px; .page-title { font-size: 24px; font-weight: 600; } }
  .table-section { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 4px 12px rgba(0,0,0,0.04); }
}
</style>
