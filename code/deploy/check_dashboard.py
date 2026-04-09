#!/usr/bin/env python
import paramiko
import requests

# 先触发错误
BASE_URL = "http://47.97.245.82/api"
resp = requests.post(f"{BASE_URL}/auth/login", json={"username": "admin", "password": "123456"})
token = resp.json()["data"]["token"]
headers = {"Authorization": f"Bearer {token}"}
requests.get(f"{BASE_URL}/dashboard/overview", headers=headers)

# 然后检查日志
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("查看 Dashboard Overview 错误...")
cmd = "tail -100 /var/log/bcrm/bcrm.log | grep -A 10 'dashboard/overview'"
stdin, stdout, stderr = ssh.exec_command(cmd)
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
