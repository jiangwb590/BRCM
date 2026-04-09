#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
BCRM 自动部署脚本 v2
修复CentOS 8软件源问题
"""

import paramiko
import os
import time
import sys

# 服务器配置
SERVER_IP = "47.97.245.82"
SERVER_USER = "root"
SERVER_PASS = "Diandian@123"

# 本地路径
LOCAL_BASE = r"D:\workspace\BCRM"
LOCAL_BACKEND_JAR = os.path.join(LOCAL_BASE, "code", "back-end", "target", "bcrm-server-1.0.0.jar")
LOCAL_FRONTEND_DIR = os.path.join(LOCAL_BASE, "code", "front-end", "dist")
LOCAL_DEPLOY_DIR = os.path.join(LOCAL_BASE, "code", "deploy")

# 远程路径
REMOTE_BASE = "/opt/bcrm"
REMOTE_BACKEND_DIR = f"{REMOTE_BASE}/backend"
REMOTE_FRONTEND_DIR = f"{REMOTE_BASE}/frontend"
REMOTE_LOG_DIR = "/var/log/bcrm"

class BCMSDeployer:
    def __init__(self):
        self.ssh = None
        self.sftp = None
        
    def connect(self):
        """连接服务器"""
        print(f"正在连接服务器 {SERVER_IP}...")
        self.ssh = paramiko.SSHClient()
        self.ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        self.ssh.connect(SERVER_IP, username=SERVER_USER, password=SERVER_PASS, timeout=30)
        self.sftp = self.ssh.open_sftp()
        print("服务器连接成功！")
        
    def disconnect(self):
        """断开连接"""
        if self.sftp:
            self.sftp.close()
        if self.ssh:
            self.ssh.close()
        print("已断开服务器连接")
        
    def run_command(self, command, show_output=True, timeout=300):
        """执行远程命令"""
        stdin, stdout, stderr = self.ssh.exec_command(command, timeout=timeout)
        exit_code = stdout.channel.recv_exit_status()
        output = stdout.read().decode('utf-8', errors='ignore')
        error = stderr.read().decode('utf-8', errors='ignore')
        
        if show_output:
            if output:
                print(output)
            if error and 'DEPRECATION' not in error:
                print(f"[STDERR] {error}")
        
        return exit_code, output, error
    
    def upload_file(self, local_path, remote_path):
        """上传单个文件"""
        print(f"  上传: {os.path.basename(local_path)} -> {remote_path}")
        self.sftp.put(local_path, remote_path)
        
    def upload_directory(self, local_dir, remote_dir):
        """上传整个目录"""
        print(f"\n上传目录: {local_dir} -> {remote_dir}")
        
        # 确保远程目录存在
        self.run_command(f"mkdir -p {remote_dir}", show_output=False)
        
        # 遍历本地目录
        for root, dirs, files in os.walk(local_dir):
            # 计算相对路径
            rel_path = os.path.relpath(root, local_dir)
            if rel_path == '.':
                remote_root = remote_dir
            else:
                remote_root = os.path.join(remote_dir, rel_path).replace('\\', '/')
            
            # 创建远程子目录
            for d in dirs:
                remote_subdir = os.path.join(remote_root, d).replace('\\', '/')
                self.run_command(f"mkdir -p {remote_subdir}", show_output=False)
            
            # 上传文件
            for f in files:
                local_file = os.path.join(root, f)
                remote_file = os.path.join(remote_root, f).replace('\\', '/')
                self.sftp.put(local_file, remote_file)
                
    def upload_deployment_files(self):
        """上传部署文件"""
        print("\n=== 上传部署文件 ===")
        
        # 1. 上传后端JAR
        print("\n上传后端JAR包...")
        jar_name = "bcrm.jar"
        self.upload_file(LOCAL_BACKEND_JAR, f"{REMOTE_BACKEND_DIR}/{jar_name}")
        
        # 2. 上传前端文件
        print("\n上传前端文件...")
        self.upload_directory(LOCAL_FRONTEND_DIR, REMOTE_FRONTEND_DIR)
        
        # 3. 上传部署脚本
        print("\n上传部署脚本...")
        self.upload_file(os.path.join(LOCAL_DEPLOY_DIR, "install_env.sh"), "/opt/bcrm-deploy/install_env.sh")
        self.upload_file(os.path.join(LOCAL_DEPLOY_DIR, "init_database.sh"), "/opt/bcrm-deploy/init_database.sh")
        self.upload_file(os.path.join(LOCAL_DEPLOY_DIR, "deploy.sh"), "/opt/bcrm-deploy/deploy.sh")
        
        # 4. 上传SQL文件
        print("\n上传SQL文件...")
        sql_dir = os.path.join(LOCAL_DEPLOY_DIR, "sql")
        if os.path.exists(sql_dir):
            for f in os.listdir(sql_dir):
                if f.endswith('.sql'):
                    self.upload_file(os.path.join(sql_dir, f), f"/opt/bcrm-deploy/sql/{f}")
                    print(f"    {f}")
        
        # 5. 上传Nginx配置
        print("\n上传Nginx配置...")
        self.upload_file(os.path.join(LOCAL_DEPLOY_DIR, "nginx", "bcrm.conf"), "/opt/bcrm-deploy/nginx/bcrm.conf")
        
        # 6. 上传systemd服务文件
        print("\n上传systemd服务文件...")
        self.upload_file(os.path.join(LOCAL_DEPLOY_DIR, "systemd", "bcrm.service"), "/opt/bcrm-deploy/systemd/bcrm.service")
        
        # 7. 设置执行权限
        print("\n设置脚本执行权限...")
        self.run_command("chmod +x /opt/bcrm-deploy/*.sh")
        
        print("\n文件上传完成！")
        
    def fix_centos_repo(self):
        """修复CentOS 8软件源"""
        print("\n=== 修复CentOS 8软件源 ===")
        
        # 备份原有源
        self.run_command("mkdir -p /etc/yum.repos.d/backup")
        self.run_command("mv /etc/yum.repos.d/*.repo /etc/yum.repos.d/backup/ 2>/dev/null || true", show_output=False)
        
        # 创建vault源
        repo_content = """[BaseOS]
