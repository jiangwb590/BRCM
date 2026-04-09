import request from '@/utils/request'

// 分页查询产品
export function getProductPage(params) {
  return request({
    url: '/product/page',
    method: 'get',
    params
  })
}

// 获取所有产品
export function getAllProducts() {
  return request({
    url: '/product/all',
    method: 'get'
  })
}

// 获取产品详情
export function getProductById(id) {
  return request({
    url: `/product/${id}`,
    method: 'get'
  })
}

// 新增产品
export function addProduct(data) {
  return request({
    url: '/product',
    method: 'post',
    data
  })
}

// 修改产品
export function updateProduct(data) {
  return request({
    url: '/product',
    method: 'put',
    data
  })
}

// 删除产品
export function deleteProduct(id) {
  return request({
    url: `/product/${id}`,
    method: 'delete'
  })
}

// 入库
export function stockIn(data) {
  return request({
    url: '/product/stock-in',
    method: 'post',
    data
  })
}

// 出库
export function stockOut(data) {
  return request({
    url: '/product/stock-out',
    method: 'post',
    data
  })
}

// 获取入库记录
export function getStockInPage(params) {
  return request({
    url: '/stock-in/page',
    method: 'get',
    params
  })
}

// 新增入库记录
export function addStockIn(data) {
  return request({
    url: '/stock-in',
    method: 'post',
    data
  })
}

// 获取出库记录
export function getStockOutPage(params) {
  return request({
    url: '/stock-out/page',
    method: 'get',
    params
  })
}

// 新增出库记录
export function addStockOut(data) {
  return request({
    url: '/stock-out',
    method: 'post',
    data
  })
}
