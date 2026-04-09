#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("="*50)
print("验证部署状态")
print("="*50)

print("\n1. 检查服务状态:")
stdin, stdout, stderr = ssh.exec_command("systemctl is-active bcrm")
result = stdout.read().decode().strip()
print(f"   BCRM服务: {result}")

print("\n2. 检查端口:")
stdin, stdout, stderr = ssh.exec_command("ss -tuln | grep -E ':(80|8080|3306)'")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n3. 测试后端API:")
stdin, stdout, stderr = ssh.exec_command("curl -s http://localhost:8080/api/auth/captcha")
print(stdout.read().decode('utf-8', errors='ignore')[:300])

print("\n4. 测试前端:")
stdin, stdout, stderr = ssh.exec_command("curl -s -o /dev/null -w 'HTTP状态: %{http_code}\\n' http://localhost/")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n5. 检查前端文件:")
stdin, stdout, stderr = ssh.exec_command("ls -la /opt/bcrm/frontend/ | head -10")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n6. 检查Nginx配置:")
stdin, stdout, stderr = ssh.exec_command("nginx -t 2>&1")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n" + "="*50)
print("部署验证完成")
print("="*50)
print("\n访问地址: http://47.97.245.82/")
print("默认账号: admin / admin123")

ssh.close()