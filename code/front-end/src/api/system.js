import request from '@/utils/request'

// 分页查询用户
export function getUserPage(params) {
  return request({ url: '/system/user/page', method: 'get', params })
}

// 获取用户详情
export function getUserById(id) {
  return request({ url: `/system/user/${id}`, method: 'get' })
}

// 新增用户
export function addUser(data) {
  return request({ url: '/system/user', method: 'post', data })
}

// 修改用户
export function updateUser(data) {
  return request({ url: '/system/user', method: 'put', data })
}

// 删除用户
export function deleteUser(id) {
  return request({ url: `/system/user/${id}`, method: 'delete' })
}

// 重置密码
export function resetPassword(id) {
  return request({ url: `/system/user/reset-password/${id}`, method: 'put' })
}

// 获取所有角色
export function getAllRoles() {
  return request({ url: '/system/role/all', method: 'get' })
}

// 分页查询角色
export function getRolePage(params) {
  return request({ url: '/system/role/page', method: 'get', params })
}

// 新增角色
export function addRole(data) {
  return request({ url: '/system/role', method: 'post', data })
}

// 修改角色
export function updateRole(data) {
  return request({ url: '/system/role', method: 'put', data })
}

// 删除角色
export function deleteRole(id) {
  return request({ url: `/system/role/${id}`, method: 'delete' })
}

// 分配角色菜单权限
export function assignRoleMenus(id, menuIds) {
  return request({ url: `/system/role/${id}/menus`, method: 'post', data: menuIds })
}

// 获取角色菜单ID
export function getRoleMenuIds(id) {
  return request({ url: `/system/role/${id}/menus`, method: 'get' })
}

// 获取菜单树
export function getMenuTree() {
  return request({ url: '/system/menu/tree', method: 'get' })
}

// 新增菜单
export function addMenu(data) {
  return request({ url: '/system/menu', method: 'post', data })
}

// 修改菜单
export function updateMenu(data) {
  return request({ url: '/system/menu', method: 'put', data })
}

// 删除菜单
export function deleteMenu(id) {
  return request({ url: `/system/menu/${id}`, method: 'delete' })
}

// 获取字典列表
export function getDictPage(params) {
  return request({ url: '/system/dict/page', method: 'get', params })
}

// 新增字典
export function addDict(data) {
  return request({ url: '/system/dict', method: 'post', data })
}

// 修改字典
export function updateDict(data) {
  return request({ url: '/system/dict', method: 'put', data })
}

// 删除字典
export function deleteDict(id) {
  return request({ url: `/system/dict/${id}`, method: 'delete' })
}

// 获取字典项列表
export function getDictItems(dictId) {
  return request({ url: `/system/dict/item/${dictId}`, method: 'get' })
}

// 新增字典项
export function addDictItem(data) {
  return request({ url: '/system/dict/item', method: 'post', data })
}

// 修改字典项
export function updateDictItem(data) {
  return request({ url: '/system/dict/item', method: 'put', data })
}

// 删除字典项
export function deleteDictItem(id) {
  return request({ url: `/system/dict/item/${id}`, method: 'delete' })
}

// 获取美容师列表
export function getBeauticians() {
  return request({ url: '/system/user/beauticians', method: 'get' })
}

// 根据字典编码获取字典项
export function getDictByCode(dictCode) {
  return request({ url: `/system/dict/code/${dictCode}`, method: 'get' })
}

// 分页查询操作日志
export function getOperLogPage(params) {
  return request({ url: '/system/oper-log/page', method: 'get', params })
}
