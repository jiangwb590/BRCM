#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("检查验证码接口配置...")
stdin, stdout, stderr = ssh.exec_command("curl -s http://localhost:8080/api/auth/captchaImage 2>&1 | head -c 300")
print("直接访问后端验证码接口:")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查AuthController路由...")
stdin, stdout, stderr = ssh.exec_command("unzip -l /opt/bcrm/backend/bcrm.jar 2>/dev/null | grep -i AuthController")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
