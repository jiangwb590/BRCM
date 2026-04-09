import request from '@/utils/request'

// 分页查询客户
export function getCustomerPage(params) {
  return request({
    url: '/customer/page',
    method: 'get',
    params
  })
}

// 获取客户详情
export function getCustomerById(id) {
  return request({
    url: `/customer/${id}`,
    method: 'get'
  })
}

// 新增客户
export function addCustomer(data) {
  return request({
    url: '/customer',
    method: 'post',
    data
  })
}

// 修改客户
export function updateCustomer(data) {
  return request({
    url: '/customer',
    method: 'put',
    data
  })
}

// 删除客户
export function deleteCustomer(id) {
  return request({
    url: `/customer/${id}`,
    method: 'delete'
  })
}

// 批量删除客户
export function batchDeleteCustomer(ids) {
  return request({
    url: '/customer/batch',
    method: 'delete',
    data: ids
  })
}

// 获取客户来源统计
export function getSourceStatistics() {
  return request({
    url: '/customer/statistics/source',
    method: 'get'
  })
}

// 获取客户分类统计
export function getCategoryStatistics() {
  return request({
    url: '/customer/statistics/category',
    method: 'get'
  })
}

// 导出客户数据
export function exportCustomer(params) {
  return request({
    url: '/customer/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
