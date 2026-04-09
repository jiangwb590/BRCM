#!/usr/bin/env python
import paramiko
import requests

# 触发错误
BASE_URL = "http://47.97.245.82/api"
resp = requests.post(f"{BASE_URL}/auth/login", json={"username": "admin", "password": "123456"})
token = resp.json()["data"]["token"]
headers = {"Authorization": f"Bearer {token}"}
resp = requests.get(f"{BASE_URL}/dashboard/overview", headers=headers)
print(f"Response: {resp.status_code}")
print(f"Body: {resp.text}")

# 检查日志
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("\n查看最新错误...")
cmd = "tail -300 /var/log/bcrm/bcrm.log | grep -B 2 -A 5 'ERROR'"
stdin, stdout, stderr = ssh.exec_command(cmd)
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()