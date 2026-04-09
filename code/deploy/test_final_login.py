#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("测试登录接口:")
stdin, stdout, stderr = ssh.exec_command("curl -s -X POST http://localhost/api/auth/login -H 'Content-Type: application/json' -d '{\"username\":\"admin\",\"password\":\"admin123\"}'")
result = stdout.read().decode('utf-8', errors='ignore')
print(result[:500])

# 检查是否成功
if '"code":200' in result or '"code": 200' in result:
    print("\n登录成功！")
else:
    print("\n登录失败，检查密码...")
    
    # 尝试重置密码
    print("\n重置admin密码...")
    # BCrypt加密的admin123
    new_password = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH" # admin123的BCrypt
    stdin, stdout, stderr = ssh.exec_command(f"mysql -u bcrm -p'BCRM@2024#App' bcrm -e \"UPDATE sys_user SET password='{new_password}' WHERE username='admin'\" 2>/dev/null")
    stdout.channel.recv_exit_status()
    
    print("重新测试登录:")
    stdin, stdout, stderr = ssh.exec_command("curl -s -X POST http://localhost/api/auth/login -H 'Content-Type: application/json' -d '{\"username\":\"admin\",\"password\":\"admin123\"}'")
    result2 = stdout.read().decode('utf-8', errors='ignore')
    print(result2[:500])

ssh.close()