#!/usr/bin/env python
"""
修复 service_project 表结构
"""
import paramiko

def fix_service_project():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)
    
    print("="*60)
    print("修复 service_project 表结构")
    print("="*60)
    
    # 重新创建符合实体类的表
    sql = """
DROP TABLE IF EXISTS service_project;
CREATE TABLE service_project (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    project_code VARCHAR(30) DEFAULT NULL COMMENT '项目编号',
    name VARCHAR(100) NOT NULL COMMENT '项目名称',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    category_name VARCHAR(50) DEFAULT NULL COMMENT '分类名称',
    image VARCHAR(255) DEFAULT NULL COMMENT '项目图片',
    description VARCHAR(500) DEFAULT NULL COMMENT '项目描述',
    duration INT DEFAULT 60 COMMENT '服务时长(分钟)',
    price DECIMAL(10,2) NOT NULL COMMENT '项目价格',
    member_price DECIMAL(10,2) DEFAULT NULL COMMENT '会员价',
    cost_price DECIMAL(10,2) DEFAULT NULL COMMENT '成本价',
    is_package TINYINT DEFAULT 0 COMMENT '是否套餐：0-否 1-是',
    is_card_project TINYINT DEFAULT 0 COMMENT '是否次卡项目：0-否 1-是',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    sort INT DEFAULT 0 COMMENT '排序',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目表';
"""
    
    stdin, stdout, stderr = ssh.exec_command(f"cat > /tmp/fix_service_project.sql << 'EOF'\n{sql}\nEOF")
    stdout.read()
    
    print("执行表修复...")
    cmd = "mysql -u bcrm -p'BCRM@2024#App' bcrm < /tmp/fix_service_project.sql 2>&1"
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=60)
    print(stdout.read().decode('utf-8', errors='ignore'))
    
    print("\n验证表结构:")
    stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' -e 'DESC bcrm.service_project' 2>/dev/null")
    print(stdout.read().decode('utf-8', errors='ignore'))
    
    print("\n重启BCRM服务...")
    stdin, stdout, stderr = ssh.exec_command("systemctl restart bcrm")
    stdout.read()
    
    import time
    time.sleep(5)
    
    stdin, stdout, stderr = ssh.exec_command("systemctl is-active bcrm")
    status = stdout.read().decode().strip()
    print(f"服务状态: {status}")
    
    ssh.close()
    print("\n修复完成！")

if __name__ == "__main__":
    fix_service_project()
