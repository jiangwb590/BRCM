-- ========================================
-- 修复字典表结构
-- 将双表设计改为单表设计（通过parent_id关联）
-- ========================================

-- 添加缺失的字段
ALTER TABLE sys_dict ADD COLUMN dict_value VARCHAR(50) DEFAULT NULL COMMENT '字典值' AFTER dict_code;
ALTER TABLE sys_dict ADD COLUMN dict_label VARCHAR(50) DEFAULT NULL COMMENT '字典标签' AFTER dict_value;
ALTER TABLE sys_dict ADD COLUMN parent_id BIGINT DEFAULT NULL COMMENT '父级ID' AFTER dict_label;

-- 添加索引
ALTER TABLE sys_dict ADD INDEX idx_parent_id (parent_id);

-- 删除唯一约束（字典项不需要唯一编码）
ALTER TABLE sys_dict DROP INDEX uk_dict_code;

-- 修改dict_code允许NULL
ALTER TABLE sys_dict MODIFY COLUMN dict_code VARCHAR(50) DEFAULT NULL COMMENT '字典编码';

-- 迁移数据：将sys_dict_item的数据迁移到sys_dict
-- 字典项的dict_code设为NULL，通过parent_id关联父级
INSERT INTO sys_dict (dict_name, dict_code, dict_value, dict_label, parent_id, sort, status, create_by, create_time, remark)
SELECT 
    item.item_label AS dict_name,
    NULL AS dict_code,
    item.item_value AS dict_value,
    item.item_label AS dict_label,
    item.dict_id AS parent_id,
    item.sort AS sort,
    item.status AS status,
    item.create_by AS create_by,
    item.create_time AS create_time,
    NULL AS remark
FROM sys_dict_item item;

-- 迁移完成后可以删除旧的字典项表（建议先备份数据后再执行）
-- DROP TABLE IF EXISTS sys_dict_item;