name=CentOS-8 - BaseOS
baseurl=https://vault.centos.org/centos/8/BaseOS/$basearch/os/
gpgcheck=0
enabled=1

[AppStream]
name=CentOS-8 - AppStream
baseurl=https://vault.centos.org/centos/8/AppStream/$basearch/os/
gpgcheck=0
enabled=1

[extras]
name=CentOS-8 - Extras
baseurl=https://vault.centos.org/centos/8/extras/$basearch/os/
gpgcheck=0
enabled=1
"""
        
        # 写入新源配置
        cmd = f"cat > /etc/yum.repos.d/CentOS-Vault.repo << 'EOF'\n{repo_content}\nEOF"
        self.run_command(cmd, show_output=False)
        
        # 清理缓存
        self.run_command("dnf clean all")
        self.run_command("dnf makecache")
        
        print("软件源修复完成！")
        
    def install_environment(self):
        """安装服务器环境"""
        print("\n=== 安装服务器环境 ===")
        
        # 安装基础工具
        print("\n安装基础工具...")
        self.run_command("dnf install -y wget curl vim net-tools tar", timeout=180)
        
        # 安装JDK
        print("\n安装JDK...")
        exit_code, _, _ = self.run_command("dnf install -y java-17-openjdk java-17-openjdk-devel", timeout=180)
        if exit_code != 0:
            print("尝试安装JDK 11...")
            self.run_command("dnf install -y java-11-openjdk java-11-openjdk-devel", timeout=180)
        
        # 验证Java
        self.run_command("java -version")
        
        # 安装MySQL
        print("\n安装MySQL...")
        # 下载MySQL repo
        self.run_command("cd /tmp && rm -f mysql*.rpm && wget -q https://dev.mysql.com/get/mysql80-community-release-el8-9.noarch.rpm", timeout=60)
        self.run_command("rpm -ivh /tmp/mysql80-community-release-el8-9.noarch.rpm --nodeps || true", show_output=False)
        
        # 安装MySQL服务器
        exit_code, _, _ = self.run_command("dnf install -y mysql-community-server", timeout=300)
        if exit_code != 0:
            print("尝试使用系统MySQL...")
            self.run_command("dnf install -y mysql-server", timeout=180)
        
        # 启动MySQL
        self.run_command("systemctl start mysqld 2>/dev/null || systemctl start mysql", show_output=False)
        self.run_command("systemctl enable mysqld 2>/dev/null || systemctl enable mysql", show_output=False)
        
        # 安装Nginx
        print("\n安装Nginx...")
        self.run_command("dnf install -y nginx", timeout=180)
        self.run_command("mkdir -p /etc/nginx/conf.d", show_output=False)
        self.run_command("systemctl start nginx", show_output=False)
        self.run_command("systemctl enable nginx", show_output=False)
        
        # 配置防火墙
        print("\n配置防火墙...")
        self.run_command("systemctl start firewalld 2>/dev/null || true", show_output=False)
        self.run_command("firewall-cmd --permanent --add-port=22/tcp 2>/dev/null || true", show_output=False)
        self.run_command("firewall-cmd --permanent --add-port=80/tcp 2>/dev/null || true", show_output=False)
        self.run_command("firewall-cmd --permanent --add-port=443/tcp 2>/dev/null || true", show_output=False)
        self.run_command("firewall-cmd --reload 2>/dev/null || true", show_output=False)
        
        print("\n环境安装完成！")
        
    def init_database(self):
        """初始化数据库"""
        print("\n=== 初始化数据库 ===")
        
        # 获取MySQL临时密码
        _, temp_pass, _ = self.run_command("grep 'temporary password' /var/log/mysqld.log 2>/dev/null | awk '{print $NF}'", show_output=False)
        temp_pass = temp_pass.strip()
        
        if temp_pass:
            print(f"检测到MySQL临时密码")
            
            # 使用临时密码修改root密码
            alter_cmd = f"""mysql --connect-expired-password -u root -p'{temp_pass}' -e "
