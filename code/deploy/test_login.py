#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("查看完整的错误日志:")
stdin, stdout, stderr = ssh.exec_command("tail -200 /var/log/bcrm/bcrm.log | grep -A5 'ERROR\\|Exception' | head -50")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n查看实际登录测试:")
stdin, stdout, stderr = ssh.exec_command("curl -s -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{\"username\":\"admin\",\"password\":\"admin123\"}'")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
