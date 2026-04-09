#!/usr/bin/env python
import paramiko
import time
import json

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("上传新版本JAR...")
sftp = ssh.open_sftp()
sftp.put(r"D:\workspace\BCRM\code\back-end\target\bcrm-server-1.0.0.jar", "/opt/bcrm/bcrm-server-1.0.0.jar")
sftp.close()
print("上传完成")

print("\n重启服务...")
stdin, stdout, stderr = ssh.exec_command("systemctl restart bcrm")
stdout.read()

time.sleep(5)

stdin, stdout, stderr = ssh.exec_command("systemctl is-active bcrm")
status = stdout.read().decode().strip()
print(f"服务状态: {status}")

if status == "active":
    time.sleep(3)
    print("\n测试API...")
    stdin, stdout, stderr = ssh.exec_command('curl -s -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d \'{"username":"admin","password":"123456"}\'')
    login_result = stdout.read().decode()
    print(f"登录响应: {login_result[:100]}...")
    
    try:
        token = json.loads(login_result)["data"]["token"]
        cmd = 'curl -s http://localhost:8080/api/dashboard/overview -H "Authorization: Bearer ' + token + '"'
        stdin, stdout, stderr = ssh.exec_command(cmd)
        result = stdout.read().decode()
        data = json.loads(result)
        if data.get("code") == 200:
            d = data["data"]
            print(f"\nDashboard数据:")
            print(f"  到店人次: {d.get('visitTimes', 0)}")
            print(f"  到店人数: {d.get('visitCustomers', 0)}")
            print(f"  本月营收: {d.get('monthRevenue', 0)}")
    except Exception as e:
        print(f"解析错误: {e}")

ssh.close()
