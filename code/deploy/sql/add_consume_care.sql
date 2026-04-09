-- ========================================
-- 添加消费关怀配置和扩展字段
-- ========================================

-- 添加消费关怀间隔天数配置
INSERT INTO sys_dict (dict_name, dict_code, dict_value, dict_label, parent_id, sort, status, create_by, create_time, remark)
VALUES ('消费回访间隔天数', 'consume_care_interval', '3', '消费后回访天数', NULL, 10, 1, 1, NOW(), '消费后多少天发送回访短信，单位：天');

-- 扩展customer_care表，添加消费回访相关字段
ALTER TABLE customer_care ADD COLUMN consume_record_id BIGINT DEFAULT NULL COMMENT '关联消费记录ID' AFTER customerName;
ALTER TABLE customer_care ADD COLUMN project_id BIGINT DEFAULT NULL COMMENT '关联项目ID' AFTER consume_record_id;
ALTER TABLE customer_care ADD COLUMN project_name VARCHAR(100) DEFAULT NULL COMMENT '项目/套餐名称' AFTER project_id;
ALTER TABLE customer_care ADD COLUMN project_description TEXT DEFAULT NULL COMMENT '项目/套餐描述(用于短信内容)' AFTER project_name;
ALTER TABLE customer_care ADD COLUMN customer_phone VARCHAR(20) DEFAULT NULL COMMENT '客户手机号' AFTER project_description;

-- 添加索引
ALTER TABLE customer_care ADD INDEX idx_consume_record_id (consume_record_id);
ALTER TABLE customer_care ADD INDEX idx_status_plan_date (status, plan_date);
