#!/usr/bin/env python
import paramiko
import time
import os

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

# 检查本地jar是否存在
local_jar = r"D:\workspace\BCRM\code\back-end\target\bcrm-server-1.0.0.jar"
print(f"本地JAR大小: {os.path.getsize(local_jar)} bytes")

print("\n停止服务...")
stdin, stdout, stderr = ssh.exec_command("systemctl stop bcrm")
stdout.read()
time.sleep(2)

print("上传新版本JAR...")
sftp = ssh.open_sftp()
sftp.put(local_jar, "/opt/bcrm/bcrm-server-1.0.0.jar")
sftp.close()

# 验证上传
stdin, stdout, stderr = ssh.exec_command("ls -la /opt/bcrm/bcrm-server-1.0.0.jar")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n启动服务...")
stdin, stdout, stderr = ssh.exec_command("systemctl start bcrm")
stdout.read()
time.sleep(10)

stdin, stdout, stderr = ssh.exec_command("systemctl is-active bcrm")
status = stdout.read().decode().strip()
print(f"服务状态: {status}")

ssh.close()