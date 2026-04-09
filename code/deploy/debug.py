#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("查看最新错误日志:")
stdin, stdout, stderr = ssh.exec_command("tail -80 /var/log/bcrm/bcrm.log 2>/dev/null | tail -50")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n测试数据库连接:")
stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' -e 'SELECT 1'")
print(stdout.read().decode('utf-8', errors='ignore'))
print(stderr.read().decode('utf-8', errors='ignore'))

ssh.close()