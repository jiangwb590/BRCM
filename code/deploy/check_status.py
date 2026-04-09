#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
检查BCRM部署状态
"""

import paramiko

SERVER_IP = "47.97.245.82"
SERVER_USER = "root"
SERVER_PASS = "Diandian@123"

def check_status():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(SERVER_IP, username=SERVER_USER, password=SERVER_PASS, timeout=30)
    
    def run(cmd):
        stdin, stdout, stderr = ssh.exec_command(cmd)
        print(stdout.read().decode('utf-8', errors='ignore'))
    
    print("="*50)
    print("检查Java...")
    print("="*50)
    run("java -version 2>&1 | head -3")
    
    print("\n" + "="*50)
    print("检查MySQL...")
    print("="*50)
    run("systemctl status mysqld --no-pager | head -5 || systemctl status mysql --no-pager | head -5")
    
    print("\n" + "="*50)
    print("检查Nginx...")
    print("="*50)
    run("systemctl status nginx --no-pager | head -5")
    
    print("\n" + "="*50)
    print("检查BCRM服务...")
    print("="*50)
    run("systemctl status bcrm --no-pager | head -10")
    
    print("\n" + "="*50)
    print("检查端口...")
    print("="*50)
    run("ss -tuln | grep -E ':(80|8080|3306)'")
    
    print("\n" + "="*50)
    print("检查数据库...")
    print("="*50)
    run("mysql -u bcrm -p'BCRM@2024#App' bcrm -e 'SELECT COUNT(*) as tables FROM information_schema.tables WHERE table_schema=\"bcrm\"' 2>/dev/null || echo 'Database not ready'")
    
    print("\n" + "="*50)
    print("查看最近日志...")
    print("="*50)
    run("tail -30 /var/log/bcrm/bcrm.log 2>/dev/null || journalctl -u bcrm -n 20 --no-pager 2>/dev/null || echo 'No logs yet'")
    
    ssh.close()

if __name__ == "__main__":
    check_status()
