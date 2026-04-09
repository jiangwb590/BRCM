#!/bin/bash
# ========================================
# BCRM 数据库初始化脚本
# 使用方法：./init_database.sh
# ========================================

set -e

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="bcrm"
DB_USER="bcrm"
DB_PASS="BCRM@2024#App"

echo "=========================================="
echo "BCRM 数据库初始化脚本"
echo "=========================================="

# 检查MySQL服务状态
if ! systemctl is-active --quiet mysqld; then
    echo "启动MySQL服务..."
    systemctl start mysqld
fi

# 读取root密码
read -sp "请输入MySQL root密码: " MYSQL_ROOT_PASSWORD
echo ""

# 测试连接
echo "测试MySQL连接..."
mysql -u root -p"$MYSQL_ROOT_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1 || {
    echo "错误：无法连接MySQL，请检查密码"
    exit 1
}

echo "MySQL连接成功！"

# ----------------------------------------
# 创建数据库和用户
# ----------------------------------------
echo "创建数据库和用户..."
mysql -u root -p"$MYSQL_ROOT_PASSWORD" <<EOF
-- 创建数据库
CREATE DATABASE IF NOT EXISTS ${DB_NAME} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASS}';
CREATE USER IF NOT EXISTS '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASS}';

-- 授权
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'%';
FLUSH PRIVILEGES;

SELECT '数据库创建成功！' AS Result;
EOF

echo "数据库: $DB_NAME"
echo "用户名: $DB_USER"
echo "密码: $DB_PASS"

# ----------------------------------------
# 导入表结构
# ----------------------------------------
echo ""
echo "导入数据库表结构..."

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# 导入初始化SQL
if [ -f "$SCRIPT_DIR/sql/init.sql" ]; then
    mysql -u $DB_USER -p"$DB_PASS" $DB_NAME < "$SCRIPT_DIR/sql/init.sql"
    echo "表结构导入完成"
else
    echo "警告: 未找到 $SCRIPT_DIR/sql/init.sql"
fi

# 导入字典修复SQL
if [ -f "$SCRIPT_DIR/sql/fix_dict.sql" ]; then
    mysql -u $DB_USER -p"$DB_PASS" $DB_NAME < "$SCRIPT_DIR/sql/fix_dict.sql"
    echo "字典数据修复完成"
fi

# 导入消费关怀SQL
if [ -f "$SCRIPT_DIR/sql/add_consume_care.sql" ]; then
    mysql -u $DB_USER -p"$DB_PASS" $DB_NAME < "$SCRIPT_DIR/sql/add_consume_care.sql"
    echo "消费关怀功能配置完成"
fi

# ----------------------------------------
# 验证数据库
# ----------------------------------------
echo ""
echo "验证数据库表..."
TABLE_COUNT=$(mysql -u $DB_USER -p"$DB_PASS" $DB_NAME -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '$DB_NAME';")
echo "数据库 $DB_NAME 包含 $TABLE_COUNT 张表"

# 检查关键表
mysql -u $DB_USER -p"$DB_PASS" $DB_NAME -e "
SELECT 
    (SELECT COUNT(*) FROM sys_user) AS '用户数',
    (SELECT COUNT(*) FROM sys_role) AS '角色数',
    (SELECT COUNT(*) FROM sys_menu) AS '菜单数',
    (SELECT COUNT(*) FROM sys_dict) AS '字典数';
"

echo ""
echo "=========================================="
echo "数据库初始化完成！"
echo "=========================================="
echo ""
echo "数据库连接信息："
echo "  地址: $DB_HOST:$DB_PORT"
echo "  数据库: $DB_NAME"
echo "  用户名: $DB_USER"
echo "  密码: $DB_PASS"
