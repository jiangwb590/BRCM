-- ========================================
-- BCRM 数据库表结构修复脚本
-- 修复实体类与数据库表结构不一致的问题
-- ========================================

USE bcrm;

-- ========================================
-- 1. 创建消费记录表 consume_record
-- ========================================
DROP TABLE IF EXISTS consume_record;
CREATE TABLE consume_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    consume_no VARCHAR(30) NOT NULL COMMENT '消费单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    project_id BIGINT DEFAULT NULL COMMENT '项目ID',
    project_name VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
    consume_type VARCHAR(20) DEFAULT NULL COMMENT '消费类型：现金消费/次卡消费/储值消费',
    amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '消费金额',
    consume_times INT DEFAULT 0 COMMENT '消费次数（次卡消费时使用）',
    pay_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '实际支付金额',
    discount_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '折扣金额',
    beautician_id BIGINT DEFAULT NULL COMMENT '美容师ID',
    beautician_name VARCHAR(50) DEFAULT NULL COMMENT '美容师姓名',
    card_id BIGINT DEFAULT NULL COMMENT '次卡ID（次卡消费时使用）',
    purchase_id BIGINT DEFAULT NULL COMMENT '套餐购买记录ID',
    appointment_id BIGINT DEFAULT NULL COMMENT '关联预约ID',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '支付方式：现金/微信/支付宝/银行卡',
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
    KEY idx_consume_time (consume_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费记录表';

-- ========================================
-- 2. 创建客户关怀表 customer_care
-- ========================================
DROP TABLE IF EXISTS customer_care;
CREATE TABLE customer_care (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '关怀ID',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    consume_record_id BIGINT DEFAULT NULL COMMENT '关联消费记录ID',
    project_id BIGINT DEFAULT NULL COMMENT '关联项目ID',
    project_name VARCHAR(100) DEFAULT NULL COMMENT '项目/套餐名称',
    project_description VARCHAR(500) DEFAULT NULL COMMENT '项目/套餐描述',
    customer_phone VARCHAR(20) DEFAULT NULL COMMENT '客户手机号',
    care_type VARCHAR(30) DEFAULT NULL COMMENT '关怀类型：生日关怀/消费关怀/沉睡唤醒/回访提醒',
    content VARCHAR(500) DEFAULT NULL COMMENT '关怀内容',
    plan_date DATE DEFAULT NULL COMMENT '计划日期',
    status VARCHAR(20) DEFAULT '待执行' COMMENT '执行状态：待执行/已执行/已取消',
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
    KEY idx_plan_date (plan_date),
    KEY idx_status (status),
    KEY idx_care_type (care_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户关怀表';

-- ========================================
-- 3. 修改客户表 customer - 添加缺失字段
-- ========================================
-- 添加微信字段
ALTER TABLE customer ADD COLUMN wechat VARCHAR(50) DEFAULT NULL COMMENT '微信号' AFTER phone;

-- 添加 category 字段（如果不存在）
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'customer' AND COLUMN_NAME = 'category');
SET @sql := IF(@exist_col = 0, 
    'ALTER TABLE customer ADD COLUMN category VARCHAR(20) DEFAULT ''potential'' COMMENT ''客户分类：potential-潜在 new-新客户 old-老客户 vip-VIP sleep-沉睡'' AFTER source', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 advisor_id 字段（如果不存在）
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'customer' AND COLUMN_NAME = 'advisor_id');
SET @sql := IF(@exist_col = 0, 
    'ALTER TABLE customer ADD COLUMN advisor_id BIGINT DEFAULT NULL COMMENT ''顾问ID'' AFTER member_level_id', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 total_amount 字段（如果不存在）
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'customer' AND COLUMN_NAME = 'total_amount');
SET @sql := IF(@exist_col = 0, 
    'ALTER TABLE customer ADD COLUMN total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT ''累计消费金额'' AFTER advisor_id', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 consume_count 字段（如果不存在）
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'customer' AND COLUMN_NAME = 'consume_count');
SET @sql := IF(@exist_col = 0, 
    'ALTER TABLE customer ADD COLUMN consume_count INT DEFAULT 0 COMMENT ''消费次数'' AFTER total_amount', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 last_consume_time 字段（如果不存在）
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'customer' AND COLUMN_NAME = 'last_consume_time');
SET @sql := IF(@exist_col = 0, 
    'ALTER TABLE customer ADD COLUMN last_consume_time DATETIME DEFAULT NULL COMMENT ''最近消费时间'' AFTER consume_count', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 balance 字段（如果不存在）
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'customer' AND COLUMN_NAME = 'balance');
SET @sql := IF(@exist_col = 0, 
    'ALTER TABLE customer ADD COLUMN balance DECIMAL(12,2) DEFAULT 0.00 COMMENT ''储值余额'' AFTER last_consume_time', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 points 字段（如果不存在）
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'customer' AND COLUMN_NAME = 'points');
SET @sql := IF(@exist_col = 0, 
    'ALTER TABLE customer ADD COLUMN points INT DEFAULT 0 COMMENT ''积分'' AFTER balance', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 tags 字段（如果不存在）
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'customer' AND COLUMN_NAME = 'tags');
SET @sql := IF(@exist_col = 0, 
    'ALTER TABLE customer ADD COLUMN tags VARCHAR(500) DEFAULT NULL COMMENT ''标签（多个用逗号分隔）'' AFTER points', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 4. 修改预约表 appointment - 添加缺失字段
-- ========================================
-- 删除旧字段（如果存在）
ALTER TABLE appointment DROP COLUMN IF EXISTS service_id;
ALTER TABLE appointment DROP COLUMN IF EXISTS service_name;
ALTER TABLE appointment DROP COLUMN IF EXISTS staff_id;
ALTER TABLE appointment DROP COLUMN IF EXISTS staff_name;
ALTER TABLE appointment DROP COLUMN IF EXISTS appointment_time;
ALTER TABLE appointment DROP COLUMN IF EXISTS duration;

-- 添加 project_id 字段
ALTER TABLE appointment ADD COLUMN project_id BIGINT DEFAULT NULL COMMENT '项目ID' AFTER customer_phone;

-- 添加 project_name 字段
ALTER TABLE appointment ADD COLUMN project_name VARCHAR(100) DEFAULT NULL COMMENT '项目名称' AFTER project_id;

-- 添加 start_time 字段
ALTER TABLE appointment ADD COLUMN start_time TIME DEFAULT NULL COMMENT '预约开始时间' AFTER appointment_date;

-- 添加 end_time 字段
ALTER TABLE appointment ADD COLUMN end_time TIME DEFAULT NULL COMMENT '预约结束时间' AFTER start_time;

-- 添加 beautician_id 字段
ALTER TABLE appointment ADD COLUMN beautician_id BIGINT DEFAULT NULL COMMENT '美容师ID' AFTER end_time;

-- 添加 beautician_name 字段
ALTER TABLE appointment ADD COLUMN beautician_name VARCHAR(50) DEFAULT NULL COMMENT '美容师姓名' AFTER beautician_id;

-- 修改 status 字段类型为 VARCHAR
ALTER TABLE appointment MODIFY COLUMN status VARCHAR(20) DEFAULT '待确认' COMMENT '状态：待确认/已确认/服务中/已完成/已取消/爽约';

-- 添加 cancel_time 字段（如果不存在）
SET @exist_col := (SELECT COUNT(*) FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'appointment' AND COLUMN_NAME = 'cancel_time');
SET @sql := IF(@exist_col = 0, 
    'ALTER TABLE appointment ADD COLUMN cancel_time DATETIME DEFAULT NULL COMMENT ''取消时间'' AFTER cancel_reason', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 5. 创建充值记录表 recharge_record（如果不存在）
-- ========================================
SET @exist_table := (SELECT COUNT(*) FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'recharge_record');
SET @sql := IF(@exist_table = 0, 
    'CREATE TABLE recharge_record (
        id BIGINT NOT NULL AUTO_INCREMENT COMMENT ''记录ID'',
        recharge_no VARCHAR(30) NOT NULL COMMENT ''充值单号'',
        customer_id BIGINT NOT NULL COMMENT ''客户ID'',
        customer_name VARCHAR(50) DEFAULT NULL COMMENT ''客户姓名'',
        recharge_amount DECIMAL(12,2) NOT NULL COMMENT ''充值金额'',
        gift_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT ''赠送金额'',
        total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT ''实际到账金额'',
        pay_method VARCHAR(20) DEFAULT NULL COMMENT ''支付方式'',
        operator_id BIGINT DEFAULT NULL COMMENT ''操作人ID'',
        operator_name VARCHAR(50) DEFAULT NULL COMMENT ''操作人姓名'',
        remark VARCHAR(500) DEFAULT NULL COMMENT ''备注'',
        recharge_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''充值时间'',
        status TINYINT DEFAULT 1 COMMENT ''状态：0-已退款 1-正常'',
        create_by BIGINT DEFAULT NULL COMMENT ''创建人'',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
        update_by BIGINT DEFAULT NULL COMMENT ''更新人'',
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'',
        deleted TINYINT DEFAULT 0 COMMENT ''删除标记：0-未删除 1-已删除'',
        PRIMARY KEY (id),
        UNIQUE KEY uk_recharge_no (recharge_no),
        KEY idx_customer_id (customer_id),
        KEY idx_recharge_time (recharge_time)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''充值记录表''', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 6. 创建会员卡表 member_card（如果不存在）
-- ========================================
SET @exist_table := (SELECT COUNT(*) FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'member_card');
SET @sql := IF(@exist_table = 0, 
    'CREATE TABLE member_card (
        id BIGINT NOT NULL AUTO_INCREMENT COMMENT ''会员卡ID'',
        card_no VARCHAR(30) NOT NULL COMMENT ''卡号'',
        customer_id BIGINT NOT NULL COMMENT ''客户ID'',
        customer_name VARCHAR(50) DEFAULT NULL COMMENT ''客户姓名'',
        card_type TINYINT NOT NULL COMMENT ''卡类型：1-次卡 2-储值卡'',
        card_name VARCHAR(100) DEFAULT NULL COMMENT ''卡名称'',
        total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT ''总金额'',
        remain_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT ''剩余金额'',
        total_times INT DEFAULT 0 COMMENT ''总次数'',
        remain_times INT DEFAULT 0 COMMENT ''剩余次数'',
        start_date DATE DEFAULT NULL COMMENT ''生效日期'',
        end_date DATE DEFAULT NULL COMMENT ''到期日期'',
        status TINYINT DEFAULT 1 COMMENT ''状态：0-已过期 1-正常 2-已用完'',
        create_by BIGINT DEFAULT NULL COMMENT ''创建人'',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
        update_by BIGINT DEFAULT NULL COMMENT ''更新人'',
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'',
        deleted TINYINT DEFAULT 0 COMMENT ''删除标记：0-未删除 1-已删除'',
        remark VARCHAR(500) DEFAULT NULL COMMENT ''备注'',
        PRIMARY KEY (id),
        UNIQUE KEY uk_card_no (card_no),
        KEY idx_customer_id (customer_id),
        KEY idx_end_date (end_date)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''会员卡表''', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 7. 创建套餐购买记录表 package_purchase（如果不存在）
-- ========================================
SET @exist_table := (SELECT COUNT(*) FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'package_purchase');
SET @sql := IF(@exist_table = 0, 
    'CREATE TABLE package_purchase (
        id BIGINT NOT NULL AUTO_INCREMENT COMMENT ''购买ID'',
        purchase_no VARCHAR(30) NOT NULL COMMENT ''购买单号'',
        customer_id BIGINT NOT NULL COMMENT ''客户ID'',
        customer_name VARCHAR(50) DEFAULT NULL COMMENT ''客户姓名'',
        package_id BIGINT NOT NULL COMMENT ''套餐ID'',
        package_name VARCHAR(100) DEFAULT NULL COMMENT ''套餐名称'',
        pay_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT ''支付金额'',
        pay_method VARCHAR(20) DEFAULT NULL COMMENT ''支付方式'',
        start_date DATE DEFAULT NULL COMMENT ''生效日期'',
        end_date DATE DEFAULT NULL COMMENT ''到期日期'',
        status TINYINT DEFAULT 1 COMMENT ''状态：0-已过期 1-正常 2-已用完'',
        create_by BIGINT DEFAULT NULL COMMENT ''创建人'',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
        update_by BIGINT DEFAULT NULL COMMENT ''更新人'',
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'',
        deleted TINYINT DEFAULT 0 COMMENT ''删除标记：0-未删除 1-已删除'',
        remark VARCHAR(500) DEFAULT NULL COMMENT ''备注'',
        PRIMARY KEY (id),
        UNIQUE KEY uk_purchase_no (purchase_no),
        KEY idx_customer_id (customer_id),
        KEY idx_package_id (package_id),
        KEY idx_end_date (end_date)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''套餐购买记录表''', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 8. 创建服务项目表 service_project（如果不存在）
-- ========================================
SET @exist_table := (SELECT COUNT(*) FROM information_schema.TABLES 
    WHERE TABLE_SCHEMA = 'bcrm' AND TABLE_NAME = 'service_project');
SET @sql := IF(@exist_table = 0, 
    'CREATE TABLE service_project (
        id BIGINT NOT NULL AUTO_INCREMENT COMMENT ''项目ID'',
        project_code VARCHAR(30) DEFAULT NULL COMMENT ''项目编码'',
        project_name VARCHAR(100) NOT NULL COMMENT ''项目名称'',
        category_id BIGINT DEFAULT NULL COMMENT ''分类ID'',
        category_name VARCHAR(50) DEFAULT NULL COMMENT ''分类名称'',
        price DECIMAL(10,2) NOT NULL COMMENT ''价格'',
        original_price DECIMAL(10,2) DEFAULT NULL COMMENT ''原价'',
        duration INT DEFAULT 60 COMMENT ''时长(分钟)'',
        description VARCHAR(500) DEFAULT NULL COMMENT ''描述'',
        image VARCHAR(255) DEFAULT NULL COMMENT ''图片'',
        service_times INT DEFAULT 0 COMMENT ''累计服务次数'',
        is_hot TINYINT DEFAULT 0 COMMENT ''是否热门：0-否 1-是'',
        is_recommend TINYINT DEFAULT 0 COMMENT ''是否推荐：0-否 1-是'',
        sort INT DEFAULT 0 COMMENT ''排序'',
        status TINYINT DEFAULT 1 COMMENT ''状态：0-下架 1-上架'',
        create_by BIGINT DEFAULT NULL COMMENT ''创建人'',
        create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间'',
        update_by BIGINT DEFAULT NULL COMMENT ''更新人'',
        update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''更新时间'',
        deleted TINYINT DEFAULT 0 COMMENT ''删除标记：0-未删除 1-已删除'',
        remark VARCHAR(500) DEFAULT NULL COMMENT ''备注'',
        PRIMARY KEY (id),
        KEY idx_category_id (category_id),
        KEY idx_status (status)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=''服务项目表''', 
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 迁移 service_item 数据到 service_project（如果 service_project 为空）
INSERT INTO service_project (project_code, project_name, category_id, category_name, price, original_price, duration, description, image, service_times, is_hot, is_recommend, sort, status, create_by, create_time, update_by, update_time, deleted, remark)
SELECT service_code, service_name, category_id, category_name, price, original_price, duration, description, image, service_times, is_hot, is_recommend, sort, status, create_by, create_time, update_by, update_time, deleted, remark
FROM service_item WHERE NOT EXISTS (SELECT 1 FROM service_project LIMIT 1);

-- ========================================
-- 完成提示
-- ========================================
SELECT '数据库表结构修复完成！' AS message;
