#!/usr/bin/env python
import paramiko
import time
import os

# 构建后端
print("=" * 60)
print("步骤1: 构建后端项目")
print("=" * 60)

# 数据库SQL
sql = """
-- 添加产品相关字段到 consume_record 表
ALTER TABLE consume_record ADD COLUMN IF NOT EXISTS product_id BIGINT DEFAULT NULL COMMENT '产品ID';
ALTER TABLE consume_record ADD COLUMN IF NOT EXISTS product_name VARCHAR(100) DEFAULT NULL COMMENT '产品名称';
ALTER TABLE consume_record ADD COLUMN IF NOT EXISTS quantity INT DEFAULT 1 COMMENT '购买数量';
"""

# 连接服务器
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("\n执行数据库更新...")
stdin, stdout, stderr = ssh.exec_command(f"""mysql -u bcrm -p'BCRM@2024#App' bcrm << 'EOF'
{sql}
EOF""")
print(stdout.read().decode('utf-8', errors='ignore'))
print(stderr.read().decode('utf-8', errors='ignore'))

print("\n停止服务...")
stdin, stdout, stderr = ssh.exec_command("systemctl stop bcrm")
stdout.read()
time.sleep(2)

print("上传后端JAR...")
sftp = ssh.open_sftp()
sftp.put(r"D:\workspace\BCRM\code\back-end\target\bcrm-server-1.0.0.jar", "/opt/bcrm/backend/bcrm.jar")
sftp.close()
print("后端JAR上传完成")

# 上传前端
print("\n上传前端文件...")
local_dist = r"D:\workspace\BCRM\code\front-end\dist"
remote_dist = "/opt/bcrm/frontend"

# 清空远程目录
stdin, stdout, stderr = ssh.exec_command(f"rm -rf {remote_dist}/*")
stdout.read()

# 上传前端文件
for root, dirs, files in os.walk(local_dist):
    for file in files:
        local_path = os.path.join(root, file)
        relative_path = os.path.relpath(local_path, local_dist)
        remote_path = f"{remote_dist}/{relative_path}".replace("\\", "/")
        
        # 创建目录
        remote_dir = os.path.dirname(remote_path)
        stdin, stdout, stderr = ssh.exec_command(f"mkdir -p {remote_dir}")
        stdout.read()
        
        sftp = ssh.open_sftp()
        sftp.put(local_path, remote_path)
        sftp.close()

print("前端文件上传完成")

print("\n启动服务...")
stdin, stdout, stderr = ssh.exec_command("systemctl start bcrm")
stdout.read()
time.sleep(10)

stdin, stdout, stderr = ssh.exec_command("systemctl is-active bcrm")
status = stdout.read().decode().strip()
print(f"服务状态: {status}")

ssh.close()
print("\n部署完成！")