#!/usr/bin/env python
import paramiko
import subprocess

# 先在本地生成BCrypt密码
print("生成BCrypt密码...")
# 使用Java生成BCrypt密码
java_code = """
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class GenPass {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("admin123"));
    }
}
"""

# 直接使用已知的BCrypt密码
# admin123 的BCrypt加密结果（每次生成不同，但都有效）
bcrypt_password = "$2a$10$EqKcp1WFKVQISheBxmXNGexPR.i7QYXOJC.OFfQDT8iSaHuuPdlUW"

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print(f"更新admin密码为admin123...")
cmd = f"mysql -u bcrm -p'BCRM@2024#App' bcrm -e \"UPDATE sys_user SET password='{bcrypt_password}' WHERE username='admin'\""
stdin, stdout, stderr = ssh.exec_command(cmd)
stdout.channel.recv_exit_status()

print("验证密码已更新:")
stdin, stdout, stderr = ssh.exec_command("mysql -u bcrm -p'BCRM@2024#App' bcrm -e \"SELECT username, password FROM sys_user WHERE username='admin'\"")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n测试登录:")
stdin, stdout, stderr = ssh.exec_command("curl -s -X POST http://localhost/api/auth/login -H 'Content-Type: application/json' -d '{\"username\":\"admin\",\"password\":\"admin123\"}'")
result = stdout.read().decode('utf-8', errors='ignore')
print(result)

if '\"code\":200' in result or 'token' in result:
    print("\n✓ 登录成功！")
else:
    print("\n登录仍失败，检查AuthService...")

ssh.close()