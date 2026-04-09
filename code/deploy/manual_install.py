#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
手动安装BCRM环境
"""

import paramiko
import time

SERVER_IP = "47.97.245.82"
SERVER_USER = "root"
SERVER_PASS = "Diandian@123"

def run_cmd(ssh, cmd, timeout=300):
    print(f"\n>>> 执行: {cmd[:60]}...")
    stdin, stdout, stderr = ssh.exec_command(cmd, timeout=timeout)
    exit_code = stdout.channel.recv_exit_status()
    out = stdout.read().decode('utf-8', errors='ignore')
    err = stderr.read().decode('utf-8', errors='ignore')
    if out:
        print(out[-2000:] if len(out) > 2000 else out)
    if err and 'DEPRECATION' not in err:
        print(f"[ERR] {err[-1000:]}")
    return exit_code, out, err

def main():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(SERVER_IP, username=SERVER_USER, password=SERVER_PASS, timeout=30)
    print("已连接服务器")
    
    # 1. 修复软件源
    print("\n" + "="*50)
    print("步骤1: 修复CentOS 8软件源")
    print("="*50)
    
    run_cmd(ssh, "mkdir -p /etc/yum.repos.d/backup")
    run_cmd(ssh, "mv /etc/yum.repos.d/*.repo /etc/yum.repos.d/backup/ 2>/dev/null || true")
    
    repo = '''[BaseOS]
name=CentOS-8 - BaseOS
baseurl=https://vault.centos.org/centos/8/BaseOS/$basearch/os/
gpgcheck=0
enabled=1

[AppStream]
name=CentOS-8 - AppStream
baseurl=https://vault.centos.org/centos/8/AppStream/$basearch/os/
gpgcheck=0
enabled=1

[extras]
name=CentOS-8 - Extras
baseurl=https://vault.centos.org/centos/8/extras/$basearch/os/
gpgcheck=0
enabled=1'''
    
    run_cmd(ssh, f"cat > /etc/yum.repos.d/CentOS-Vault.repo << 'EOF'\n{repo}\nEOF")
    run_cmd(ssh, "dnf clean all")
    run_cmd(ssh, "dnf makecache", timeout=120)
    
    # 2. 安装基础工具
    print("\n" + "="*50)
    print("步骤2: 安装基础工具")
    print("="*50)
    run_cmd(ssh, "dnf install -y wget curl vim net-tools tar", timeout=300)
    
    # 3. 安装JDK
    print("\n" + "="*50)
    print("步骤3: 安装JDK")
    print("="*50)
    code, _, _ = run_cmd(ssh, "dnf install -y java-17-openjdk java-17-openjdk-devel", timeout=300)
    if code != 0:
        print("JDK 17安装失败，尝试JDK 11...")
        run_cmd(ssh, "dnf install -y java-11-openjdk java-11-openjdk-devel", timeout=300)
    
    run_cmd(ssh, "java -version 2>&1 | head -3")
    
    # 4. 安装MySQL
    print("\n" + "="*50)
    print("步骤4: 安装MySQL")
    print("="*50)
    run_cmd(ssh, "cd /tmp && rm -f mysql*.rpm")
    run_cmd(ssh, "wget -q https://dev.mysql.com/get/mysql80-community-release-el8-9.noarch.rpm -O mysql.rpm", timeout=120)
    run_cmd(ssh, "rpm -ivh mysql.rpm --nodeps 2>/dev/null || true")
    
    code, _, _ = run_cmd(ssh, "dnf module disable mysql -y 2>/dev/null || true")
    code, _, _ = run_cmd(ssh, "dnf install -y mysql-community-server --nobest", timeout=600)
    if code != 0:
        print("尝试安装系统MySQL...")
        run_cmd(ssh, "dnf install -y mysql-server", timeout=300)
    
    run_cmd(ssh, "systemctl start mysqld 2>/dev/null || systemctl start mysql")
    run_cmd(ssh, "systemctl enable mysqld 2>/dev/null || systemctl enable mysql")
    run_cmd(ssh, "systemctl status mysqld --no-pager | head -5 || systemctl status mysql --no-pager | head -5")
    
    # 5. 安装Nginx
    print("\n" + "="*50)
    print("步骤5: 安装Nginx")
    print("="*50)
    run_cmd(ssh, "dnf install -y nginx", timeout=300)
    run_cmd(ssh, "mkdir -p /etc/nginx/conf.d")
    run_cmd(ssh, "systemctl start nginx")
    run_cmd(ssh, "systemctl enable nginx")
    
    # 6. 配置防火墙
    print("\n" + "="*50)
    print("步骤6: 配置防火墙")
    print("="*50)
    run_cmd(ssh, "systemctl start firewalld 2>/dev/null || true")
    run_cmd(ssh, "firewall-cmd --permanent --add-port=22/tcp 2>/dev/null || true")
    run_cmd(ssh, "firewall-cmd --permanent --add-port=80/tcp 2>/dev/null || true")
    run_cmd(ssh, "firewall-cmd --permanent --add-port=443/tcp 2>/dev/null || true")
    run_cmd(ssh, "firewall-cmd --reload 2>/dev/null || true")
    
    # 7. 配置MySQL
    print("\n" + "="*50)
    print("步骤7: 配置MySQL")
    print("="*50)
    
    # 获取临时密码
    _, temp_pass, _ = run_cmd(ssh, "grep 'temporary password' /var/log/mysqld.log 2>/dev/null | awk '{print $NF}'", show_output=False)
    temp_pass = temp_pass.strip()
    
    if temp_pass:
        print(f"找到临时密码，修改root密码...")
        run_cmd(ssh, f"mysql --connect-expired-password -u root -p'{temp_pass}' -e \"ALTER USER 'root'@'localhost' IDENTIFIED BY 'Diandian@123'; FLUSH PRIVILEGES;\" 2>&1 || true")
    
    # 创建数据库
    run_cmd(ssh, "mysql -u root -p'Diandian@123' -e \"CREATE DATABASE IF NOT EXISTS bcrm DEFAULT CHARACTER SET utf8mb4;\" 2>&1 || echo 'DB exists'")
    run_cmd(ssh, "mysql -u root -p'Diandian@123' -e \"CREATE USER IF NOT EXISTS 'bcrm'@'localhost' IDENTIFIED BY 'BCRM@2024#App'; GRANT ALL ON bcrm.* TO 'bcrm'@'localhost'; FLUSH PRIVILEGES;\" 2>&1 || true")
    
    # 导入SQL
    print("\n导入数据库...")
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm < /opt/bcrm-deploy/sql/init.sql 2>&1 || echo 'import error'")
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm < /opt/bcrm-deploy/sql/fix_dict.sql 2>&1 || true")
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm < /opt/bcrm-deploy/sql/add_consume_care.sql 2>&1 || true")
    
    # 验证数据库
    run_cmd(ssh, "mysql -u bcrm -p'BCRM@2024#App' bcrm -e 'SELECT COUNT(*) as tables FROM information_schema.tables WHERE table_schema=\"bcrm\"'")
    
    # 8. 配置Nginx
    print("\n" + "="*50)
    print("步骤8: 配置Nginx")
    print("="*50)
    run_cmd(ssh, "cp /opt/bcrm-deploy/nginx/bcrm.conf /etc/nginx/conf.d/")
    run_cmd(ssh, "nginx -t && nginx -s reload")
    
    # 9. 启动服务
    print("\n" + "="*50)
    print("步骤9: 启动BCRM服务")
    print("="*50)
    run_cmd(ssh, "cp /opt/bcrm-deploy/systemd/bcrm.service /etc/systemd/system/")
    run_cmd(ssh, "systemctl daemon-reload")
    run_cmd(ssh, "systemctl stop bcrm 2>/dev/null || true")
    run_cmd(ssh, "systemctl start bcrm")
    run_cmd(ssh, "systemctl enable bcrm")
    
    time.sleep(10)
    
    # 10. 验证
    print("\n" + "="*50)
    print("步骤10: 验证部署")
    print("="*50)
    run_cmd(ssh, "systemctl status bcrm --no-pager | head -10")
    run_cmd(ssh, "ss -tuln | grep -E ':(80|8080)'")
    run_cmd(ssh, "curl -s http://localhost/ | head -5")
    run_cmd(ssh, "curl -s http://localhost:8080/api/auth/captcha | head -c 100")
    
    print("\n" + "="*50)
    print("部署完成!")
    print(f"访问地址: http://{SERVER_IP}/")
    print("默认账号: admin / admin123")
    print("="*50)
    
    ssh.close()

if __name__ == "__main__":
    main()
