#!/usr/bin/env python
"""
修复 appointment 表结构
"""
import paramiko

def fix_appointment():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)
    
    print("="*60)
    print("修复 appointment 表结构")
    print("="*60)
    
    # 先删除旧表，重新创建符合实体类定义的表
    sql = """
DROP TABLE IF EXISTS appointment;
CREATE TABLE appointment (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    appointment_no VARCHAR(30) NOT NULL COMMENT '预约编号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    customer_name VARCHAR(50) DEFAULT NULL COMMENT '客户姓名',
    customer_phone VARCHAR(20) DEFAULT NULL COMMENT '客户手机号',
    project_id BIGINT DEFAULT NULL COMMENT '项目ID',
    project_name VARCHAR(100) DEFAULT NULL COMMENT '项目名称',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME DEFAULT NULL COMMENT '预约开始时间',
    end_time TIME DEFAULT NULL COMMENT '预约结束时间',
    beautician_id BIGINT DEFAULT NULL COMMENT '美容师ID',
    beautician_name VARCHAR(50) DEFAULT NULL COMMENT '美容师姓名',
    status VARCHAR(20) DEFAULT '待确认' COMMENT '状态：待确认/已确认/服务中/已完成/已取消/爽约',
    source VARCHAR(20) DEFAULT '前台预约' COMMENT '预约来源：前台预约/电话预约/微信预约',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    cancel_reason VARCHAR(200) DEFAULT NULL COMMENT '取消原因',
    cancel_time DATETIME DEFAULT NULL COMMENT '取消时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_appointment_no (appointment_no),
    KEY idx_customer_id (customer_id),
    KEY idx_appointment_date (appointment_date),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';
"""
    
    # 写入临时SQL文件
    stdin, stdout, stderr = ssh.exec_command(f"cat > /tmp/fix_appointment.sql << 'EOF'\n{sql}\nEOF")
    stdout.read()
    
    # 执行
    print("执行 appointment 表修复...")
    cmd = "mysql -u bcrm -p'BCRM@2024#App' bcrm < /tmp/fix_appointment.sql 2>&1"
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=60)
    print(stdout.read().decode('utf-8', errors='ignore'))
    
    # 验证
    print("\n验证表结构:")
    stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' -e 'DESC bcrm.appointment' 2>/dev/null")
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
    fix_appointment()
