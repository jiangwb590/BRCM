-- 按钮级别权限数据
-- menu_type: 1-目录, 2-菜单, 3-按钮
-- perms格式: 模块:操作 (如 customer:add, customer:edit, customer:delete)
-- 执行此脚本前，请先确认菜单表中的parent_id

-- 清空现有按钮权限（重新初始化）
DELETE FROM sys_menu WHERE menu_type = 3;

-- =====================
-- 按钮权限数据
-- 注意：parent_id需要根据实际菜单ID调整
-- =====================

-- 客户管理按钮 (假设parent_id=2)
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, perms, sort) VALUES
(2, '新增客户', 'customer:add', 3, 'customer:add', 1),
(2, '编辑客户', 'customer:edit', 3, 'customer:edit', 2),
(2, '删除客户', 'customer:delete', 3, 'customer:delete', 3),
(2, '导出客户', 'customer:export', 3, 'customer:export', 4),
(2, '充值', 'customer:recharge', 3, 'customer:recharge', 5);

-- 预约管理按钮 (假设parent_id=3)
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, perms, sort) VALUES
(3, '新增预约', 'appointment:add', 3, 'appointment:add', 1),
(3, '编辑预约', 'appointment:edit', 3, 'appointment:edit', 2),
(3, '取消预约', 'appointment:cancel', 3, 'appointment:cancel', 3);

-- 项目管理按钮 (假设parent_id=4)
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, perms, sort) VALUES
(4, '新增项目', 'project:add', 3, 'project:add', 1),
(4, '编辑项目', 'project:edit', 3, 'project:edit', 2),
(4, '删除项目', 'project:delete', 3, 'project:delete', 3);

-- 项目消耗按钮 (假设parent_id=5)
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, perms, sort) VALUES
(5, '开单消费', 'consume:add', 3, 'consume:add', 1),
(5, '删除记录', 'consume:delete', 3, 'consume:delete', 2),
(5, '导出', 'consume:export', 3, 'consume:export', 3);

-- 会员管理按钮 (假设parent_id=7)
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, perms, sort) VALUES
(7, '新增储值卡', 'memberCard:add', 3, 'memberCard:add', 1),
(7, '编辑储值卡', 'memberCard:edit', 3, 'memberCard:edit', 2),
(7, '删除储值卡', 'memberCard:delete', 3, 'memberCard:delete', 3);

-- 进销存按钮 (假设parent_id=6)
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, perms, sort) VALUES
(6, '新增产品', 'product:add', 3, 'product:add', 1),
(6, '编辑产品', 'product:edit', 3, 'product:edit', 2),
(6, '删除产品', 'product:delete', 3, 'product:delete', 3),
(6, '入库操作', 'stock:in', 3, 'stock:in', 4),
(6, '出库操作', 'stock:out', 3, 'stock:out', 5);

-- 客户维系按钮 (假设parent_id=8)
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, perms, sort) VALUES
(8, '新增关怀', 'care:add', 3, 'care:add', 1),
(8, '删除关怀', 'care:delete', 3, 'care:delete', 2),
(8, '执行关怀', 'care:execute', 3, 'care:execute', 3);

-- 消息提醒按钮 (假设parent_id=9)
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, perms, sort) VALUES
(9, '发送消息', 'message:send', 3, 'message:send', 1),
(9, '删除消息', 'message:delete', 3, 'message:delete', 2);

-- 系统管理按钮 (假设parent_id=11)
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, perms, sort) VALUES
(11, '新增用户', 'user:add', 3, 'user:add', 1),
(11, '编辑用户', 'user:edit', 3, 'user:edit', 2),
(11, '删除用户', 'user:delete', 3, 'user:delete', 3),
(11, '新增角色', 'role:add', 3, 'role:add', 4),
(11, '编辑角色', 'role:edit', 3, 'role:edit', 5),
(11, '删除角色', 'role:delete', 3, 'role:delete', 6),
(11, '菜单管理', 'menu:manage', 3, 'menu:manage', 7);

-- 查看插入结果
SELECT id, parent_id, menu_name, menu_type, perms FROM sys_menu WHERE menu_type = 3 ORDER BY parent_id, sort;