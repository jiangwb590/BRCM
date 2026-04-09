import request from '@/utils/request'

// 获取客户来源趋势
export function getCustomerSourceTrend(days = 7, channel = '') {
  const params = { days }
  if (channel) {
    params.channel = channel
  }
  return request({
    url: '/dashboard/customer-source-trend',
    method: 'get',
    params
  })
}
