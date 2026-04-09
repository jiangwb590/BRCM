# BCRM 服务器部署指南

## 目录

1. [环境准备](#1-环境准备)
2. [本地构建](#2-本地构建)
3. [上传文件](#3-上传文件)
4. [安装环境](#4-安装环境)
5. [配置数据库](#5-配置数据库)
6. [部署应用](#6-部署应用)
7. [验证部署](#7-验证部署)
8. [常见问题](#8-常见问题)

---

## 1. 环境准备

### 服务器要求

| 项目 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2核 | 4核 |
| 内存 | 4GB | 8GB |
| 磁盘 | 40GB | 100GB SSD |
| 系统 | CentOS 8 | CentOS 8 |

### 需要的信息

- 服务器公网IP地址
- SSH登录用户名和密码
- (可选) 域名和SSL证书

---

## 2. 本地构建

### 2.1 构建后端

在本地 Windows 开发环境执行：

```bash
# 进入后端目录
cd D:\workspace\BCRM\code\back-end

# 编译打包（跳过测试）
mvn clean package -DskipTests

# 构建产物位于：
# target/bcrm-0.0.1-SNAPSHOT.jar
```

### 2.2 构建前端

```bash
# 进入前端目录
cd D:\workspace\BCRM\code\front-end

# 安装依赖（如果还没安装）
npm install

# 构建生产版本
npm run build

# 构建产物位于：
# dist/
```

### 2.3 准备部署文件

创建部署目录结构：

```
deploy/
├── install_env.sh          # 环境安装脚本
├── init_database.sh        # 数据库初始化脚本
├── deploy.sh               # 部署脚本
├── sql/
│   ├── init.sql            # 数据库表结构
│   ├── fix_dict.sql        # 字典修复
│   └── add_consume_care.sql # 消费关怀功能
├── nginx/
│   └── bcrm.conf           # Nginx配置
└── systemd/
    └── bcrm.service        # 系统服务配置
```

---

## 3. 上传文件

### 3.1 使用 SCP 上传（Windows PowerShell）

```powershell
# 设置服务器IP
$SERVER_IP = "your_server_ip"
$SERVER_USER = "root"

# 创建临时部署目录
$TEMP_DIR = "D:\workspace\BCRM\code\deploy"

# 上传部署脚本和配置
scp -r $TEMP_DIR ${SERVER_USER}@${SERVER_IP}:/opt/bcrm-deploy

# 上传SQL文件
scp D:\workspace\BCRM\code\back-end\src\main\resources\db\*.sql ${SERVER_USER}@${SERVER_IP}:/opt/bcrm-deploy/sql/

# 上传后端JAR包
scp D:\workspace\BCRM\code\back-end\target\bcrm-0.0.1-SNAPSHOT.jar ${SERVER_USER}@${SERVER_IP}:/opt/bcrm/backend/bcrm.jar

# 上传前端文件
scp -r D:\workspace\BCRM\code\front-end\dist\* ${SERVER_USER}@${SERVER_IP}:/opt/bcrm/frontend/
```

### 3.2 使用 FTP/SFTP 工具

推荐使用 FileZilla 或 WinSCP：

1. 连接服务器（使用SSH账号密码）
2. 上传文件到 `/opt/bcrm-deploy/`

---

## 4. 安装环境

### 4.1 SSH 连接服务器

```bash
ssh root@your_server_ip
```

### 4.2 执行环境安装脚本

```bash
# 添加执行权限
chmod +x /opt/bcrm-deploy/install_env.sh

# 执行安装
/opt/bcrm-deploy/install_env.sh
```

安装过程中会自动安装：
- JDK 17
- MySQL 8
- Node.js 18
- Nginx

**注意**：安装完成后会显示 MySQL 临时密码，请记录下来。

---

## 5. 配置数据库

### 5.1 修改 MySQL root 密码

```bash
# 使用临时密码登录MySQL
mysql -u root -p

# 在MySQL中执行（修改密码）
ALTER USER 'root'@'localhost' IDENTIFIED BY 'YourNewPassword123!';
FLUSH PRIVILEGES;
EXIT;
```

### 5.2 初始化数据库

```bash
# 进入部署目录
cd /opt/bcrm-deploy

# 执行数据库初始化脚本
chmod +x init_database.sh
./init_database.sh
```

按提示输入 MySQL root 密码，脚本会自动：
- 创建数据库 `bcrm`
- 创建用户 `bcrm`
- 导入表结构和初始数据

---

## 6. 部署应用

### 6.1 配置 Nginx

```bash
# 复制Nginx配置
cp /opt/bcrm-deploy/nginx/bcrm.conf /etc/nginx/conf.d/

# 测试配置
nginx -t

# 重载Nginx
nginx -s reload
```

### 6.2 配置系统服务

```bash
# 复制服务文件
cp /opt/bcrm-deploy/systemd/bcrm.service /etc/systemd/system/

# 重载systemd
systemctl daemon-reload

# 启动服务
systemctl start bcrm

# 设置开机自启
systemctl enable bcrm
```

### 6.3 或使用部署脚本

```bash
# 添加执行权限
chmod +x /opt/bcrm-deploy/deploy.sh

# 安装部署
/opt/bcrm-deploy/deploy.sh install
```

---

## 7. 验证部署

### 7.1 检查服务状态

```bash
# 查看后端服务状态
systemctl status bcrm

# 查看端口监听
netstat -tuln | grep -E ":(80|8080)"

# 查看应用日志
tail -f /var/log/bcrm/bcrm.log
```

### 7.2 测试访问

```bash
# 测试后端API
curl http://localhost:8080/api/auth/captcha

# 测试前端页面
curl http://localhost/
```

### 7.3 浏览器访问

打开浏览器，访问：
- `http://your_server_ip/` - 前端页面
- `http://your_server_ip/api/` - 后端API

默认管理员账号：
- 用户名：`admin`
- 密码：`admin123`

---

## 8. 常见问题

### Q1: 后端无法启动

```bash
# 查看详细日志
journalctl -u bcrm -n 100

# 检查端口占用
netstat -tuln | grep 8080

# 检查数据库连接
mysql -u bcrm -p'BCRM@2024#App' -e "SELECT 1;"
```

### Q2: 前端页面无法访问

```bash
# 检查Nginx状态
systemctl status nginx

# 检查Nginx配置
nginx -t

# 查看Nginx错误日志
tail -f /var/log/nginx/error.log
```

### Q3: API请求跨域

Nginx已配置反向代理，前端直接请求 `/api/` 即可，不需要跨域。

### Q4: 数据库连接失败

```bash
# 检查MySQL状态
systemctl status mysqld

# 检查用户权限
mysql -u root -p -e "SHOW GRANTS FOR 'bcrm'@'localhost';"
```

### Q5: 修改数据库密码

```bash
# 修改服务文件中的密码
vim /etc/systemd/system/bcrm.service

# 修改环境变量
Environment="DB_PASS=YourNewPassword"

# 重载并重启
systemctl daemon-reload
systemctl restart bcrm
```

---

## 安全建议

1. **修改默认密码**：登录后立即修改 admin 账号密码
2. **配置HTTPS**：申请SSL证书，启用HTTPS
3. **修改数据库密码**：使用强密码替换默认密码
4. **关闭调试端口**：生产环境关闭8080端口外网访问
5. **定期备份**：配置数据库自动备份

---

## 维护命令

```bash
# 启动服务
systemctl start bcrm

# 停止服务
systemctl stop bcrm

# 重启服务
systemctl restart bcrm

# 查看状态
systemctl status bcrm

# 查看日志
journalctl -u bcrm -f

# 或使用部署脚本
/opt/bcrm-deploy/deploy.sh start
/opt/bcrm-deploy/deploy.sh stop
/opt/bcrm-deploy/deploy.sh restart
/opt/bcrm-deploy/deploy.sh status
/opt/bcrm-deploy/deploy.sh logs
```
