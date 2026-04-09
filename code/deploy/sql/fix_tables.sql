
-- ========================================
-- 1. 修复 appointment 表
-- ========================================
-- 先删除旧表，重新创建
DROP TABLE IF EXISTS appointment;
CREATE TABLE appointment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    appointment_no VARCHAR(30) NOT NULL COMMENT '预约单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    customer_phone VARCHAR(20) DEFAULT NULL COMMENT '客户电话',
    project_id BIGINT DEFAULT NULL COMMENT '项目ID',
    project_name VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME DEFAULT NULL COMMENT '开始时间',
    end_time TIME DEFAULT NULL COMMENT '结束时间',
    beautician_id BIGINT DEFAULT NULL COMMENT '美容师ID',
    beautician_name VARCHAR(50) DEFAULT NULL COMMENT '美容师姓名',
    status VARCHAR(20) DEFAULT '0' COMMENT '状态：0-待确认 1-已确认 2-服务中 3-已完成 4-已取消',
    source VARCHAR(20) DEFAULT 'manual' COMMENT '来源：manual-手动 wechat-微信 phone-电话',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    cancel_reason VARCHAR(200) DEFAULT NULL COMMENT '取消原因',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_appointment_no (appointment_no),
    KEY idx_customer_id (customer_id),
    KEY idx_beautician_id (beautician_id),
    KEY idx_appointment_date (appointment_date),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- ========================================
