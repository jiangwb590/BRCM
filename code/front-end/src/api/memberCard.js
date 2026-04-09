import request from '@/utils/request'

// 分页查询会员卡
export function getMemberCardPage(params) {
  return request({
    url: '/member-card/page',
    method: 'get',
    params
  })
}

// 获取客户会员卡
export function getMemberCardByCustomerId(customerId) {
  return request({
    url: `/member-card/customer/${customerId}`,
    method: 'get'
  })
}

// 获取会员卡详情
export function getMemberCardById(id) {
  return request({
    url: `/member-card/${id}`,
    method: 'get'
  })
}

// 开卡
export function createMemberCard(data) {
  return request({
    url: '/member-card',
    method: 'post',
    data
  })
}

// 充值
export function rechargeMemberCard(id, amount) {
  return request({
    url: `/member-card/recharge/${id}`,
    method: 'post',
    params: { amount }
  })
}

// 作废会员卡
export function disableMemberCard(id) {
  return request({
    url: `/member-card/disable/${id}`,
    method: 'post'
  })
}
