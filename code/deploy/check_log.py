#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("查看完整日志...")
stdin, stdout, stderr = ssh.exec_command("tail -200 /var/log/bcrm/bcrm.log")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
