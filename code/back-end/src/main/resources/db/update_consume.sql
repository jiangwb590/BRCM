-- ========================================
-- 消费功能更新脚本
-- 1. 套餐购买记录表添加剩余次数字段
-- 2. 消费记录表添加套餐购买记录关联字段
-- ========================================

-- 套餐购买记录表添加剩余次数字段
ALTER TABLE package_purchase ADD COLUMN remain_times INT DEFAULT 0 COMMENT '剩余次数' AFTER times;

-- 更新现有数据的剩余次数（如果有的话）
UPDATE package_purchase SET remain_times = times WHERE remain_times = 0 OR remain_times IS NULL;

-- 消费记录表添加套餐购买记录关联
ALTER TABLE consume_record ADD COLUMN purchase_id BIGINT DEFAULT NULL COMMENT '套餐购买记录ID' AFTER card_id;
ALTER TABLE consume_record ADD COLUMN consume_times INT DEFAULT 1 COMMENT '消费次数' AFTER amount;

-- 添加索引
ALTER TABLE consume_record ADD INDEX idx_purchase_id (purchase_id);
