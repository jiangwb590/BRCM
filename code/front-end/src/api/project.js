import request from '@/utils/request'

// 分页查询项目
export function getProjectPage(params) {
  return request({
    url: '/project/page',
    method: 'get',
    params
  })
}

// 获取所有上架项目
export function getActiveProjects() {
  return request({
    url: '/project/active',
    method: 'get'
  })
}

// 获取项目详情
export function getProjectById(id) {
  return request({
    url: `/project/${id}`,
    method: 'get'
  })
}

// 新增项目
export function addProject(data) {
  return request({
    url: '/project',
    method: 'post',
    data
  })
}

// 修改项目
export function updateProject(data) {
  return request({
    url: '/project',
    method: 'put',
    data
  })
}

// 删除项目
export function deleteProject(id) {
  return request({
    url: `/project/${id}`,
    method: 'delete'
  })
}

// 修改项目状态
export function updateProjectStatus(id, status) {
  return request({
    url: `/project/status/${id}`,
    method: 'put',
    params: { status }
  })
}
