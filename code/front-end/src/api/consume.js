import request from '@/utils/request'

// 分页查询消费记录
export function getConsumePage(params) {
  return request({
    url: '/consume/page',
    method: 'get',
    params
  })
}

// 获取客户消费记录
export function getConsumeByCustomerId(customerId) {
  return request({
    url: `/consume/customer/${customerId}`,
    method: 'get'
  })
}

// 创建消费记录
export function createConsume(data) {
  return request({
    url: '/consume',
    method: 'post',
    data
  })
}

// 获取营收统计
export function getRevenueStatistics(startDate, endDate) {
  return request({
    url: '/consume/statistics/revenue',
    method: 'get',
    params: { startDate, endDate }
  })
}

// 获取今日营收
export function getTodayRevenue() {
  return request({
    url: '/consume/statistics/today',
    method: 'get'
  })
}
