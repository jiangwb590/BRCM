#!/usr/bin/env python
import requests

BASE_URL = "http://47.97.245.82/api"
resp = requests.post(f"{BASE_URL}/auth/login", json={"username": "admin", "password": "123456"})
token = resp.json()["data"]["token"]
headers = {"Authorization": f"Bearer {token}"}

# 测试套餐列表
resp = requests.get(f"{BASE_URL}/package/page?pageNum=1&pageSize=10", headers=headers)
print(f"套餐列表: {resp.json()}")
