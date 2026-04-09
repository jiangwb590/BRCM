-- ========================================
-- BCRM 美容院客户关系管理系统
-- 数据库初始化脚本
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS bcrm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE bcrm;

-- ========================================
-- 系统表（9张）
-- ========================================

-- ----------------------------
-- 1. 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    dept_id BIGINT DEFAULT NULL COMMENT '部门ID',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ----------------------------
-- 2. 角色表
-- ----------------------------
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ----------------------------
-- 3. 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ----------------------------
-- 4. 菜单表
-- ----------------------------
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_code VARCHAR(50) DEFAULT NULL COMMENT '菜单编码',
    menu_type TINYINT DEFAULT 1 COMMENT '类型：1-目录 2-菜单 3-按钮',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    perms VARCHAR(100) DEFAULT NULL COMMENT '权限标识',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    visible TINYINT DEFAULT 1 COMMENT '是否可见：0-隐藏 1-显示',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- ----------------------------
-- 5. 角色菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu (role_id, menu_id),
    KEY idx_role_id (role_id),
    KEY idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ----------------------------
-- 6. 数据字典表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    dict_name VARCHAR(50) NOT NULL COMMENT '字典名称',
    dict_code VARCHAR(50) NOT NULL COMMENT '字典编码',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dict_code (dict_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- ----------------------------
-- 7. 字典项表
-- ----------------------------
DROP TABLE IF EXISTS sys_dict_item;
CREATE TABLE sys_dict_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典项ID',
    dict_id BIGINT NOT NULL COMMENT '字典ID',
    item_label VARCHAR(50) NOT NULL COMMENT '字典项标签',
    item_value VARCHAR(50) NOT NULL COMMENT '字典项值',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_dict_id (dict_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项表';

-- ----------------------------
-- 8. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_oper_log;
CREATE TABLE sys_oper_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    title VARCHAR(100) DEFAULT NULL COMMENT '操作标题',
    business_type TINYINT DEFAULT 0 COMMENT '业务类型：0-其他 1-新增 2-修改 3-删除',
    method VARCHAR(200) DEFAULT NULL COMMENT '方法名称',
    request_method VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
    oper_name VARCHAR(50) DEFAULT NULL COMMENT '操作人员',
    oper_url VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    oper_ip VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    oper_location VARCHAR(100) DEFAULT NULL COMMENT '操作地点',
    oper_param TEXT DEFAULT NULL COMMENT '请求参数',
    json_result TEXT DEFAULT NULL COMMENT '返回结果',
    status TINYINT DEFAULT 1 COMMENT '状态：0-失败 1-成功',
    error_msg TEXT DEFAULT NULL COMMENT '错误消息',
    oper_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    cost_time BIGINT DEFAULT 0 COMMENT '耗时(毫秒)',
    PRIMARY KEY (id),
    KEY idx_oper_time (oper_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ----------------------------
-- 9. 登录日志表
-- ----------------------------
DROP TABLE IF EXISTS sys_login_log;
CREATE TABLE sys_login_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    username VARCHAR(50) DEFAULT NULL COMMENT '用户名',
    ipaddr VARCHAR(50) DEFAULT NULL COMMENT '登录IP',
    login_location VARCHAR(100) DEFAULT NULL COMMENT '登录地点',
    browser VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
    os VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
    status TINYINT DEFAULT 1 COMMENT '状态：0-失败 1-成功',
    msg VARCHAR(255) DEFAULT NULL COMMENT '提示消息',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (id),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- ========================================
-- 业务表（18张）
-- ========================================

-- ----------------------------
-- 10. 客户表
-- ----------------------------
DROP TABLE IF EXISTS customer;
CREATE TABLE customer (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '客户ID',
    name VARCHAR(50) NOT NULL COMMENT '客户姓名',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    gender TINYINT DEFAULT 2 COMMENT '性别：1-男 2-女',
    birthday DATE DEFAULT NULL COMMENT '生日',
    age INT DEFAULT NULL COMMENT '年龄',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像',
    source VARCHAR(20) DEFAULT NULL COMMENT '来源：douyin-抖音 meituan-美团 xiaohongshu-小红书 wechat-微信 friend-朋友介绍 natural-自然进店 other-其他',
    referee_id BIGINT DEFAULT NULL COMMENT '介绍人ID',
    referee_name VARCHAR(50) DEFAULT NULL COMMENT '介绍人姓名',
    customer_type VARCHAR(20) DEFAULT 'potential' COMMENT '客户类型：potential-潜在 new-新客户 old-老客户 vip-VIP sleep-沉睡',
    member_level_id BIGINT DEFAULT NULL COMMENT '会员等级ID',
    member_level_name VARCHAR(50) DEFAULT NULL COMMENT '会员等级名称',
    total_consume DECIMAL(12,2) DEFAULT 0.00 COMMENT '累计消费金额',
    total_times INT DEFAULT 0 COMMENT '累计消费次数',
    last_consume_time DATETIME DEFAULT NULL COMMENT '最后消费时间',
    balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '储值余额',
    points INT DEFAULT 0 COMMENT '积分',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_phone (phone),
    KEY idx_customer_type (customer_type),
    KEY idx_member_level (member_level_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户表';

-- ----------------------------
-- 11. 标签表
-- ----------------------------
DROP TABLE IF EXISTS tag;
CREATE TABLE tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    tag_color VARCHAR(20) DEFAULT NULL COMMENT '标签颜色',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- ----------------------------
-- 12. 客户标签关联表
-- ----------------------------
DROP TABLE IF EXISTS customer_tag;
CREATE TABLE customer_tag (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_customer_tag (customer_id, tag_id),
    KEY idx_customer_id (customer_id),
    KEY idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户标签关联表';

-- ----------------------------
-- 13. 预约表
-- ----------------------------
DROP TABLE IF EXISTS appointment;
CREATE TABLE appointment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    appointment_no VARCHAR(30) NOT NULL COMMENT '预约单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    customer_phone VARCHAR(20) DEFAULT NULL COMMENT '客户电话',
    service_id BIGINT NOT NULL COMMENT '服务项目ID',
    service_name VARCHAR(100) DEFAULT NULL COMMENT '服务项目名称',
    staff_id BIGINT DEFAULT NULL COMMENT '服务人员ID',
    staff_name VARCHAR(50) DEFAULT NULL COMMENT '服务人员姓名',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    appointment_time TIME NOT NULL COMMENT '预约时间',
    duration INT DEFAULT 60 COMMENT '预计时长(分钟)',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待确认 1-已确认 2-服务中 3-已完成 4-已取消',
    cancel_reason VARCHAR(200) DEFAULT NULL COMMENT '取消原因',
    source VARCHAR(20) DEFAULT 'manual' COMMENT '来源：manual-手动 wechat-微信 phone-电话',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_appointment_no (appointment_no),
    KEY idx_customer_id (customer_id),
    KEY idx_staff_id (staff_id),
    KEY idx_appointment_date (appointment_date),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ----------------------------
-- 14. 服务分类表
-- ----------------------------
DROP TABLE IF EXISTS service_category;
CREATE TABLE service_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务分类表';

-- ----------------------------
-- 15. 服务项目表
-- ----------------------------
DROP TABLE IF EXISTS service_item;
CREATE TABLE service_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    service_code VARCHAR(30) DEFAULT NULL COMMENT '项目编码',
    service_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    category_name VARCHAR(50) DEFAULT NULL COMMENT '分类名称',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    duration INT DEFAULT 60 COMMENT '时长(分钟)',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    image VARCHAR(255) DEFAULT NULL COMMENT '图片',
    service_times INT DEFAULT 0 COMMENT '累计服务次数',
    is_package TINYINT DEFAULT 0 COMMENT '是否套餐：0-否 1-是',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热门：0-否 1-是',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推荐：0-否 1-是',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目表';

-- ----------------------------
-- 16. 项目套餐表
-- ----------------------------
DROP TABLE IF EXISTS service_package;
CREATE TABLE service_package (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    package_name VARCHAR(100) NOT NULL COMMENT '套餐名称',
    package_code VARCHAR(30) DEFAULT NULL COMMENT '套餐编码',
    price DECIMAL(10,2) NOT NULL COMMENT '套餐价格',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    image VARCHAR(255) DEFAULT NULL COMMENT '图片',
    valid_days INT DEFAULT 365 COMMENT '有效天数',
    service_times INT DEFAULT 0 COMMENT '累计服务次数',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目套餐表';

-- ----------------------------
-- 17. 套餐项目关联表
-- ----------------------------
DROP TABLE IF EXISTS package_item;
CREATE TABLE package_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    package_id BIGINT NOT NULL COMMENT '套餐ID',
    service_id BIGINT NOT NULL COMMENT '项目ID',
    service_name VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
    times INT DEFAULT 1 COMMENT '次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_package_id (package_id),
    KEY idx_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐项目关联表';

-- ----------------------------
-- 18. 客户项目购买表（次卡/储值卡）
-- ----------------------------
DROP TABLE IF EXISTS customer_service;
CREATE TABLE customer_service (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    service_type TINYINT NOT NULL COMMENT '类型：1-次卡 2-储值卡',
    service_id BIGINT DEFAULT NULL COMMENT '服务项目ID（次卡）',
    service_name VARCHAR(100) DEFAULT NULL COMMENT '服务项目名称',
    total_times INT DEFAULT 0 COMMENT '总次数（次卡）',
    remain_times INT DEFAULT 0 COMMENT '剩余次数（次卡）',
    total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额（储值卡）',
    remain_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '剩余金额（储值卡）',
    pay_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '支付金额',
    pay_type VARCHAR(20) DEFAULT NULL COMMENT '支付方式：cash-现金 wechat-微信 alipay-支付宝 card-银行卡',
    start_date DATE DEFAULT NULL COMMENT '生效日期',
    end_date DATE DEFAULT NULL COMMENT '到期日期',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已过期 1-正常 2-已用完',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_customer_id (customer_id),
    KEY idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户项目购买表';

-- ----------------------------
-- 19. 消费记录表
-- ----------------------------
DROP TABLE IF EXISTS consumption_record;
CREATE TABLE consumption_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    consume_no VARCHAR(30) NOT NULL COMMENT '消费单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    service_id BIGINT DEFAULT NULL COMMENT '服务项目ID',
    service_name VARCHAR(100) DEFAULT NULL COMMENT '服务项目名称',
    consume_type TINYINT DEFAULT 1 COMMENT '消费类型：1-服务消费 2-开卡消费 3-充值',
    consume_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '消费金额',
    pay_type VARCHAR(20) DEFAULT NULL COMMENT '支付方式：cash-现金 wechat-微信 alipay-支付宝 card-银行卡 stored-储值卡 times-次卡',
    customer_service_id BIGINT DEFAULT NULL COMMENT '客户服务ID（次卡/储值卡）',
    staff_id BIGINT DEFAULT NULL COMMENT '服务人员ID',
    staff_name VARCHAR(50) DEFAULT NULL COMMENT '服务人员姓名',
    consume_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已退款 1-正常',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_consume_no (consume_no),
    KEY idx_customer_id (customer_id),
    KEY idx_consume_time (consume_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费记录表';

-- ----------------------------
-- 20. 会员等级表
-- ----------------------------
DROP TABLE IF EXISTS member_level;
CREATE TABLE member_level (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '等级ID',
    level_name VARCHAR(50) NOT NULL COMMENT '等级名称',
    level_code VARCHAR(30) NOT NULL COMMENT '等级编码',
    min_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '最低消费金额',
    max_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '最高消费金额',
    discount DECIMAL(3,2) DEFAULT 1.00 COMMENT '折扣（如0.95表示95折）',
    upgrade_points INT DEFAULT 0 COMMENT '升级所需积分',
    level_icon VARCHAR(255) DEFAULT NULL COMMENT '等级图标',
    level_color VARCHAR(20) DEFAULT NULL COMMENT '等级颜色',
    benefits VARCHAR(500) DEFAULT NULL COMMENT '会员权益',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_level_code (level_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- ----------------------------
-- 21. 产品分类表
-- ----------------------------
DROP TABLE IF EXISTS product_category;
CREATE TABLE product_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品分类表';

-- ----------------------------
-- 22. 产品表
-- ----------------------------
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品ID',
    product_code VARCHAR(30) DEFAULT NULL COMMENT '产品编码',
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    category_name VARCHAR(50) DEFAULT NULL COMMENT '分类名称',
    specification VARCHAR(100) DEFAULT NULL COMMENT '规格',
    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
    purchase_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '进货价',
    sale_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '售价',
    stock INT DEFAULT 0 COMMENT '库存数量',
    min_stock INT DEFAULT 10 COMMENT '最低库存预警',
    image VARCHAR(255) DEFAULT NULL COMMENT '图片',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_stock (stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- ----------------------------
-- 23. 入库记录表
-- ----------------------------
DROP TABLE IF EXISTS stock_in_record;
CREATE TABLE stock_in_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    stock_in_no VARCHAR(30) NOT NULL COMMENT '入库单号',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_name VARCHAR(100) DEFAULT NULL COMMENT '产品名称',
    product_code VARCHAR(30) DEFAULT NULL COMMENT '产品编码',
    quantity INT NOT NULL COMMENT '入库数量',
    unit_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '单价',
    total_price DECIMAL(12,2) DEFAULT 0.00 COMMENT '总价',
    supplier VARCHAR(100) DEFAULT NULL COMMENT '供应商',
    stock_in_type TINYINT DEFAULT 1 COMMENT '入库类型：1-采购入库 2-退货入库 3-其他',
    stock_in_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-作废 1-正常',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_in_no (stock_in_no),
    KEY idx_product_id (product_id),
    KEY idx_stock_in_time (stock_in_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入库记录表';

-- ----------------------------
-- 24. 出库记录表
-- ----------------------------
DROP TABLE IF EXISTS stock_out_record;
CREATE TABLE stock_out_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    stock_out_no VARCHAR(30) NOT NULL COMMENT '出库单号',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_name VARCHAR(100) DEFAULT NULL COMMENT '产品名称',
    product_code VARCHAR(30) DEFAULT NULL COMMENT '产品编码',
    quantity INT NOT NULL COMMENT '出库数量',
    unit_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '单价',
    total_price DECIMAL(12,2) DEFAULT 0.00 COMMENT '总价',
    stock_out_type TINYINT DEFAULT 1 COMMENT '出库类型：1-服务消耗 2-报废 3-其他',
    related_id BIGINT DEFAULT NULL COMMENT '关联ID（如消费记录ID）',
    stock_out_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '出库时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-作废 1-正常',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_stock_out_no (stock_out_no),
    KEY idx_product_id (product_id),
    KEY idx_stock_out_time (stock_out_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出库记录表';

-- ----------------------------
-- 25. 提醒规则表
-- ----------------------------
DROP TABLE IF EXISTS reminder_rule;
CREATE TABLE reminder_rule (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    rule_name VARCHAR(50) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(30) NOT NULL COMMENT '规则类型：birthday-生日 consume-消费 sleep-沉睡 expire-到期',
    rule_desc VARCHAR(200) DEFAULT NULL COMMENT '规则描述',
    trigger_condition VARCHAR(500) DEFAULT NULL COMMENT '触发条件JSON',
    template_content VARCHAR(500) DEFAULT NULL COMMENT '模板内容',
    is_auto TINYINT DEFAULT 0 COMMENT '是否自动：0-手动 1-自动',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒规则表';

-- ----------------------------
-- 26. 提醒记录表
-- ----------------------------
DROP TABLE IF EXISTS reminder;
CREATE TABLE reminder (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    reminder_no VARCHAR(30) DEFAULT NULL COMMENT '提醒编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    customer_phone VARCHAR(20) DEFAULT NULL COMMENT '客户电话',
    reminder_type VARCHAR(30) NOT NULL COMMENT '提醒类型：birthday-生日 consume-消费 sleep-沉睡 expire-到期',
    reminder_title VARCHAR(100) DEFAULT NULL COMMENT '提醒标题',
    reminder_content VARCHAR(500) DEFAULT NULL COMMENT '提醒内容',
    trigger_time DATETIME DEFAULT NULL COMMENT '触发时间',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    handle_by BIGINT DEFAULT NULL COMMENT '处理人',
    handle_result VARCHAR(200) DEFAULT NULL COMMENT '处理结果',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待处理 1-已处理 2-已忽略',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_customer_id (customer_id),
    KEY idx_reminder_type (reminder_type),
    KEY idx_status (status),
    KEY idx_trigger_time (trigger_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提醒记录表';

-- ----------------------------
-- 27. 消息表
-- ----------------------------
DROP TABLE IF EXISTS message;
CREATE TABLE message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    message_type VARCHAR(30) NOT NULL COMMENT '消息类型：system-系统 appointment-预约 stock-库存 member-会员',
    title VARCHAR(100) NOT NULL COMMENT '消息标题',
    content VARCHAR(500) DEFAULT NULL COMMENT '消息内容',
    receiver_id BIGINT DEFAULT NULL COMMENT '接收人ID（NULL表示全部）',
    sender_id BIGINT DEFAULT NULL COMMENT '发送人ID',
    related_id BIGINT DEFAULT NULL COMMENT '关联业务ID',
    related_type VARCHAR(30) DEFAULT NULL COMMENT '关联业务类型',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-撤回 1-正常',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_receiver_id (receiver_id),
    KEY idx_message_type (message_type),
    KEY idx_is_read (is_read),
    KEY idx_send_time (send_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ========================================
-- 初始化数据
-- ========================================

-- 初始化角色
INSERT INTO sys_role (role_name, role_code, sort, status, remark) VALUES
('超级管理员', 'admin', 1, 1, '超级管理员，拥有所有权限'),
('店长', 'manager', 2, 1, '店长角色'),
('美容师', 'beautician', 3, 1, '美容师角色'),
('前台', 'receptionist', 4, 1, '前台角色');

-- 初始化管理员用户（密码：123456，使用BCrypt加密）
INSERT INTO sys_user (username, password, real_name, phone, gender, status, remark) VALUES
('admin', '$2a$10$F1rx4TnCQm6p1AGnEEFKBOrTgsmrJomc9JUZjq0eRetmUbt.GY0Xe', '超级管理员', '13800138000', 1, 1, '超级管理员');

-- 关联用户角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 初始化菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_code, menu_type, path, component, perms, icon, sort) VALUES
(0, '工作台', 'workbench', 1, '/dashboard', 'dashboard/index', NULL, 'HomeOutlined', 1),
(0, '客户管理', 'customer', 1, '/customer', 'customer/index', NULL, 'TeamOutlined', 2),
(0, '预约管理', 'appointment', 1, '/appointment', 'appointment/index', NULL, 'CalendarOutlined', 3),
(0, '项目管理', 'project', 1, '/project', 'project/index', NULL, 'AppstoreOutlined', 4),
(0, '项目消耗', 'consume', 1, '/consume', 'consume/index', NULL, 'CreditCardOutlined', 5),
(0, '进销存', 'inventory', 1, '/inventory', 'inventory/index', NULL, 'DatabaseOutlined', 6),
(0, '会员管理', 'member', 1, '/member', 'member/index', NULL, 'CrownOutlined', 7),
(0, '客户维系', 'care', 1, '/care', 'care/index', NULL, 'HeartOutlined', 8),
(0, '消息提醒', 'message', 1, '/message', 'message/index', NULL, 'BellOutlined', 9),
(0, '数据分析', 'analysis', 1, '/analysis', 'analysis/index', NULL, 'BarChartOutlined', 10),
(0, '系统管理', 'system', 1, '/system', 'system/index', NULL, 'SettingOutlined', 11);

-- 初始化会员等级
INSERT INTO member_level (level_name, level_code, min_amount, max_amount, discount, benefits, sort) VALUES
('普通会员', 'normal', 0, 999, 0.95, '生日关怀、消费积分', 1),
('银卡会员', 'silver', 1000, 2999, 0.90, '优先预约、专属客服', 2),
('金卡会员', 'gold', 3000, 5999, 0.85, 'VIP包间、免费茶点', 3),
('钻石会员', 'diamond', 6000, 999999, 0.80, '专属美容师、年度礼包', 4);

-- 初始化字典
INSERT INTO sys_dict (dict_name, dict_code, sort) VALUES
('客户来源', 'customer_source', 1),
('客户类型', 'customer_type', 2),
('支付方式', 'pay_type', 3),
('预约状态', 'appointment_status', 4);

-- 初始化字典项
INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort) VALUES
(1, '抖音', 'douyin', 1),
(1, '美团', 'meituan', 2),
(1, '小红书', 'xiaohongshu', 3),
(1, '微信', 'wechat', 4),
(1, '朋友介绍', 'friend', 5),
(1, '自然进店', 'natural', 6),
(1, '其他', 'other', 7);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort) VALUES
(2, '潜在客户', 'potential', 1),
(2, '新客户', 'new', 2),
(2, '老客户', 'old', 3),
(2, 'VIP客户', 'vip', 4),
(2, '沉睡客户', 'sleep', 5);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort) VALUES
(3, '现金', 'cash', 1),
(3, '微信', 'wechat', 2),
(3, '支付宝', 'alipay', 3),
(3, '银行卡', 'card', 4),
(3, '储值卡', 'stored', 5),
(3, '次卡', 'times', 6);

INSERT INTO sys_dict_item (dict_id, item_label, item_value, sort) VALUES
(4, '待确认', '0', 1),
(4, '已确认', '1', 2),
(4, '服务中', '2', 3),
(4, '已完成', '3', 4),
(4, '已取消', '4', 5);

-- 初始化服务分类
INSERT INTO service_category (category_name, sort) VALUES
('面部护理', 1),
('身体护理', 2),
('美甲美睫', 3);

-- 初始化服务项目
INSERT INTO service_item (service_code, service_name, category_id, category_name, price, duration, description, is_hot, is_recommend, sort) VALUES
('FW001', '深层补水护理', 1, '面部护理', 298.00, 60, '深层补水、改善干燥、修复屏障', 1, 1, 1),
('FW002', '全身精油按摩', 2, '身体护理', 398.00, 90, '放松身心、缓解疲劳、改善睡眠', 0, 1, 2),
('FW003', '祛痘修复护理', 1, '面部护理', 358.00, 45, '祛痘淡印、修复受损、控油平衡', 0, 0, 3),
('FW004', '美甲艺术套餐', 3, '美甲美睫', 168.00, 60, '精致美甲、手部护理、款式设计', 0, 0, 4),
('FW005', '美睫嫁接服务', 3, '美甲美睫', 258.00, 90, '自然卷翘、持久定型、日常妆容', 0, 0, 5);

-- 完成提示
SELECT '数据库初始化完成！' AS message;
