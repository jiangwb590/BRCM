#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("后端服务日志位置:")
print("="*50)

print("\n【1. 应用日志文件】")
stdin, stdout, stderr = ssh.exec_command("ls -la /var/log/bcrm/")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n【2. 查看最近日志内容】")
stdin, stdout, stderr = ssh.exec_command("tail -20 /var/log/bcrm/bcrm.log")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n【3. systemd日志】")
stdin, stdout, stderr = ssh.exec_command("journalctl -u bcrm -n 10 --no-pager")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n【4. 服务配置中的日志路径】")
stdin, stdout, stderr = ssh.exec_command("grep -i log /etc/systemd/system/bcrm.service")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()