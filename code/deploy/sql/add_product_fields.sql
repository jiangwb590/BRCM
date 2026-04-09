-- 添加产品相关字段到 consume_record 表
ALTER TABLE consume_record ADD COLUMN product_id BIGINT DEFAULT NULL COMMENT '产品ID' AFTER project_name;
ALTER TABLE consume_record ADD COLUMN product_name VARCHAR(100) DEFAULT NULL COMMENT '产品名称' AFTER product_id;
ALTER TABLE consume_record ADD COLUMN quantity INT DEFAULT 1 COMMENT '购买数量' AFTER product_name;

-- 更新消费类型注释
ALTER TABLE consume_record MODIFY COLUMN consume_type VARCHAR(20) COMMENT '消费类型：cash-现金消费/times-次卡消费/stored-储值消费/product-购买产品';
