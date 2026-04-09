#!/usr/bin/env python
import paramiko
import time
import os

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

# 启动服务
print("\n启动服务...")
stdin, stdout, stderr = ssh.exec_command("systemctl start bcrm")
stdout.read()
time.sleep(10)

stdin, stdout, stderr = ssh.exec_command("systemctl is-active bcrm")
status = stdout.read().decode().strip()
print(f"服务状态: {status}")

ssh.close()
print("\n部署完成!")