ALTER USER 'root'@'localhost' IDENTIFIED BY 'Diandian@123';
FLUSH PRIVILEGES;" 2>&1 || true"""
            self.run_command(alter_cmd)
        
        # 创建数据库和用户
        print("\n创建数据库和用户...")
        create_cmd = """mysql -u root -p'Diandian@123' -e "
CREATE DATABASE IF NOT EXISTS bcrm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'bcrm'@'localhost' IDENTIFIED BY 'BCRM@2024#App';
CREATE USER IF NOT EXISTS 'bcrm'@'%' IDENTIFIED BY 'BCRM@2024#App';
GRANT ALL PRIVILEGES ON bcrm.* TO 'bcrm'@'localhost';
GRANT ALL PRIVILEGES ON bcrm.* TO 'bcrm'@'%';
FLUSH PRIVILEGES;" 2>&1 || echo 'Database may already exist, continuing...'"""
        
        exit_code, output, _ = self.run_command(create_cmd)
        print(output)
        
        # 导入SQL文件
        print("\n导入数据库结构...")
        sql_files = [
            "/opt/bcrm-deploy/sql/init.sql",
            "/opt/bcrm-deploy/sql/fix_dict.sql",
            "/opt/bcrm-deploy/sql/add_consume_care.sql"
        ]
        
        for sql_file in sql_files:
            _, test_out, _ = self.run_command(f"test -f {sql_file} && echo 'exists'", show_output=False)
            if 'exists' in test_out:
                self.run_command(f"mysql -u bcrm -p'BCRM@2024#App' bcrm < {sql_file} 2>&1 || echo 'Import warning...'")
                print(f"  导入: {os.path.basename(sql_file)}")
        
        # 验证
        _, output, _ = self.run_command("mysql -u bcrm -p'BCRM@2024#App' bcrm -e 'SELECT COUNT(*) as tables FROM information_schema.tables WHERE table_schema=\"bcrm\"' 2>/dev/null", show_output=False)
        print(f"\n数据库表统计: {output.strip()}")
        
        print("\n数据库初始化完成！")
        
    def configure_nginx(self):
        """配置Nginx"""
        print("\n=== 配置Nginx ===")
        
        # 备份默认配置
        self.run_command("mv /etc/nginx/nginx.conf.default /etc/nginx/nginx.conf.bak 2>/dev/null || true", show_output=False)
        
        # 复制BCRM配置
        self.run_command("cp /opt/bcrm-deploy/nginx/bcrm.conf /etc/nginx/conf.d/")
        
        # 测试配置
        exit_code, output, _ = self.run_command("nginx -t 2>&1")
        
        if exit_code == 0:
            # 重载Nginx
            self.run_command("nginx -s reload 2>&1")
            print("Nginx配置完成！")
            return True
        else:
            print(f"Nginx配置有误: {output}")
            return False
        
    def setup_service(self):
        """设置系统服务"""
        print("\n=== 配置系统服务 ===")
        
        # 更新服务文件中的Java路径
        self.run_command("sed -i 's|/usr/bin/java|/usr/bin/java|g' /opt/bcrm-deploy/systemd/bcrm.service", show_output=False)
        
        # 复制服务文件
        self.run_command("cp /opt/bcrm-deploy/systemd/bcrm.service /etc/systemd/system/")
        
        # 重载systemd
        self.run_command("systemctl daemon-reload")
        
        # 重启服务
        print("启动BCRM服务...")
        self.run_command("systemctl stop bcrm 2>/dev/null || true")
        self.run_command("systemctl start bcrm")
        self.run_command("systemctl enable bcrm")
        
        # 等待服务启动
        time.sleep(10)
        
        # 检查状态
        _, output, _ = self.run_command("systemctl status bcrm --no-pager", show_output=False)
        
        if "active (running)" in output:
            print("服务启动成功！")
        else:
            print("服务状态:")
            print(output)
            
            # 查看日志
            print("\n查看启动日志:")
            self.run_command("tail -50 /var/log/bcrm/bcrm.log 2>/dev/null || journalctl -u bcrm -n 30 --no-pager")
            
    def verify_deployment(self):
        """验证部署"""
        print("\n=== 验证部署 ===")
        
        # 检查端口
        print("\n检查端口监听:")
        self.run_command("ss -tuln | grep -E ':(80|8080)' || netstat -tuln | grep -E ':(80|8080)'")
        
        # 测试后端API
        print("\n测试后端API:")
        self.run_command("curl -s http://localhost:8080/api/auth/captcha | head -c 200")
        
        # 测试前端
        print("\n\n测试前端页面:")
        _, output, _ = self.run_command("curl -s -o /dev/null -w '%{http_code}' http://localhost/", show_output=False)
        print(f"HTTP状态码: {output.strip()}")
        
        print(f"\n{'='*50}")
        print("部署完成！")
        print(f"{'='*50}")
        print(f"\n访问地址: http://{SERVER_IP}/")
        print(f"默认账号: admin / admin123")
        
    def deploy(self):
        """完整部署流程"""
        try:
            # 1. 连接服务器
            self.connect()
            
            # 2. 创建目录
            print("\n=== 创建远程目录 ===")
            self.run_command("mkdir -p /opt/bcrm/backend /opt/bcrm/frontend /var/log/bcrm /opt/bcrm-deploy/sql /opt/bcrm-deploy/nginx /opt/bcrm-deploy/systemd")
            print("目录创建完成")
            
            # 3. 上传文件
            self.upload_deployment_files()
            
            # 4. 修复软件源
            self.fix_centos_repo()
            
            # 5. 安装环境
            self.install_environment()
            
            # 6. 初始化数据库
            self.init_database()
            
            # 7. 配置Nginx
            self.configure_nginx()
            
            # 8. 启动服务
            self.setup_service()
            
            # 9. 验证
            self.verify_deployment()
            
        except Exception as e:
            print(f"\n错误: {e}")
            import traceback
            traceback.print_exc()
        finally:
            self.disconnect()

if __name__ == "__main__":
    deployer = BCMSDeployer()
    deployer.deploy()