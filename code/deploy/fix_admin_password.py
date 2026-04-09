#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

# 正确的BCrypt密码（对应明文123456）
correct_password = "$2a$10$F1rx4TnCQm6p1AGnEEFKBOrTgsmrJomc9JUZjq0eRetmUbt.GY0Xe"

print("修复admin密码...")
# 使用转义方式更新
cmd = f'''mysql -u bcrm -p'BCRM@2024#App' bcrm << 'SQLEOF'
UPDATE sys_user SET password='{correct_password}' WHERE username='admin';
SELECT username, LENGTH(password) as pwd_len, LEFT(password, 20) as pwd_start FROM sys_user WHERE username='admin';
SQLEOF'''
stdin, stdout, stderr = ssh.exec_command(cmd)
print(stdout.read().decode('utf-8', errors='ignore'))
print(stderr.read().decode('utf-8', errors='ignore'))

print("\n测试登录（密码：123456）:")
stdin, stdout, stderr = ssh.exec_command("curl -s -X POST http://localhost/api/auth/login -H 'Content-Type: application/json' -d '{\"username\":\"admin\",\"password\":\"123456\"}'")
result = stdout.read().decode('utf-8', errors='ignore')
print(result[:300])

if '\"code\":200' in result or '\"token\"' in result:
    print("\n✓ 登录成功！")
else:
    print("\n仍失败，查看日志...")
    stdin, stdout, stderr = ssh.exec_command("tail -30 /var/log/bcrm/bcrm.log | grep -i 'error\\|exception'")
    print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()