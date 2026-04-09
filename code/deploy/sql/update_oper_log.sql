-- 操作日志表添加操作对象字段
ALTER TABLE sys_oper_log ADD COLUMN target_type VARCHAR(50) DEFAULT NULL COMMENT '操作对象类型' AFTER cost_time;
ALTER TABLE sys_oper_log ADD COLUMN target_id BIGINT DEFAULT NULL COMMENT '操作对象ID' AFTER target_type;
ALTER TABLE sys_oper_log ADD COLUMN target_name VARCHAR(100) DEFAULT NULL COMMENT '操作对象名称' AFTER target_id;

-- 创建索引
ALTER TABLE sys_oper_log ADD INDEX idx_oper_name (oper_name);
ALTER TABLE sys_oper_log ADD INDEX idx_oper_time (oper_time);
ALTER TABLE sys_oper_log ADD INDEX idx_target_type (target_type);
