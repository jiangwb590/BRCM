#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("查看服务器日志...")
stdin, stdout, stderr = ssh.exec_command("tail -100 /var/log/bcrm/bcrm.log | grep -A 3 'ERROR'")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n查看本月时间范围...")
stdin, stdout, stderr = ssh.exec_command("""mysql -u bcrm -p'BCRM@2024#App' -e "
SELECT NOW() as now_time;
SELECT '本月开始' as label, '2026-04-01 00:00:00' as start_time;
SELECT '本月结束' as label, DATE_FORMAT(NOW(), '%Y-%m-%d 23:59:59') as end_time;
SELECT '消费记录:' as label;
SELECT id, consume_time, amount, customer_id FROM bcrm.consume_record;
" 2>/dev/null""")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n直接执行统计查询...")
stdin, stdout, stderr = ssh.exec_command("""mysql -u bcrm -p'BCRM@2024#App' -e "
SELECT COUNT(*) as visit_times FROM bcrm.consume_record WHERE consume_time BETWEEN '2026-04-01 00:00:00' AND NOW();
SELECT COUNT(DISTINCT customer_id) as visit_customers FROM bcrm.consume_record WHERE consume_time BETWEEN '2026-04-01 00:00:00' AND NOW() AND customer_id IS NOT NULL;
" 2>/dev/null""")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
