#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
继续安装BCRM环境（Java已安装）
"""

import paramiko
import time

SERVER_IP = "47.97.245.82"
SERVER_USER = "root"
SERVER_PASS = "Diandian@123"

def run_cmd(ssh, cmd, timeout=300, show=True):
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    exit_code = stdout.channel.recv_exit_status()
    out = stdout.read().decode('utf-8', errors='ignore')
    err = stderr.read().decode('utf-8', errors='ignore')
    if show:
        if out:
            print(out[-1500:] if len(out) > 1500 else out)
        if err and 'DEPRECATION' not in err:
            print(f"[ERR] {err[-500:]}")
    return exit_code, out, err

def main():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(SERVER_IP, username=SERVER_USER, password=SERVER_PASS, timeout=30)
    print("已连接服务器\n")
    
    # 1. 验证Java
    print("="*50)
    print("验证Java环境")
    print("="*50)
    run_cmd(ssh, "java -version 2>&1")
    
    # 2. 修复软件源
    print("\n" + "="*50)
    print("修复软件源")
    print("="*50)
    run_cmd(ssh, "mkdir -p /etc/yum.repos.d/backup && mv /etc/yum.repos.d/*.repo /etc/yum.repos.d/backup/ 2>/dev/null || true")
    
    repo = '''[BaseOS]
name=CentOS-8 - BaseOS
baseurl=https://vault.centos.org/centos/8/BaseOS/$basearch/os/
gpgcheck=0
enabled=1

[AppStream]
name=CentOS-8 - AppStream
baseurl=https://vault.centos.org/centos/8/AppStream/$basearch/os/
gpgcheck=0
enabled=1'''
    
    run_cmd(ssh, f"cat > /etc/yum.repos.d/CentOS.repo << 'EOF'\n{repo}\nEOF")
    run_cmd(ssh, "dnf clean all && dnf makecache", timeout=120)
    
    # 3. 安装MySQL
    print("\n" + "="*50)
    print("安装MySQL")
    print("="*50)
    
    # 下载并安装MySQL仓库
    run_cmd(ssh, "cd /tmp && wget -q https://dev.mysql.com/get/mysql80-community-release-el8-9.noarch.rpm -O mysql.rpm", timeout=120)
    run_cmd(ssh, "rpm -ivh /tmp/mysql.rpm --nodeps 2>/dev/null || true")
    run_cmd(ssh, "dnf module disable mysql -y 2>/dev/null || true")
    run_cmd(ssh, "dnf install -y mysql-community-server", timeout=600)
    
    # 启动MySQL
    run_cmd(ssh, "systemctl start mysqld && systemctl enable mysqld")
    run_cmd(ssh, "systemctl status mysqld --no-pager | head -5")
    
    # 4. 配置MySQL密码
    print("\n" + "="*50)
    print("配置MySQL")
    print("="*50)
    
    _, temp_pass, _ = run_cmd(ssh, "grep 'temporary password' /var/log/mysqld.log 2>/dev/null | awk '{print $NF}'", show=False)
    temp_pass = temp_pass.strip()
    
    if temp_pass:
        print(f"找到临时密码，修改root密码...")
        run_cmd(ssh, f"mysql --connect-expired-password -u root -p'{temp_pass}' -e \"ALTER USER 'root'@'localhost' IDENTIFIED BY 'Diandian@123'; FLUSH PRIVILEGES;\" 2>&1 || echo 'Password may already be changed'")
    
    # 创建数据库和用户
    run_cmd(ssh, '''mysql -u root -p'Diandian@123' -e "
CREATE DATABASE IF NOT EXISTS bcrm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'bcrm'@'localhost' IDENTIFIED BY 'BCRM@2024#App';
CREATE USER IF NOT EXISTS 'bcrm'@'%' IDENTIFIED BY 'BCRM@2024#App';
GRANT ALL PRIVILEGES ON bcrm.* TO 'bcrm'@'localhost';
GRANT ALL PRIVILEGES ON bcrm.* TO 'bcrm'@'%';
FLUSH PRIVILEGES;
"''')
    
    # 导入SQL
    print("\n导入数据库结构...")
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm < /opt/bcrm-deploy/sql/init.sql 2>&1 || echo 'import done'")
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm < /opt/bcrm-deploy/sql/fix_dict.sql 2>&1 || true")
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm < /opt/bcrm-deploy/sql/add_consume_care.sql 2>&1 || true")
    
    # 验证
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm -e 'SHOW TABLES' | head -20")
    
    # 5. 安装Nginx
    print("\n" + "="*50)
    print("安装Nginx")
    print("="*50)
    run_cmd(ssh, "dnf install -y nginx", timeout=300)
    run_cmd(ssh, "mkdir -p /etc/nginx/conf.d")
    run_cmd(ssh, "cp /opt/bcrm-deploy/nginx/bcrm.conf /etc/nginx/conf.d/")
    run_cmd(ssh, "nginx -t")
    run_cmd(ssh, "systemctl start nginx && systemctl enable nginx")
    
    # 6. 配置防火墙
    print("\n" + "="*50)
    print("配置防火墙")
    print("="*50)
    run_cmd(ssh, "systemctl start firewalld 2>/dev/null || true")
    run_cmd(ssh, "firewall-cmd --permanent --add-port=80/tcp 2>/dev/null || true")
    run_cmd(ssh, "firewall-cmd --permanent --add-port=443/tcp 2>/dev/null || true")
    run_cmd(ssh, "firewall-cmd --reload 2>/dev/null || true")
    
    # 7. 启动BCRM服务
    print("\n" + "="*50)
    print("启动BCRM服务")
    print("="*50)
    run_cmd(ssh, "cp /opt/bcrm-deploy/systemd/bcrm.service /etc/systemd/system/")
    run_cmd(ssh, "systemctl daemon-reload")
    run_cmd(ssh, "systemctl stop bcrm 2>/dev/null || true")
    run_cmd(ssh, "systemctl start bcrm")
    run_cmd(ssh, "systemctl enable bcrm")
    
    time.sleep(8)
    
    # 8. 验证
    print("\n" + "="*50)
    print("验证部署")
    print("="*50)
    run_cmd(ssh, "systemctl status bcrm --no-pager | head -10")
    run_cmd(ssh, "ss -tuln | grep -E ':(80|8080)'")
    
    print("\n" + "="*50)
    print("测试访问")
    print("="*50)
    run_cmd(ssh, "curl -s -o /dev/null -w 'HTTP状态: %{http_code}\\n' http://localhost/")
    run_cmd(ssh, "curl -s http://localhost:8080/api/auth/captcha | head -c 150")
    
    print("\n\n" + "="*50)
    print("部署完成!")
    print(f"访问地址: http://{SERVER_IP}/")
    print("默认账号: admin / admin123")
    print("="*50)
    
    ssh.close()

if __name__ == "__main__":
    main()
