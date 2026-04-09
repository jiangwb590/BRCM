import request from '@/utils/request'

// 分页查询消息
export function getMessagePage(params) {
  return request({
    url: '/message/page',
    method: 'get',
    params
  })
}

// 获取未读消息
export function getUnreadMessages() {
  return request({
    url: '/message/unread',
    method: 'get'
  })
}

// 获取未读消息数量
export function getUnreadCount() {
  return request({
    url: '/message/unread/count',
    method: 'get'
  })
}

// 标记消息已读
export function markAsRead(id) {
  return request({
    url: `/message/read/${id}`,
    method: 'post'
  })
}

// 标记所有消息已读
export function markAllAsRead() {
  return request({
    url: '/message/read/all',
    method: 'post'
  })
}
