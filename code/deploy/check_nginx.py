#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("检查Nginx主配置:")
stdin, stdout, stderr = ssh.exec_command("cat /etc/nginx/nginx.conf | head -50")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查BCRM配置:")
stdin, stdout, stderr = ssh.exec_command("cat /etc/nginx/conf.d/bcrm.conf")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查前端文件位置:")
stdin, stdout, stderr = ssh.exec_command("ls -la /opt/bcrm/frontend/")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()