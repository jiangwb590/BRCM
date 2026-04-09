#!/usr/bin/env python
import paramiko
import time

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("检查JAR包内容...")
stdin, stdout, stderr = ssh.exec_command("unzip -l /opt/bcrm/bcrm-server-1.0.0.jar | grep 'DashboardServiceImpl'")
output = stdout.read().decode('utf-8', errors='ignore')
print(output)

print("\n检查服务运行状态...")
stdin, stdout, stderr = ssh.exec_command("ps aux | grep java | grep -v grep")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n查看最新日志...")
time.sleep(3)
stdin, stdout, stderr = ssh.exec_command("tail -50 /var/log/bcrm/bcrm.log | grep -E '(visitTimes|getVisitTimes|客流|ERROR)'")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()