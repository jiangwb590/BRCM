#!/usr/bin/env python
"""
修复 message 表结构
"""
import paramiko

def fix_message():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)
    
    print("="*60)
    print("修复 message 表结构")
    print("="*60)
    
    # 重新创建 message 表
    sql = """
DROP TABLE IF EXISTS message;
CREATE TABLE message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    title VARCHAR(100) NOT NULL COMMENT '消息标题',
    content VARCHAR(500) DEFAULT NULL COMMENT '消息内容',
    message_type VARCHAR(30) NOT NULL COMMENT '消息类型：系统消息/预约提醒/消费提醒/库存预警',
    receiver_id BIGINT DEFAULT NULL COMMENT '接收人ID',
    receiver_name VARCHAR(50) DEFAULT NULL COMMENT '接收人姓名',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    read_time DATETIME DEFAULT NULL COMMENT '阅读时间',
    related_id BIGINT DEFAULT NULL COMMENT '关联业务ID',
    related_type VARCHAR(30) DEFAULT NULL COMMENT '关联业务类型',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除 1-已删除',
    PRIMARY KEY (id),
    KEY idx_receiver_id (receiver_id),
    KEY idx_message_type (message_type),
    KEY idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';
"""
    
    stdin, stdout, stderr = ssh.exec_command(f"cat > /tmp/fix_message.sql << 'EOF'\n{sql}\nEOF")
    stdout.read()
    
    print("执行 message 表修复...")
    cmd = "mysql -u bcrm -p'BCRM@2024#App' bcrm < /tmp/fix_message.sql 2>&1"
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=60)
    print(stdout.read().decode('utf-8', errors='ignore'))
    
    print("\n验证表结构:")
    stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' -e 'DESC bcrm.message' 2>/dev/null")
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
    fix_message()