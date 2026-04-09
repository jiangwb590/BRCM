-- ========================================
-- BCRM 增量更新脚本
-- 仅包含新增的表（充值、套餐购买功能）
-- ========================================

USE bcrm;

-- ========================================
-- 1. 创建充值记录表
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
    total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '到账总金额',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '支付方式：cash-现金 wechat-微信 alipay-支付宝 bank-银行卡',
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
-- 2. 修复 service_package 表（添加times字段）
-- ========================================
-- 使用存储过程添加times字段（如果不存在）
DELIMITER //
DROP PROCEDURE IF EXISTS add_times_column //
CREATE PROCEDURE add_times_column()
BEGIN
    DECLARE col_count INT;
    SELECT COUNT(*) INTO col_count FROM information_schema.columns 
    WHERE table_schema = 'bcrm' AND table_name = 'service_package' AND column_name = 'times';
    IF col_count = 0 THEN
        ALTER TABLE service_package ADD COLUMN times INT DEFAULT 10 COMMENT '包含次数' AFTER original_price;
    END IF;
END //
DELIMITER ;
CALL add_times_column();
DROP PROCEDURE IF EXISTS add_times_column;

-- ========================================
-- 3. 创建套餐项目关联表
-- ========================================
DROP TABLE IF EXISTS package_item;
CREATE TABLE package_item (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    package_id BIGINT NOT NULL COMMENT '套餐ID',
    service_id BIGINT DEFAULT NULL COMMENT '项目ID',
    service_name VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
    times INT DEFAULT 1 COMMENT '次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_package_id (package_id),
    KEY idx_service_id (service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐项目关联表';

-- ========================================
-- 4. 创建套餐购买记录表
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
-- 5. 插入测试套餐数据
-- ========================================
INSERT IGNORE INTO service_package (package_name, package_code, price, original_price, times, valid_days, description, status, sort) VALUES
('补水神器1号套餐', 'PK001', 298.00, 358.00, 10, 365, '深层补水护理套餐，包含10次补水护理', 1, 1),
('美白焕肤套餐', 'PK002', 398.00, 498.00, 10, 365, '美白焕肤护理套餐，包含10次美白护理', 1, 2),
('抗衰紧致套餐', 'PK003', 598.00, 798.00, 10, 365, '抗衰紧致护理套餐，包含10次抗衰护理', 1, 3),
('美甲美睫套餐', 'PK004', 198.00, 258.00, 6, 180, '美甲美睫套餐，包含6次美甲或美睫服务', 1, 4);

-- ========================================
-- 6. 添加美容师测试数据
-- ========================================
INSERT IGNORE INTO sys_user (username, password, real_name, phone, gender, status, remark) VALUES
('beautician1', '$2a$10$F1rx4TnCQm6p1AGnEEFKBOrTgsmrJomc9JUZjq0eRetmUbt.GY0Xe', '王小美', '13800138001', 2, 1, '美容师'),
('beautician2', '$2a$10$F1rx4TnCQm6p1AGnEEFKBOrTgsmrJomc9JUZjq0eRetmUbt.GY0Xe', '李小丽', '13800138002', 2, 1, '美容师'),
('beautician3', '$2a$10$F1rx4TnCQm6p1AGnEEFKBOrTgsmrJomc9JUZjq0eRetmUbt.GY0Xe', '张小芳', '13800138003', 2, 1, '美容师');

-- 关联美容师用户和角色（美容师角色ID为3）
INSERT IGNORE INTO sys_user_role (user_id, role_id) 
SELECT id, 3 FROM sys_user WHERE username IN ('beautician1', 'beautician2', 'beautician3');

SELECT '增量更新完成！' AS message;
