#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("修复 service_package 表...")

sql = """DROP TABLE IF EXISTS service_package;
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
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';
"""

# 写入临时文件
stdin, stdout, stderr = ssh.exec_command("cat > /tmp/fix_package.sql << 'ENDSQL'\n" + sql + "\nENDSQL")
stdout.read()

# 执行
stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' bcrm < /tmp/fix_package.sql 2>&1")
print(stdout.read().decode('utf-8', errors='ignore'))

stdin, stdout, stderr = ssh.exec_command("systemctl restart bcrm")
stdout.read()

import time
time.sleep(5)

stdin, stdout, stderr = ssh.exec_command("systemctl is-active bcrm")
print(f"服务状态: {stdout.read().decode().strip()}")

ssh.close()
print("完成！")