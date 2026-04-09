#!/bin/bash
# ========================================
# BCRM 服务器环境安装脚本
# 适用于 CentOS 8
# 使用方法：chmod +x install_env.sh && ./install_env.sh
# ========================================

set -e

echo "=========================================="
echo "BCRM 服务器环境安装脚本"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否为root用户
if [ "$EUID" -ne 0 ]; then
    print_error "请使用root用户或sudo执行此脚本"
    exit 1
fi

# ----------------------------------------
# 0. 修复CentOS 8软件源（CentOS 8已停止维护）
# ----------------------------------------
print_info "修复CentOS 8软件源..."

# 备份原有源配置
mkdir -p /etc/yum.repos.d/backup
mv /etc/yum.repos.d/*.repo /etc/yum.repos.d/backup/ 2>/dev/null || true

# 创建新的vault源配置
cat > /etc/yum.repos.d/CentOS-Vault.repo << 'EOF'
[BaseOS]
name=CentOS-8 - BaseOS
baseurl=https://vault.centos.org/centos/8/BaseOS/$basearch/os/
gpgcheck=1
enabled=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-centosofficial

[AppStream]
name=CentOS-8 - AppStream
baseurl=https://vault.centos.org/centos/8/AppStream/$basearch/os/
gpgcheck=1
enabled=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-centosofficial

[extras]
name=CentOS-8 - Extras
baseurl=https://vault.centos.org/centos/8/extras/$basearch/os/
gpgcheck=1
enabled=1
gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-centosofficial
EOF

# 清理缓存
dnf clean all
dnf makecache

print_info "软件源修复完成"

# ----------------------------------------
# 1. 更新系统
# ----------------------------------------
print_info "更新系统软件包..."
dnf update -y --nobest || dnf update -y

# ----------------------------------------
# 2. 安装常用工具
# ----------------------------------------
print_info "安装常用工具..."
dnf install -y wget curl vim git net-tools tar || true

# ----------------------------------------
# 3. 安装 JDK 17
# ----------------------------------------
print_info "安装 JDK 17..."
dnf install -y java-17-openjdk java-17-openjdk-devel || {
    # 如果系统源没有JDK17，尝试安装JDK11
    print_warn "JDK 17不可用，尝试安装JDK 11..."
    dnf install -y java-11-openjdk java-11-openjdk-devel
}

# 验证Java安装
java -version
print_info "JDK 安装完成"

# ----------------------------------------
# 4. 安装 MySQL 8
# ----------------------------------------
print_info "安装 MySQL 8..."

# 下载MySQL社区版rpm包
cd /tmp
wget -q https://dev.mysql.com/get/mysql80-community-release-el8-9.noarch.rpm || {
    print_warn "MySQL rpm下载失败，尝试备用地址..."
    curl -L -O https://dev.mysql.com/get/mysql80-community-release-el8-9.noarch.rpm
}

# 安装MySQL仓库配置
rpm --import https://repo.mysql.com/RPM-GPG-KEY-mysql-2023 2>/dev/null || true
rpm -ivh mysql80-community-release-el8-9.noarch.rpm --nodeps || true

# 安装MySQL服务器
dnf install -y mysql-community-server || {
    print_warn "MySQL安装失败，尝试使用系统自带的mysql..."
    dnf install -y mysql-server
}

# 启动 MySQL
systemctl start mysqld 2>/dev/null || systemctl start mysql
systemctl enable mysqld 2>/dev/null || systemctl enable mysql

# 获取临时密码
TEMP_PASSWORD=$(grep 'temporary password' /var/log/mysqld.log 2>/dev/null | awk '{print $NF}' || echo "")
if [ -n "$TEMP_PASSWORD" ]; then
    print_info "MySQL 临时密码: $TEMP_PASSWORD"
    print_warn "请记录此密码，稍后需要使用"
else
    print_info "MySQL 已安装（无临时密码，可能需要手动初始化）"
fi

# ----------------------------------------
# 5. 安装 Nginx
# ----------------------------------------
print_info "安装 Nginx..."
dnf install -y nginx

# 创建nginx配置目录
mkdir -p /etc/nginx/conf.d

# 启动 Nginx
systemctl start nginx
systemctl enable nginx

# 验证Nginx
nginx -v
print_info "Nginx 安装完成"

# ----------------------------------------
# 6. 配置防火墙
# ----------------------------------------
print_info "配置防火墙..."
systemctl start firewalld 2>/dev/null || true
systemctl enable firewalld 2>/dev/null || true

# 开放必要端口
firewall-cmd --permanent --add-port=22/tcp 2>/dev/null || true
firewall-cmd --permanent --add-port=80/tcp 2>/dev/null || true
firewall-cmd --permanent --add-port=443/tcp 2>/dev/null || true
firewall-cmd --permanent --add-port=8080/tcp 2>/dev/null || true

firewall-cmd --reload 2>/dev/null || true
print_info "防火墙配置完成"

# ----------------------------------------
# 7. 创建应用目录
# ----------------------------------------
print_info "创建应用目录..."
mkdir -p /opt/bcrm/{backend,frontend,logs,data}
mkdir -p /var/log/bcrm

print_info "目录结构创建完成"

# ----------------------------------------
# 8. 安装完成提示
# ----------------------------------------
echo ""
echo "=========================================="
echo "环境安装完成！"
echo "=========================================="
echo ""
echo "已安装软件版本："
echo "  - JDK: $(java -version 2>&1 | head -n 1)"
echo "  - MySQL: $(mysql --version 2>/dev/null || echo '已安装')"
echo "  - Nginx: $(nginx -v 2>&1)"
echo ""
if [ -n "$TEMP_PASSWORD" ]; then
    echo "MySQL临时密码: $TEMP_PASSWORD"
fi
echo ""
echo "下一步操作："
echo "  1. 配置MySQL root密码"
echo "  2. 初始化数据库"
echo "  3. 启动应用服务"
echo ""
