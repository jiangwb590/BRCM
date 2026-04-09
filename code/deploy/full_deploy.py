#!/usr/bin/env python
import paramiko
import time
import os
import requests

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("停止服务...")
stdin, stdout, stderr = ssh.exec_command("systemctl stop bcrm")
stdout.read()
time.sleep(2)

# 上传后端JAR
print("\n上传后端JAR...")
local_jar = r"D:\workspace\BCRM\code\back-end\target\bcrm-server-1.0.0.jar"
sftp = ssh.open_sftp()
sftp.put(local_jar, "/opt/bcrm/backend/bcrm.jar")
sftp.close()
print("后端JAR上传完成")

# 上传前端文件
print("\n上传前端文件...")
local_dist = r"D:\workspace\BCRM\code\front-end\dist"
remote_dist = "/opt/bcrm/frontend"

# 创建目录并清空
stdin, stdout, stderr = ssh.exec_command(f"mkdir -p {remote_dist} && rm -rf {remote_dist}/*")
stdout.read()

# 上传前端文件
sftp = ssh.open_sftp()
count = 0
for root, dirs, files in os.walk(local_dist):
    # 创建子目录
    for d in dirs:
        local_dir = os.path.join(root, d)
        relative_dir = os.path.relpath(local_dir, local_dist)
        remote_dir = os.path.join(remote_dist, relative_dir).replace("\\", "/")
        try:
            sftp.stat(remote_dir)
        except:
            stdin, stdout, stderr = ssh.exec_command(f"mkdir -p {remote_dir}")
            stdout.read()
    # 上传文件
    for file in files:
        local_path = os.path.join(root, file)
        relative_path = os.path.relpath(local_path, local_dist)
        remote_path = os.path.join(remote_dist, relative_path).replace("\\", "/")
        sftp.put(local_path, remote_path)
        count += 1

sftp.close()
print(f"前端文件上传完成，共 {count} 个文件")

# 执行数据库更新SQL
print("\n执行数据库更新SQL...")
sql = """
ALTER TABLE consume_record ADD COLUMN product_id BIGINT DEFAULT NULL COMMENT '产品ID';
ALTER TABLE consume_record ADD COLUMN product_name VARCHAR(100) DEFAULT NULL COMMENT '产品名称';
ALTER TABLE consume_record ADD COLUMN quantity INT DEFAULT 1 COMMENT '购买数量';
"""
stdin, stdout, stderr = ssh.exec_command(f"mysql -u bcrm -p'BCRM@2024#App' bcrm -e \"{sql}\" 2>&1")
print(stdout.read().decode('utf-8', errors='ignore'))

# 启动服务
print("\n启动服务...")
stdin, stdout, stderr = ssh.exec_command("systemctl start bcrm")
stdout.read()
time.sleep(10)

stdin, stdout, stderr = ssh.exec_command("systemctl is-active bcrm")
status = stdout.read().decode().strip()
print(f"服务状态: {status}")

ssh.close()

# 测试API
print("\n测试API...")
BASE_URL = "http://47.97.245.82/api"
try:
    resp = requests.post(f"{BASE_URL}/auth/login", json={"username": "admin", "password": "123456"}, timeout=30)
    data = resp.json()
    if data.get("code") == 200:
        token = data["data"]["token"]
        print("登录成功")
        
        resp = requests.get(f"{BASE_URL}/dashboard/overview", headers={"Authorization": f"Bearer {token}"}, timeout=30)
        result = resp.json()
        if result.get("code") == 200:
            d = result["data"]
            print(f"\nDashboard数据:")
            print(f"  总业绩: {d.get('monthRevenue', 0)}")
            print(f"  营业收入: {d.get('totalRevenue', 0)}")
            print(f"  项目业绩: {d.get('projectRevenue', 0)}")
            print(f"  产品业绩: {d.get('productRevenue', 0)}")
            print(f"  卡业绩: {d.get('cardRevenue', 0)}")
            print(f"  到店人次: {d.get('visitTimes', 0)}")
            print(f"  到店人数: {d.get('visitCustomers', 0)}")
    else:
        print(f"登录失败: {data}")
except Exception as e:
    print(f"API测试错误: {e}")

print("\n部署完成!")