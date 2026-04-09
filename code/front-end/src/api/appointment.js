import request from '@/utils/request'

// 分页查询预约
export function getAppointmentPage(params) {
  return request({
    url: '/appointment/page',
    method: 'get',
    params
  })
}

// 获取指定日期的预约
export function getAppointmentByDate(date) {
  return request({
    url: `/appointment/date/${date}`,
    method: 'get'
  })
}

// 获取预约详情
export function getAppointmentById(id) {
  return request({
    url: `/appointment/${id}`,
    method: 'get'
  })
}

// 新增预约
export function addAppointment(data) {
  return request({
    url: '/appointment',
    method: 'post',
    data
  })
}

// 修改预约
export function updateAppointment(data) {
  return request({
    url: '/appointment',
    method: 'put',
    data
  })
}

// 取消预约
export function cancelAppointment(id, reason) {
  return request({
    url: `/appointment/cancel/${id}`,
    method: 'post',
    params: { reason }
  })
}

// 确认预约
export function confirmAppointment(id) {
  return request({
    url: `/appointment/confirm/${id}`,
    method: 'post'
  })
}

// 开始服务
export function startService(id) {
  return request({
    url: `/appointment/start/${id}`,
    method: 'post'
  })
}

// 完成服务
export function completeService(id) {
  return request({
    url: `/appointment/complete/${id}`,
    method: 'post'
  })
}

// 获取今日预约统计
export function getTodayStatistics() {
  return request({
    url: '/appointment/statistics/today',
    method: 'get'
  })
}
