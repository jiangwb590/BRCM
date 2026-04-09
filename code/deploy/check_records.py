#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("检查消费记录详情...")
stdin, stdout, stderr = ssh.exec_command("""mysql -u bcrm -p'BCRM@2024#App' -e "
SELECT * FROM bcrm.consume_record;
SELECT '本月时间范围:';
SELECT '2026-04-01 00:00:00' as start, NOW() as end;
SELECT '本月消费记录数:';
SELECT COUNT(*) FROM bcrm.consume_record WHERE consume_time BETWEEN '2026-04-01 00:00:00' AND NOW();
SELECT 'deleted=0的记录:';
SELECT COUNT(*) FROM bcrm.consume_record WHERE deleted = 0;
" 2>/dev/null""")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
