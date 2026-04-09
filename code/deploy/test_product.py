#!/usr/bin/env python
import requests

BASE_URL = "http://47.97.245.82/api"
resp = requests.post(f"{BASE_URL}/auth/login", json={"username": "admin", "password": "123456"})
token = resp.json()["data"]["token"]
headers = {"Authorization": f"Bearer {token}"}

print("测试产品列表接口:")
resp = requests.get(f"{BASE_URL}/product/page?pageNum=1&pageSize=10", headers=headers)
print(f"状态码: {resp.status_code}")
print(f"响应: {resp.text[:300]}")
