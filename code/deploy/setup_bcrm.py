#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
配置BCRM（环境已安装）
"""

import paramiko
import time

SERVER_IP = "47.97.245.82"
SERVER_USER = "root"
SERVER_PASS = "Diandian@123"

def run_cmd(ssh, cmd, timeout=120, show=True):
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    exit_code = stdout.channel.recv_exit_status()
    out = stdout.read().decode('utf-8', errors='ignore')
    err = stderr.read().decode('utf-8', errors='ignore')
    if show:
        if out:
            print(out[-1500:] if len(out) > 1500 else out)
        if err and 'Warning' not in err:
            print(f"[ERR] {err[-500:]}")
    return exit_code, out, err

def main():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(SERVER_IP, username=SERVER_USER, password=SERVER_PASS, timeout=30)
    print("已连接服务器\n")
    
    # 1. 创建数据库和用户
    print("="*50)
    print("步骤1: 创建数据库")
    print("="*50)
    run_cmd(ssh, '''mysql -u root -p'Diandian@123' -e "
CREATE DATABASE IF NOT EXISTS bcrm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'bcrm'@'localhost' IDENTIFIED BY 'BCRM@2024#App';
CREATE USER IF NOT EXISTS 'bcrm'@'%' IDENTIFIED BY 'BCRM@2024#App';
GRANT ALL PRIVILEGES ON bcrm.* TO 'bcrm'@'localhost';
GRANT ALL PRIVILEGES ON bcrm.* TO 'bcrm'@'%';
FLUSH PRIVILEGES;
SELECT user, host FROM mysql.user WHERE user='bcrm';
"''')
    
    # 2. 导入SQL
    print("\n" + "="*50)
    print("步骤2: 导入数据库结构")
    print("="*50)
    
    # 检查SQL文件是否存在
    run_cmd(ssh, "ls -la /opt/bcrm-deploy/sql/")
    
    print("\n导入init.sql...")
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm < /opt/bcrm-deploy/sql/init.sql 2>&1")
    
    print("\n导入fix_dict.sql...")
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm < /opt/bcrm-deploy/sql/fix_dict.sql 2>&1 || true")
    
    print("\n导入add_consume_care.sql...")
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm < /opt/bcrm-deploy/sql/add_consume_care.sql 2>&1 || true")
    
    # 验证表
    print("\n验证数据表...")
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm -e 'SELECT COUNT(*) as tables FROM information_schema.tables WHERE table_schema=\"bcrm\"'")
    
    # 3. 配置Nginx
    print("\n" + "="*50)
    print("步骤3: 配置Nginx")
    print("="*50)
    run_cmd(ssh, "mkdir -p /etc/nginx/conf.d")
    run_cmd(ssh, "cp /opt/bcrm-deploy/nginx/bcrm.conf /etc/nginx/conf.d/")
    run_cmd(ssh, "cat /etc/nginx/conf.d/bcrm.conf | head -20")
    
    print("\n测试Nginx配置...")
    run_cmd(ssh, "/usr/sbin/nginx -t")
    
    print("\n重载Nginx...")
    run_cmd(ssh, "/usr/sbin/nginx -s reload || systemctl restart nginx")
    
    # 4. 配置防火墙
    print("\n" + "="*50)
    print("步骤4: 配置防火墙")
    print("="*50)
    run_cmd(ssh, "firewall-cmd --permanent --add-port=80/tcp 2>/dev/null || true")
    run_cmd(ssh, "firewall-cmd --permanent --add-port=443/tcp 2>/dev/null || true")
    run_cmd(ssh, "firewall-cmd --reload 2>/dev/null || true")
    
    # 5. 启动BCRM服务
    print("\n" + "="*50)
    print("步骤5: 启动BCRM服务")
    print("="*50)
    
    # 检查JAR文件
    run_cmd(ssh, "ls -la /opt/bcrm/backend/")
    
    # 复制服务文件
    run_cmd(ssh, "cp /opt/bcrm-deploy/systemd/bcrm.service /etc/systemd/system/")
    run_cmd(ssh, "systemctl daemon-reload")
    
    # 停止旧服务
    run_cmd(ssh, "systemctl stop bcrm 2>/dev/null || true")
    
    # 启动服务
    run_cmd(ssh, "systemctl start bcrm")
    run_cmd(ssh, "systemctl enable bcrm")
    
    print("\n等待服务启动...")
    time.sleep(10)
    
    # 查看服务状态
    print("\n服务状态:")
    run_cmd(ssh, "systemctl status bcrm --no-pager | head -15")
    
    # 6. 验证
    print("\n" + "="*50)
    print("步骤6: 验证部署")
    print("="*50)
    
    print("\n端口监听:")
    run_cmd(ssh, "ss -tuln | grep -E ':(80|8080|3306)'")
    
    print("\n测试前端:")
    run_cmd(ssh, "curl -s -o /dev/null -w 'HTTP状态: %{http_code}\\n' http://localhost/")
    
    print("\n测试后端API:")
    run_cmd(ssh, "curl -s http://localhost:8080/api/auth/captcha | head -c 200")
    
    print("\n\n" + "="*50)
    print("部署完成!")
    print("="*50)
    print(f"\n访问地址: http://{SERVER_IP}/")
    print("默认账号: admin / admin123")
    
    ssh.close()

if __name__ == "__main__":
    main()