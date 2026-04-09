#!/usr/bin/env python
"""
测试API接口验证修复
"""
import requests
import json

BASE_URL = "http://47.97.245.82/api"

def login():
    """登录获取token"""
    resp = requests.post(f"{BASE_URL}/auth/login", json={
        "username": "admin",
        "password": "123456"
    })
    data = resp.json()
    if data.get("code") == 200:
        return data["data"]["token"]
    else:
        print(f"登录失败: {data}")
        return None

def test_api(token, name, url):
    """测试API"""
    headers = {"Authorization": f"Bearer {token}"}
    try:
        resp = requests.get(f"{BASE_URL}{url}", headers=headers, timeout=10)
        data = resp.json()
        status = "OK" if data.get("code") == 200 else f"ERROR: {data.get('msg', 'unknown')}"
        print(f"  {name}: {status}")
        return data.get("code") == 200
    except Exception as e:
        print(f"  {name}: ERROR - {e}")
        return False

def main():
    print("="*60)
    print("API 接口测试")
    print("="*60)
    
    print("\n[1] 登录测试...")
    token = login()
    if not token:
        print("登录失败，退出测试")
        return
    print("  登录成功!")
    
    print("\n[2] 核心接口测试...")
    results = []
    
    # Dashboard
    results.append(test_api(token, "Dashboard概览", "/dashboard/overview"))
    results.append(test_api(token, "客户来源统计", "/dashboard/customer-source"))
    results.append(test_api(token, "收入趋势", "/dashboard/revenue-trend"))
    
    # 客户关怀
    results.append(test_api(token, "今日关怀任务", "/customer-care/today"))
    
    # 预约
    results.append(test_api(token, "今日预约", "/appointment/date/2026-04-06"))
    
    # 客户列表
    results.append(test_api(token, "客户列表", "/customer/page?pageNum=1&pageSize=10"))
    
    # 消息
    results.append(test_api(token, "未读消息", "/message/unread"))
    
    print("\n" + "="*60)
    success = sum(results)
    total = len(results)
    print(f"测试结果: {success}/{total} 通过")
    print("="*60)

if __name__ == "__main__":
    main()
