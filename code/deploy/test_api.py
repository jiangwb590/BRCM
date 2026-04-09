#!/usr/bin/env python
import requests
import time

BASE_URL = "http://47.97.245.82/api"

print("等待服务完全启动...")
time.sleep(15)

print("\n测试Dashboard...")
resp = requests.post(f"{BASE_URL}/auth/login", json={"username": "admin", "password": "123456"}, timeout=30)
data = resp.json()
if data.get("code") != 200:
    print(f"登录失败: {data}")
    exit()

token = data["data"]["token"]
print("登录成功")

resp = requests.get(f"{BASE_URL}/dashboard/overview", headers={"Authorization": f"Bearer {token}"}, timeout=30)
result = resp.json()
if result.get("code") == 200:
    d = result["data"]
    print(f"\nDashboard数据:")
    print(f"  到店人次: {d.get('visitTimes', 'N/A')}")
    print(f"  到店人数: {d.get('visitCustomers', 'N/A')}")
    print(f"  老客到店: {d.get('oldCustomerVisits', 'N/A')}")
    print(f"  本月营收: {d.get('monthRevenue', 'N/A')}")
    print(f"  卡耗: {d.get('cardConsume', 'N/A')}")
else:
    print(f"错误: {result.get('msg')}")