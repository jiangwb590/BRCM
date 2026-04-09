#!/usr/bin/env python
"""
创建 member_card 和其他缺失的表
"""
import paramiko

def fix_tables():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)
    
    print("="*60)
    print("创建缺失的表")
    print("="*60)
    
    # 创建 member_card 表
    sql = """
CREATE TABLE IF NOT EXISTS member_card (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '会员卡ID',
    card_no VARCHAR(30) NOT NULL COMMENT '卡号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    card_type TINYINT NOT NULL COMMENT '卡类型：1-次卡 2-储值卡',
    card_name VARCHAR(100) DEFAULT NULL COMMENT '卡名称',
    total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
    remain_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '剩余金额',
    total_times INT DEFAULT 0 COMMENT '总次数',
    remain_times INT DEFAULT 0 COMMENT '剩余次数',
    start_date DATE DEFAULT NULL COMMENT '生效日期',
    end_date DATE DEFAULT NULL COMMENT '到期日期',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已过期 1-正常 2-已用完',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_card_no (card_no),
    KEY idx_customer_id (customer_id),
    KEY idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员卡表';

CREATE TABLE IF NOT EXISTS recharge_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    recharge_no VARCHAR(30) NOT NULL COMMENT '充值单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    recharge_amount DECIMAL(12,2) NOT NULL COMMENT '充值金额',
    gift_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '赠送金额',
    total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '实际到账金额',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '支付方式',
    operator_id BIGINT DEFAULT NULL COMMENT '操作人ID',
    operator_name VARCHAR(50) DEFAULT NULL COMMENT '操作人姓名',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    recharge_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '充值时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已退款 1-正常',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recharge_no (recharge_no),
    KEY idx_customer_id (customer_id),
    KEY idx_recharge_time (recharge_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

CREATE TABLE IF NOT EXISTS package_purchase (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '购买ID',
    purchase_no VARCHAR(30) NOT NULL COMMENT '购买单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    package_id BIGINT NOT NULL COMMENT '套餐ID',
    package_name VARCHAR(100) DEFAULT NULL COMMENT '套餐名称',
    pay_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '支付金额',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '支付方式',
    start_date DATE DEFAULT NULL COMMENT '生效日期',
    end_date DATE DEFAULT NULL COMMENT '到期日期',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已过期 1-正常 2-已用完',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_purchase_no (purchase_no),
    KEY idx_customer_id (customer_id),
    KEY idx_package_id (package_id),
    KEY idx_end_date (end_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐购买记录表';

CREATE TABLE IF NOT EXISTS service_project (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '项目ID',
    project_code VARCHAR(30) DEFAULT NULL COMMENT '项目编码',
    project_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    category_id BIGINT DEFAULT NULL COMMENT '分类ID',
    category_name VARCHAR(50) DEFAULT NULL COMMENT '分类名称',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    duration INT DEFAULT 60 COMMENT '时长(分钟)',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    image VARCHAR(255) DEFAULT NULL COMMENT '图片',
    service_times INT DEFAULT 0 COMMENT '累计服务次数',
    is_hot TINYINT DEFAULT 0 COMMENT '是否热门：0-否 1-是',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推荐：0-否 1-是',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_category_id (category_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务项目表';
"""
    
    stdin, stdout, stderr = ssh.exec_command(f"cat > /tmp/fix_tables.sql << 'EOF'\n{sql}\nEOF")
    stdout.read()
    
    print("执行表创建...")
    cmd = "mysql -u bcrm -p'BCRM@2024#App' bcrm < /tmp/fix_tables.sql 2>&1"
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=60)
    print(stdout.read().decode('utf-8', errors='ignore'))
    
    print("\n验证表是否存在:")
    stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' -e 'SHOW TABLES FROM bcrm LIKE \"member_card\"' 2>/dev/null")
    print(stdout.read().decode('utf-8', errors='ignore'))
    
    # 重启服务
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
    fix_tables()
