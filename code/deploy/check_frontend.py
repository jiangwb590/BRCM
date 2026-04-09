#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("检查前端目录结构:")
stdin, stdout, stderr = ssh.exec_command("find /opt/bcrm/frontend -type f | head -20")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查index.html内容:")
stdin, stdout, stderr = ssh.exec_command("cat /opt/bcrm/frontend/index.html")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查Nginx用户权限:")
stdin, stdout, stderr = ssh.exec_command("ls -la /opt/bcrm/")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查错误日志:")
stdin, stdout, stderr = ssh.exec_command("tail -20 /var/log/nginx/bcrm_error.log 2>/dev/null || echo 'No error log'")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
