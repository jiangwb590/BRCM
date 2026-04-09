#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("检查服务文件:")
stdin, stdout, stderr = ssh.exec_command("cat /etc/systemd/system/bcrm.service")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查JAR中的生产配置:")
stdin, stdout, stderr = ssh.exec_command("unzip -p /opt/bcrm/backend/bcrm.jar BOOT-INF/classes/application-prod.yml 2>/dev/null | head -30")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查默认配置:")
stdin, stdout, stderr = ssh.exec_command("unzip -p /opt/bcrm/backend/bcrm.jar BOOT-INF/classes/application.yml 2>/dev/null | head -40")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
