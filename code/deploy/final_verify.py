#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("="*60)
print("BCRM 系统部署最终验证")
print("="*60)

print("\n【1. 服务状态检查】")
stdin, stdout, stderr = ssh.exec_command("systemctl is-active bcrm nginx mysqld")
result = stdout.read().decode().strip().split('\n')
print(f"   BCRM服务: {result[0]}")
print(f"   Nginx服务: {result[1]}")
print(f"   MySQL服务: {result[2]}")

print("\n【2. 端口监听检查】")
stdin, stdout, stderr = ssh.exec_command("ss -tuln | grep -E ':(80|8080|3306)' | awk '{print $5}'")
ports = stdout.read().decode().strip().split('\n')
for p in ports:
    print(f"   {p}")

print("\n【3. 前端访问测试】")
stdin, stdout, stderr = ssh.exec_command("curl -s -o /dev/null -w 'HTTP状态: %{http_code}' http://localhost/")
print(f"   前端页面: {stdout.read().decode().strip()}")

print("\n【4. 后端API测试】")
stdin, stdout, stderr = ssh.exec_command("curl -s http://localhost/api/auth/captchaImage | python3 -c 'import sys,json; d=json.load(sys.stdin); print(\"状态:\", d.get(\"code\"), \"消息:\", d.get(\"message\")[:20] if d.get(\"message\") else \"\")' 2>/dev/null || echo 'API返回非JSON'")
print(f"   验证码接口: {stdout.read().decode().strip()}")

print("\n【5. 数据库连接测试】")
stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' -e 'SELECT COUNT(*) as tables FROM information_schema.tables WHERE table_schema=\"bcrm\"' 2>/dev/null | tail -1")
print(f"   数据库表数量: {stdout.read().decode().strip()}")

print("\n【6. 公网访问测试】")
stdin, stdout, stderr = ssh.exec_command("curl -s -o /dev/null -w 'HTTP状态: %{http_code}' http://47.97.245.82/")
print(f"   公网访问: {stdout.read().decode().strip()}")

print("\n" + "="*60)
print("部署完成！")
print("="*60)
print("\n访问地址: http://47.97.245.82/")
print("默认账号: admin")
print("默认密码: admin123")
print("\n数据库信息:")
print("   主机: localhost:3306")
print("   数据库: bcrm")
print("   用户名: bcrm")
print("   密码: BCRM@2024#App")
print("="*60)

ssh.close()
