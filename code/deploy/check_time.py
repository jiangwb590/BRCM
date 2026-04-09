#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("检查服务器时间...")
stdin, stdout, stderr = ssh.exec_command("date")
print(f"服务器时间: {stdout.read().decode().strip()}")

print("\n检查数据库时间与消费记录...")
stdin, stdout, stderr = ssh.exec_command("""mysql -u bcrm -p'BCRM@2024#App' -e "
SELECT NOW() as db_time;
SELECT id, consume_no, consume_time FROM bcrm.consume_record ORDER BY id DESC LIMIT 3;
SELECT COUNT(*) as count FROM bcrm.consume_record WHERE consume_time BETWEEN '2026-04-01' AND NOW();
" 2>/dev/null""")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