-- 2. 创建 consume_record 表（替换 consumption_record）
-- ========================================
DROP TABLE IF EXISTS consume_record;
CREATE TABLE consume_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    consume_no VARCHAR(30) NOT NULL COMMENT '消费单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    project_id BIGINT DEFAULT NULL COMMENT '项目ID',
    project_name VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
    consume_type VARCHAR(20) DEFAULT NULL COMMENT '消费类型：cash-现金 times-次卡 stored-储值',
    amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '消费金额',
    pay_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '实付金额',
    discount_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '折扣金额',
    beautician_id BIGINT DEFAULT NULL COMMENT '美容师ID',
    beautician_name VARCHAR(50) DEFAULT NULL COMMENT '美容师姓名',
    card_id BIGINT DEFAULT NULL COMMENT '次卡/储值卡ID',
    appointment_id BIGINT DEFAULT NULL COMMENT '关联预约ID',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '支付方式：cash-现金 wechat-微信 alipay-支付宝 card-银行卡',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    consume_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已退款 1-正常',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_consume_no (consume_no),
    KEY idx_customer_id (customer_id),
    KEY idx_beautician_id (beautician_id),
    KEY idx_consume_time (consume_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费记录表';

-- ========================================
-- 3. 创建 customer_care 表
-- ========================================
DROP TABLE IF EXISTS customer_care;
CREATE TABLE customer_care (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关怀ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    care_type VARCHAR(30) NOT NULL COMMENT '关怀类型：birthday-生日关怀 consume-消费关怀 sleep-沉睡唤醒 visit-回访提醒',
    content VARCHAR(500) DEFAULT NULL COMMENT '关怀内容',
    plan_date DATE DEFAULT NULL COMMENT '计划日期',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待执行 done-已执行 cancelled-已取消',
    execute_time DATETIME DEFAULT NULL COMMENT '执行时间',
    execute_by BIGINT DEFAULT NULL COMMENT '执行人',
    execute_by_name VARCHAR(50) DEFAULT NULL COMMENT '执行人姓名',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_customer_id (customer_id),
    KEY idx_care_type (care_type),
    KEY idx_plan_date (plan_date),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户关怀表';

-- ========================================
-- 4. 修复 customer 表
-- ========================================
-- 添加缺失字段
ALTER TABLE customer 
    ADD COLUMN wechat VARCHAR(50) DEFAULT NULL COMMENT '微信号' AFTER phone,
    ADD COLUMN advisor_id BIGINT DEFAULT NULL COMMENT '顾问ID（专属美容师）' AFTER member_level_id,
    ADD COLUMN tags VARCHAR(500) DEFAULT NULL COMMENT '标签（多个标签用逗号分隔）' AFTER points;

-- 重命名字段以匹配实体
ALTER TABLE customer 
    CHANGE COLUMN customer_type category VARCHAR(20) DEFAULT 'potential' COMMENT '客户分类：potential-潜在 new-新客户 old-老客户 vip-VIP sleep-沉睡',
    CHANGE COLUMN total_consume total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '累计消费金额',
    CHANGE COLUMN total_times consume_count INT DEFAULT 0 COMMENT '消费次数';

-- 添加索引
ALTER TABLE customer ADD INDEX idx_advisor_id (advisor_id);

-- ========================================
-- 5. 创建 member_card 表（如果不存在）
-- ========================================
DROP TABLE IF EXISTS member_card;
CREATE TABLE member_card (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '卡片ID',
    card_no VARCHAR(30) NOT NULL COMMENT '卡号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    card_type VARCHAR(20) NOT NULL COMMENT '卡片类型：次卡/储值卡',
    project_id BIGINT DEFAULT NULL COMMENT '关联项目ID（次卡）',
    project_name VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
    total_count INT DEFAULT 0 COMMENT '总次数（次卡）',
    remain_count INT DEFAULT 0 COMMENT '剩余次数（次卡）',
    total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '储值总金额（储值卡）',
    remain_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '剩余金额（储值卡）',
    purchase_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '购卡金额',
    valid_start_date DATE DEFAULT NULL COMMENT '生效日期',
    valid_end_date DATE DEFAULT NULL COMMENT '到期日期',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用 2-已过期',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_card_no (card_no),
    KEY idx_customer_id (customer_id),
    KEY idx_card_type (card_type),
    KEY idx_valid_end_date (valid_end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员卡表';

-- ========================================
-- 6. 修复 product 表字段
-- ========================================
ALTER TABLE product 
    CHANGE COLUMN product_name name VARCHAR(100) NOT NULL COMMENT '产品名称',
    CHANGE COLUMN min_stock stock_warning INT DEFAULT 10 COMMENT '库存预警值';

-- ========================================
-- 7. 创建 service_project 表（替换 service_item）
-- ========================================
DROP TABLE IF EXISTS service_project;
CREATE TABLE service_project (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    project_code VARCHAR(30) DEFAULT NULL COMMENT '项目编号',
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    category_name VARCHAR(50) DEFAULT NULL COMMENT '分类名称',
    image VARCHAR(255) DEFAULT NULL COMMENT '项目图片',
    description VARCHAR(500) DEFAULT NULL COMMENT '项目描述',
    duration INT DEFAULT 60 COMMENT '服务时长(分钟)',
    price DECIMAL(10,2) NOT NULL COMMENT '项目价格',
    member_price DECIMAL(10,2) DEFAULT NULL COMMENT '会员价',
    cost_price DECIMAL(10,2) DEFAULT NULL COMMENT '成本价',
    is_package TINYINT DEFAULT 0 COMMENT '是否套餐：0-否 1-是',
    is_card_project TINYINT DEFAULT 1 COMMENT '是否次卡项目：0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    sort INT DEFAULT 0 COMMENT '排序',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目表';

-- 迁移 service_item 数据到 service_project
INSERT INTO service_project (project_code, name, category_id, category_name, description, duration, price, status, sort)
SELECT service_code, service_name, category_id, category_name, description, duration, price, status, sort 
FROM service_item;

-- ========================================
-- 完成提示
-- ========================================

-- ========================================
-- 8. 添加美容师测试数据
-- ========================================
-- 添加美容师用户（密码：123456）
INSERT INTO sys_user (username, password, real_name, phone, gender, status, remark) VALUES
('beautician1', '$2a$10$F1rx4TnCQm6p1AGnEEFKBOrTgsmrJomc9JUZjq0eRetmUbt.GY0Xe', '王小美', '13800138001', 2, 1, '美容师'),
('beautician2', '$2a$10$F1rx4TnCQm6p1AGnEEFKBOrTgsmrJomc9JUZjq0eRetmUbt.GY0Xe', '李小丽', '13800138002', 2, 1, '美容师'),
('beautician3', '$2a$10$F1rx4TnCQm6p1AGnEEFKBOrTgsmrJomc9JUZjq0eRetmUbt.GY0Xe', '张小芳', '13800138003', 2, 1, '美容师');

-- 关联美容师用户和角色（美容师角色ID为3）
INSERT INTO sys_user_role (user_id, role_id) VALUES
((SELECT id FROM sys_user WHERE username = 'beautician1'), 3),
((SELECT id FROM sys_user WHERE username = 'beautician2'), 3),
((SELECT id FROM sys_user WHERE username = 'beautician3'), 3);

-- ========================================
-- 9. 创建充值记录表
-- ========================================
DROP TABLE IF EXISTS recharge_record;
CREATE TABLE recharge_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '充值ID',
    recharge_no VARCHAR(30) NOT NULL COMMENT '充值单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    card_id BIGINT DEFAULT NULL COMMENT '储值卡ID',
    recharge_amount DECIMAL(12,2) NOT NULL COMMENT '充值金额',
    gift_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '赠送金额',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '到账金额',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '支付方式：cash-现金 wechat-微信 alipay-支付宝 card-银行卡',
    balance_before DECIMAL(12,2) DEFAULT 0.00 COMMENT '充值前余额',
    balance_after DECIMAL(12,2) DEFAULT 0.00 COMMENT '充值后余额',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已撤销 1-正常',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recharge_no (recharge_no),
    KEY idx_customer_id (customer_id),
    KEY idx_card_id (card_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- ========================================
-- 10. 创建套餐表（如果不存在）
-- ========================================
DROP TABLE IF EXISTS package;
CREATE TABLE package (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    package_name VARCHAR(100) NOT NULL COMMENT '套餐名称',
    package_code VARCHAR(30) DEFAULT NULL COMMENT '套餐编码',
    price DECIMAL(10,2) NOT NULL COMMENT '套餐价格',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    times INT DEFAULT 10 COMMENT '包含次数',
    valid_days INT DEFAULT 365 COMMENT '有效天数',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    image VARCHAR(255) DEFAULT NULL COMMENT '图片',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

-- ========================================
-- 11. 创建套餐项目关联表
-- ========================================
DROP TABLE IF EXISTS package_item;
CREATE TABLE package_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    package_id BIGINT NOT NULL COMMENT '套餐ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    project_name VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
    times INT DEFAULT 1 COMMENT '次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_package_id (package_id),
    KEY idx_project_id (project_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐项目关联表';

-- ========================================
-- 12. 创建套餐购买记录表
-- ========================================
DROP TABLE IF EXISTS package_purchase;
CREATE TABLE package_purchase (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '购买ID',
    purchase_no VARCHAR(30) NOT NULL COMMENT '购买单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    package_id BIGINT NOT NULL COMMENT '套餐ID',
    package_name VARCHAR(100) DEFAULT NULL COMMENT '套餐名称',
    times INT DEFAULT 0 COMMENT '购买次数',
    price DECIMAL(12,2) DEFAULT 0.00 COMMENT '套餐单价',
    total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '支付方式：balance-储值卡 cash-现金 wechat-微信 alipay-支付宝',
    card_id BIGINT DEFAULT NULL COMMENT '使用的储值卡ID',
    balance_before DECIMAL(12,2) DEFAULT 0.00 COMMENT '支付前储值余额',
    balance_after DECIMAL(12,2) DEFAULT 0.00 COMMENT '支付后储值余额',
    member_card_id BIGINT DEFAULT NULL COMMENT '生成的次卡ID',
    valid_start_date DATE DEFAULT NULL COMMENT '生效日期',
    valid_end_date DATE DEFAULT NULL COMMENT '到期日期',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已撤销 1-正常',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_purchase_no (purchase_no),
    KEY idx_customer_id (customer_id),
    KEY idx_package_id (package_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐购买记录表';

-- ========================================
-- 13. 插入测试套餐数据
-- ========================================
INSERT INTO package (package_name, package_code, price, original_price, times, valid_days, description, status, sort) VALUES
('补水神器1号套餐', 'PK001', 2980.00, 3580.00, 10, 365, '深层补水护理套餐，包含10次补水护理', 1, 1),
('美白焕肤套餐', 'PK002', 3980.00, 4980.00, 10, 365, '美白焕肤护理套餐，包含10次美白护理', 1, 2),
('抗衰紧致套餐', 'PK003', 5980.00, 7980.00, 10, 365, '抗衰紧致护理套餐，包含10次抗衰护理', 1, 3),
('美甲美睫套餐', 'PK004', 1980.00, 2580.00, 6, 180, '美甲美睫套餐，包含6次美甲或美睫服务', 1, 4);

SELECT '数据库表结构修复完成！' AS message;
