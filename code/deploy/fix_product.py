#!/usr/bin/env python
"""
修复 product 表结构
"""
import paramiko

def fix_product():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)
    
    print("="*60)
    print("修复 product 表结构")
    print("="*60)
    
    sql = """
DROP TABLE IF EXISTS product;
CREATE TABLE product (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品ID',
    product_code VARCHAR(30) DEFAULT NULL COMMENT '产品编码',
    name VARCHAR(100) NOT NULL COMMENT '产品名称',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    category_name VARCHAR(50) DEFAULT NULL COMMENT '分类名称',
    specification VARCHAR(100) DEFAULT NULL COMMENT '规格',
    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
    purchase_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '进价',
    sale_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '售价',
    stock INT DEFAULT 0 COMMENT '当前库存',
    stock_warning INT DEFAULT 10 COMMENT '库存预警值',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_stock (stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';
"""
    
    stdin, stdout, stderr = ssh.exec_command(f"cat > /tmp/fix_product.sql << 'EOF'\n{sql}\nEOF")
    stdout.read()
    
    print("执行表修复...")
    cmd = "mysql -u bcrm -p'BCRM@2024#App' bcrm < /tmp/fix_product.sql 2>&1"
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=60)
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
    fix_product()