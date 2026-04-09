#!/usr/bin/env python
import requests
import json

BASE_URL = "http://47.97.245.82/api"

# 登录
resp = requests.post(f"{BASE_URL}/auth/login", json={"username": "admin", "password": "123456"})
token = resp.json()["data"]["token"]
headers = {"Authorization": f"Bearer {token}"}

print("测试 Dashboard Overview:")
resp = requests.get(f"{BASE_URL}/dashboard/overview", headers=headers)
print(json.dumps(resp.json(), indent=2, ensure_ascii=False))

print("\n测试 未读消息:")
resp = requests.get(f"{BASE_URL}/message/unread", headers=headers)
print(json.dumps(resp.json(), indent=2, ensure_ascii=False))
