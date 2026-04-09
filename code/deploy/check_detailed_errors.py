#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("查看详细错误日志...")
print("="*60)

# 先触发错误
print("触发API请求...")
stdin, stdout, stderr = ssh.exec_command("curl -s http://localhost:8080/api/dashboard/overview -H 'Authorization: Bearer test' 2>&1")

# 查看详细日志
cmd = "tail -200 /var/log/bcrm/bcrm.log"
stdin, stdout, stderr = ssh.exec_command(cmd)
output = stdout.read().decode('utf-8', errors='ignore')

# 过滤出错误信息
lines = output.split('\n')
for i, line in enumerate(lines):
    if 'ERROR' in line or 'Exception' in line or "doesn't exist" in line or "Unknown column" in line:
        # 打印上下文
        start = max(0, i-2)
        end = min(len(lines), i+10)
        print('\n'.join(lines[start:end]))
        print("-"*40)

ssh.close()