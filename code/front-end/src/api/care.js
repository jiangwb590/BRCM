import request from '@/utils/request'

// 获取今日待办
export function getTodayTasks() {
  return request({
    url: '/customer-care/today',
    method: 'get'
  })
}

// 分页查询关怀记录
export function getCarePage(params) {
  return request({
    url: '/customer-care/page',
    method: 'get',
    params
  })
}

// 创建关怀计划
export function createCare(data) {
  return request({
    url: '/customer-care',
    method: 'post',
    data
  })
}

// 执行关怀
export function executeCare(id, remark) {
  return request({
    url: `/customer-care/execute/${id}`,
    method: 'post',
    params: { remark }
  })
}

// 取消关怀
export function cancelCare(id) {
  return request({
    url: `/customer-care/cancel/${id}`,
    method: 'post'
  })
}
