#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("检查admin用户:")
stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' bcrm -e \"SELECT id, username, password, real_name, status FROM sys_user WHERE username='admin'\"")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查sys_dict数据:")
stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' bcrm -e \"SELECT * FROM sys_dict LIMIT 10\"")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查验证码相关配置:")
stdin, stdout, stderr = ssh.exec_command("curl -s http://localhost:8080/api/auth/captchaImage 2>&1 | head -c 300")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
