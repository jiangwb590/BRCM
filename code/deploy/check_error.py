#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("="*50)
print("查看应用日志")
print("="*50)

stdin, stdout, stderr = ssh.exec_command("tail -100 /var/log/bcrm/bcrm.log 2>/dev/null || journalctl -u bcrm -n 100 --no-pager")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n" + "="*50)
print("手动运行JAR查看错误")
print("="*50)

stdin, stdout, stderr = ssh.exec_command("cd /opt/bcrm/backend && java -jar bcrm.jar --spring.profiles.active=prod 2>&1 | head -100", timeout=30)
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
