-- 添加产品消耗记录字段到 consume_record 表
ALTER TABLE consume_record ADD COLUMN product_consumptions TEXT DEFAULT NULL COMMENT '产品消耗记录（JSON格式）';