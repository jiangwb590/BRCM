<template>
  <div class="customer-detail-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button @click="router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>
    
    <!-- 客户基本信息 -->
    <div class="info-card">
      <div class="customer-header">
        <el-avatar :size="80" class="avatar">{{ customer.name?.charAt(0) }}</el-avatar>
        <div class="customer-info">
          <h2>{{ customer.name }}</h2>
          <div class="info-row">
            <el-tag :class="getCategoryTagClass(customer.category)" size="small">
              {{ customer.category || '潜在客户' }}
            </el-tag>
            <el-tag v-if="customer.memberLevelName" :class="getMemberTagClass(customer.memberLevelName)" size="small">
              {{ customer.memberLevelName }}
            </el-tag>
          </div>
        </div>
        <div class="customer-stats">
          <div class="stat-item">
            <div class="value">¥{{ formatNumber(customer.totalAmount || 0) }}</div>
            <div class="label">累计消费</div>
          </div>
          <div class="stat-item">
            <div class="value">{{ customer.consumeCount || 0 }}</div>
            <div class="label">消费次数</div>
          </div>
          <div class="stat-item">
            <div class="value">¥{{ formatNumber(customer.balance || 0) }}</div>
            <div class="label">储值余额</div>
          </div>
        </div>
      </div>
      
      <el-descriptions :column="4" border class="info-desc">
        <el-descriptions-item label="手机号">{{ customer.phone }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ customer.gender === 1 ? '男' : '女' }}</el-descriptions-item>
        <el-descriptions-item label="生日">{{ customer.birthday || '-' }}</el-descriptions-item>
        <el-descriptions-item label="微信号">{{ customer.wechat || '-' }}</el-descriptions-item>
        <el-descriptions-item label="客户来源">{{ customer.source || '-' }}</el-descriptions-item>
        <el-descriptions-item label="最近消费">{{ formatDate(customer.lastConsumeTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(customer.createTime) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ customer.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </div>
    
    <!-- 选项卡 -->
    <el-tabs v-model="activeTab" class="detail-tabs">
      <el-tab-pane label="消费记录" name="consume">
        <el-table :data="consumeList" v-loading="consumeLoading">
          <el-table-column prop="consumeNo" label="单号" width="180" />
          <el-table-column prop="projectName" label="项目" width="150" />
          <el-table-column prop="consumeType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="getConsumeTypeTag(row.consumeType)">{{ getConsumeTypeName(row.consumeType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="100">
            <template #default="{ row }">
              <span v-if="row.consumeType === 'times'" class="times">{{ row.consumeTimes || 1 }}次</span>
              <span v-else class="amount">¥{{ row.amount || 0 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="payMethod" label="支付方式" width="100">
            <template #default="{ row }">
              {{ getPayTypeName(row.payMethod) }}
            </template>
          </el-table-column>
          <el-table-column prop="beauticianName" label="服务人员" width="100" />
          <el-table-column prop="consumeTime" label="消费时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.consumeTime) }}
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="会员卡" name="card">
        <el-table :data="cardList" v-loading="cardLoading">
          <el-table-column prop="cardNo" label="卡号" width="180" />
          <el-table-column prop="cardType" label="类型" width="100">
            <template #default="{ row }">
              <el-tag :type="row.cardType === '次卡' ? 'warning' : 'success'" size="small">
                {{ row.cardType }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="projectName" label="关联项目" width="150" />
          <el-table-column label="剩余" width="120">
            <template #default="{ row }">
              <span v-if="row.cardType === '次卡'">{{ row.remainCount }}次</span>
              <span v-else>¥{{ row.remainAmount }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="validEndDate" label="有效期" width="120">
            <template #default="{ row }">
              {{ row.validEndDate || '永久' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '正常' : '已过期' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      
      <el-tab-pane label="预约记录" name="appointment">
        <el-table :data="appointmentList" v-loading="appointmentLoading">
          <el-table-column prop="appointmentNo" label="预约单号" width="180" />
          <el-table-column prop="projectName" label="服务项目" width="150" />
          <el-table-column prop="appointmentDate" label="预约日期" width="120" />
          <el-table-column label="预约时间" width="120">
            <template #default="{ row }">
              {{ row.startTime }} - {{ row.endTime }}
            </template>
          </el-table-column>
          <el-table-column prop="beauticianName" label="美容师" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCustomerById } from '@/api/customer'
import { getConsumeByCustomerId } from '@/api/consume'
import { getMemberCardByCustomerId } from '@/api/memberCard'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()

const customerId = route.params.id
const customer = ref({})
const activeTab = ref('consume')

// 列表数据
const consumeList = ref([])
const cardList = ref([])
const appointmentList = ref([])

// 加载状态
const consumeLoading = ref(false)
const cardLoading = ref(false)
const appointmentLoading = ref(false)

// 获取客户信息
async function fetchCustomer() {
  try {
    const res = await getCustomerById(customerId)
    customer.value = res.data || {}
  } catch (e) {
    console.error('获取客户信息失败', e)
  }
}

// 获取消费记录
async function fetchConsumeList() {
  consumeLoading.value = true
  try {
    const res = await getConsumeByCustomerId(customerId)
    consumeList.value = res.data || []
  } catch (e) {
    console.error('获取消费记录失败', e)
  } finally {
    consumeLoading.value = false
  }
}

// 获取会员卡
async function fetchCardList() {
  cardLoading.value = true
  try {
    const res = await getMemberCardByCustomerId(customerId)
    cardList.value = res.data || []
  } catch (e) {
    console.error('获取会员卡失败', e)
  } finally {
    cardLoading.value = false
  }
}

// 格式化
const formatNumber = (num) => num?.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',') || '0'
const formatDate = (date) => date ? dayjs(date).format('YYYY-MM-DD') : '-'
const formatDateTime = (date) => date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'

const getCategoryTagClass = (category) => {
  const classes = { 'VIP客户': 'tag-vip', '老客户': 'tag-gold', '新客户': 'tag-silver' }
  return classes[category] || ''
}

const getMemberTagClass = (level) => {
  const classes = { '钻石会员': 'tag-diamond', '金卡会员': 'tag-gold', '银卡会员': 'tag-silver' }
  return classes[level] || ''
}

const getStatusType = (status) => {
  const types = { '待确认': 'warning', '已确认': 'primary', '已完成': 'success', '已取消': 'danger' }
  return types[status] || ''
}

const getConsumeTypeName = (type) => {
  const names = { cash: '现金消费', times: '次卡消费', stored: '储值消费' }
  return names[type] || type
}

const getConsumeTypeTag = (type) => {
  const tags = { cash: 'primary', times: 'success', stored: 'warning' }
  return tags[type] || ''
}

const getPayTypeName = (type) => {
  const names = { cash: '现金', wechat: '微信', alipay: '支付宝', card: '银行卡', stored: '储值卡', times: '次卡' }
  return names[type] || '-'
}

onMounted(() => {
  fetchCustomer()
  fetchConsumeList()
  fetchCardList()
})
</script>

<style lang="scss" scoped>
.customer-detail-page {
  .page-header {
    margin-bottom: 16px;
  }
  
  .info-card {
    background: #fff;
    border-radius: 12px;
    padding: 24px;
    margin-bottom: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  }
  
  .customer-header {
    display: flex;
    align-items: center;
    gap: 24px;
    margin-bottom: 24px;
    
    .avatar {
      background-color: #F86C9A;
      color: #fff;
      font-size: 32px;
    }
    
    .customer-info {
      flex: 1;
      
      h2 {
        font-size: 24px;
        font-weight: 600;
        margin: 0 0 8px;
        color: #333;
      }
      
      .info-row {
        display: flex;
        gap: 8px;
      }
    }
    
    .customer-stats {
      display: flex;
      gap: 40px;
      
      .stat-item {
        text-align: center;
        
        .value {
          font-size: 24px;
          font-weight: 600;
          color: #F86C9A;
        }
        
        .label {
          font-size: 12px;
          color: #999;
          margin-top: 4px;
        }
      }
    }
  }
  
  .info-desc {
    :deep(.el-descriptions__label) {
      width: 100px;
    }
  }
  
  .detail-tabs {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  }
  
  .amount {
    color: #F86C9A;
    font-weight: 500;
  }
  
  .times {
    color: #52c41a;
    font-weight: 500;
  }
}

.tag-vip { background-color: rgba(248, 108, 154, 0.1); border-color: rgba(248, 108, 154, 0.4); color: #F86C9A; }
.tag-diamond { background-color: rgba(255, 193, 7, 0.1); border-color: rgba(255, 193, 7, 0.4); color: #FFC107; }
.tag-gold { background-color: rgba(255, 152, 0, 0.1); border-color: rgba(255, 152, 0, 0.4); color: #FF9800; }
.tag-silver { background-color: rgba(158, 158, 158, 0.1); border-color: rgba(158, 158, 158, 0.4); color: #9E9E9E; }
</style>
