import request from '@/utils/request'

// 分页查询充值记录
export function getRechargePage(params) {
  return request({ url: '/recharge/page', method: 'get', params })
}

// 充值
export function recharge(data) {
  return request({ url: '/recharge', method: 'post', data })
}
