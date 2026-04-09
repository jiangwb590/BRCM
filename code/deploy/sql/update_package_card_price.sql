-- ========================================
-- 套餐表添加卡扣价字段
-- ========================================

ALTER TABLE service_package ADD COLUMN card_price DECIMAL(10,2) DEFAULT NULL COMMENT '卡扣价（储值卡支付价格）' AFTER original_price;

-- 将现有price字段值同步到card_price
UPDATE service_package SET card_price = price WHERE card_price IS NULL;

-- 将price字段改名为保留，实际使用card_price作为储值支付价格
-- 注意：前端套餐列表中的price现在是卡扣价，originalPrice是原价
