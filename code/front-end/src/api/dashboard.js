import request from '@/utils/request'

// 获取概览数据
export function getOverview() {
  return request({
    url: '/dashboard/overview',
    method: 'get'
  })
}

// 获取营收趋势
export function getRevenueTrend(days = 7, startDate, endDate) {
  const params = {}
  if (startDate && endDate) {
    params.startDate = startDate
    params.endDate = endDate
  } else {
    params.days = days
  }
  return request({
    url: '/dashboard/revenue-trend',
    method: 'get',
    params
  })
}

// 获取客户来源统计
export function getCustomerSource(startDate, endDate) {
  const params = {}
  if (startDate && endDate) {
    params.startDate = startDate
    params.endDate = endDate
  }
  return request({
    url: '/dashboard/customer-source',
    method: 'get',
    params
  })
}

// 获取客户分类统计
export function getCustomerCategory() {
  return request({
    url: '/dashboard/customer-category',
    method: 'get'
  })
}

// 获取员工业绩排名
export function getEmployeePerformance(limit = 10, startDate, endDate) {
  const params = { limit }
  if (startDate && endDate) {
    params.startDate = startDate
    params.endDate = endDate
  }
  return request({
    url: '/dashboard/employee-performance',
    method: 'get',
    params
  })
}

// 获取项目销量排名
export function getProjectSales(limit = 10, startDate, endDate) {
  const params = { limit }
  if (startDate && endDate) {
    params.startDate = startDate
    params.endDate = endDate
  }
  return request({
    url: '/dashboard/project-sales',
    method: 'get',
    params
  })
}

// 获取产品/耗材消耗比例趋势
export function getConsumptionRatioTrend(days = 7) {
  return request({
    url: '/dashboard/consumption-ratio-trend',
    method: 'get',
    params: { days }
  })
}
