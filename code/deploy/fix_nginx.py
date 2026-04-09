#!/usr/bin/env python
import paramiko

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)

print("修复Nginx配置...")

# 备份原配置
stdin, stdout, stderr = ssh.exec_command("cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak")

# 创建新的nginx.conf，移除默认server块
new_nginx_conf = '''# For more information on configuration, see:
#   * Official English Documentation: http://nginx.org/en/docs/

user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log;
pid /run/nginx.pid;

include /usr/share/nginx/modules/*.conf;

events {
    worker_connections 1024;
}

http {
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    sendfile            on;
    tcp_nopush          on;
    tcp_nodelay         on;
    keepalive_timeout   65;
    types_hash_max_size 2048;

    include             /etc/nginx/mime.types;
    default_type        application/octet-stream;

    # Load modular configuration files from the /etc/nginx/conf.d directory.
    include /etc/nginx/conf.d/*.conf;
}
'''

cmd = f"cat > /etc/nginx/nginx.conf << 'NGINXEOF'\n{new_nginx_conf}\nNGINXEOF"
stdin, stdout, stderr = ssh.exec_command(cmd)
stdout.channel.recv_exit_status()

print("测试Nginx配置...")
stdin, stdout, stderr = ssh.exec_command("/usr/sbin/nginx -t 2>&1")
output = stdout.read().decode('utf-8', errors='ignore')
print(output)

if "successful" in output:
    print("\n重载Nginx...")
    stdin, stdout, stderr = ssh.exec_command("/usr/sbin/nginx -s reload")
    stdout.channel.recv_exit_status()
    print("Nginx配置已更新")
else:
    print("配置有误，请检查")

print("\n测试前端:")
stdin, stdout, stderr = ssh.exec_command("curl -s http://localhost/ | head -10")
print(stdout.read().decode('utf-8', errors='ignore'))

print("\n测试API代理:")
stdin, stdout, stderr = ssh.exec_command("curl -s http://localhost/api/auth/captchaImage | head -c 300")
print(stdout.read().decode('utf-8', errors='ignore'))

ssh.close()
