#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("修复数据库表结构...")

sql = """
-- 1. 修复 service_package 表
DROP TABLE IF EXISTS service_package;
CREATE TABLE service_package (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
    package_name VARCHAR(100) NOT NULL COMMENT '套餐名称',
    package_code VARCHAR(30) DEFAULT NULL COMMENT '套餐编码',
    price DECIMAL(10,2) NOT NULL COMMENT '套餐价格',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    card_price DECIMAL(10,2) DEFAULT NULL COMMENT '卡扣价',
    times INT DEFAULT 0 COMMENT '包含次数',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    image VARCHAR(255) DEFAULT NULL COMMENT '图片',
    valid_days INT DEFAULT 365 COMMENT '有效天数',
    service_times INT DEFAULT 0 COMMENT '累计服务次数',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

-- 2. 修复 sys_dict 表
DROP TABLE IF EXISTS sys_dict;
CREATE TABLE sys_dict (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '字典ID',
    dict_code VARCHAR(50) DEFAULT NULL COMMENT '字典编码',
    dict_name VARCHAR(100) DEFAULT NULL COMMENT '字典名称',
    dict_value VARCHAR(100) DEFAULT NULL COMMENT '字典值',
    dict_label VARCHAR(100) DEFAULT NULL COMMENT '字典标签',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    PRIMARY KEY (id),
    KEY idx_dict_code (dict_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统字典表';

-- 初始化字典数据
INSERT INTO sys_dict (dict_code, dict_name, dict_value, dict_label, parent_id, sort, status, create_time) VALUES
('customer_source', '客户来源', NULL, '客户来源', 0, 1, 1, NOW()),
('customer_source', NULL, 'douyin', '抖音', 1, 1, 1, NOW()),
('customer_source', NULL, 'meituan', '美团', 1, 2, 1, NOW()),
('customer_source', NULL, 'xiaohongshu', '小红书', 1, 3, 1, NOW()),
('customer_source', NULL, 'wechat', '微信', 1, 4, 1, NOW()),
('customer_source', NULL, 'friend', '朋友介绍', 1, 5, 1, NOW()),
('customer_source', NULL, 'walk_in', '自然进店', 1, 6, 1, NOW()),
('customer_source', NULL, 'other', '其他', 1, 7, 1, NOW()),
('pay_method', '支付方式', NULL, '支付方式', 0, 2, 1, NOW()),
('pay_method', NULL, 'cash', '现金', 9, 1, 1, NOW()),
('pay_method', NULL, 'wechat', '微信', 9, 2, 1, NOW()),
('pay_method', NULL, 'alipay', '支付宝', 9, 3, 1, NOW()),
('pay_method', NULL, 'card', '银行卡', 9, 4, 1, NOW()),
('appointment_status', '预约状态', NULL, '预约状态', 0, 3, 1, NOW()),
('appointment_status', NULL, 'pending', '待确认', 14, 1, 1, NOW()),
('appointment_status', NULL, 'confirmed', '已确认', 14, 2, 1, NOW()),
('appointment_status', NULL, 'serving', '服务中', 14, 3, 1, NOW()),
('appointment_status', NULL, 'completed', '已完成', 14, 4, 1, NOW()),
('appointment_status', NULL, 'cancelled', '已取消', 14, 5, 1, NOW()),
('appointment_status', NULL, 'no_show', '爽约', 14, 6, 1, NOW());
"""

stdin, stdout, stderr = ssh.exec_command("cat > /tmp/fix_all.sql << 'ENDSQL'\n" + sql + "\nENDSQL")
stdout.read()

stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' bcrm < /tmp/fix_all.sql 2>&1")
print(stdout.read().decode('utf-8', errors='ignore'))

stdin, stdout, stderr = ssh.exec_command("systemctl restart bcrm")
stdout.read()

import time
time.sleep(5)

stdin, stdout, stderr = ssh.exec_command("systemctl is-active bcrm")
print(f"服务状态: {stdout.read().decode().strip()}")

ssh.close()
print("完成！")
