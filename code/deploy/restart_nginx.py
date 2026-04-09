#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("检查Nginx配置文件加载:")
stdin, stdout, stderr = ssh.exec_command("nginx -T 2>&1 | grep -A5 'server_name'")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n重启Nginx服务:")
stdin, stdout, stderr = ssh.exec_command("systemctl restart nginx && systemctl status nginx --no-pager | head -10")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n清理浏览器缓存后测试:")
stdin, stdout, stderr = ssh.exec_command("curl -s -H 'Cache-Control: no-cache' http://localhost/ | head -15")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n直接访问index.html:")
stdin, stdout, stderr = ssh.exec_command("curl -s http://localhost/index.html | head -15")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n检查BCRM配置是否被加载:")
stdin, stdout, stderr = ssh.exec_command("ls -la /etc/nginx/conf.d/")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()