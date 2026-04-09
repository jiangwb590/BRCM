#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("查看admin用户完整信息:")
stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' bcrm -e \"SELECT id, username, password, real_name, status FROM sys_user WHERE username='admin'\"")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查密码长度:")
stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' bcrm -e \"SELECT username, LENGTH(password) as pwd_len FROM sys_user WHERE username='admin'\"")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()