import request from '@/utils/request'

// 分页查询套餐
export function getPackagePage(params) {
  return request({ url: '/package/page', method: 'get', params })
}

// 获取上架套餐列表
export function getActivePackages() {
  return request({ url: '/package/active', method: 'get' })
}

// 获取套餐详情
export function getPackageById(id) {
  return request({ url: `/package/${id}`, method: 'get' })
}

// 新增套餐
export function addPackage(data) {
  return request({ url: '/package', method: 'post', data })
}

// 修改套餐
export function updatePackage(data) {
  return request({ url: '/package', method: 'put', data })
}

// 删除套餐
export function deletePackage(id) {
  return request({ url: `/package/${id}`, method: 'delete' })
}

// 购买套餐
export function purchasePackage(data) {
  return request({ url: '/package/purchase', method: 'post', data })
}

// 分页查询购买记录
export function getPurchasePage(params) {
  return request({ url: '/package/purchase/page', method: 'get', params })
}

// 获取客户已购买的套餐（有剩余次数的）
export function getCustomerPurchases(customerId) {
  return request({ url: `/package/customer/${customerId}/purchases`, method: 'get' })
}