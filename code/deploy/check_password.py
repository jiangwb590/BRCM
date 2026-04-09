#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

# 使用正确的BCrypt密码
bcrypt_password = "$2a$10$EqKcp1WFKVQISheBxmXNGexPR.i7QYXOJC.OFfQDT8iSaHuuPdlUW"

print("检查admin用户完整信息:")
stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' bcrm -e \"SELECT id, username, password, status, real_name FROM sys_user WHERE username='admin'\"")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n更新密码...")
# 使用引号转义
cmd = '''mysql -u bcrm -p'BCRM@2024#App' bcrm -e "UPDATE sys_user SET password='$2a$10$EqKcp1WFKVQISheBxmXNGexPR.i7QYXOJC.OFfQDT8iSaHuuPdlUW' WHERE username='admin'"'''
stdin, stdout, stderr = ssh.exec_command(cmd)
stdout.channel.recv_exit_status()

print("再次检查:")
stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' bcrm -e \"SELECT username, LEFT(password, 30) as pwd_start, LENGTH(password) as pwd_len FROM sys_user WHERE username='admin'\"")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n查看登录日志:")
stdin, stdout, stderr = ssh.exec_command("tail -20 /var/log/bcrm/bcrm.log | grep -i 'login\\|password\\|admin' || echo 'No login logs'")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()