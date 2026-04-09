#!/usr/bin/env python
import requests
import json

BASE_URL = "http://47.97.245.82/api"

# 登录
resp = requests.post(f"{BASE_URL}/auth/login", json={"username": "admin", "password": "123456"})
token = resp.json()["data"]["token"]
headers = {"Authorization": f"Bearer {token}"}

# 测试项目列表
print("测试项目列表接口:")
resp = requests.get(f"{BASE_URL}/project/page?pageNum=1&pageSize=10", headers=headers)
print(f"状态: {resp.status_code}")
data = resp.json()
print(f"结果: {json.dumps(data, indent=2, ensure_ascii=False)[:500]}")
