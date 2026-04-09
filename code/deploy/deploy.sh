#!/bin/bash
# ========================================
# BCRM 应用部署脚本
# 使用方法: ./deploy.sh [命令]
# 命令: install | update | start | stop | restart | status | logs
# ========================================

set -e

# 应用配置
APP_NAME="bcrm"
APP_HOME="/opt/bcrm"
BACKEND_HOME="$APP_HOME/backend"
FRONTEND_HOME="$APP_HOME/frontend"
LOG_HOME="/var/log/bcrm"
PID_FILE="$BACKEND_HOME/bcrm.pid"

# 数据库配置（从环境变量读取，或使用默认值）
export DB_HOST=${DB_HOST:-localhost}
export DB_PORT=${DB_PORT:-3306}
export DB_NAME=${DB_NAME:-bcrm}
export DB_USER=${DB_USER:-bcrm}
export DB_PASS=${DB_PASS:-BCRM@2024#App}
export JWT_SECRET=${JWT_SECRET:-BCRM-JWT-SECRET-KEY-2024-PRODUCTION-ENVIRONMENT}

# JVM参数
JVM_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
print_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ----------------------------------------
# 显示帮助信息
# ----------------------------------------
show_help() {
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  install   - 首次安装部署"
    echo "  update    - 更新应用（前后端）"
    echo "  start     - 启动应用"
    echo "  stop      - 停止应用"
    echo "  restart   - 重启应用"
    echo "  status    - 查看应用状态"
    echo "  logs      - 查看应用日志"
    echo ""
}

# ----------------------------------------
# 启动应用
# ----------------------------------------
start_app() {
    print_info "启动 BCRM 应用..."
    
    if [ -f "$PID_FILE" ] && kill -0 $(cat $PID_FILE) 2>/dev/null; then
        print_warn "应用已在运行中，PID: $(cat $PID_FILE)"
        return 0
    fi
    
    # 检查JAR文件
    JAR_FILE=$(find $BACKEND_HOME -name "bcrm-*.jar" -type f | head -n 1)
    if [ -z "$JAR_FILE" ]; then
        print_error "未找到JAR文件，请先执行 install 或 update"
        return 1
    fi
    
    # 启动应用
    nohup java $JVM_OPTS \
        -jar "$JAR_FILE" \
        --spring.profiles.active=prod \
        > "$LOG_HOME/bcrm.log" 2>&1 &
    
    echo $! > "$PID_FILE"
    
    # 等待启动
    sleep 5
    
    if kill -0 $(cat $PID_FILE) 2>/dev/null; then
        print_info "应用启动成功，PID: $(cat $PID_FILE)"
        
        # 检查端口
        sleep 3
        if netstat -tuln | grep -q ":8080"; then
            print_info "应用端口 8080 已监听"
        else
            print_warn "应用端口 8080 尚未监听，请检查日志"
        fi
    else
        print_error "应用启动失败，请检查日志"
        rm -f "$PID_FILE"
        return 1
    fi
}

# ----------------------------------------
# 停止应用
# ----------------------------------------
stop_app() {
    print_info "停止 BCRM 应用..."
    
    if [ ! -f "$PID_FILE" ]; then
        print_warn "应用未在运行"
        return 0
    fi
    
    PID=$(cat $PID_FILE)
    if kill -0 $PID 2>/dev/null; then
        kill $PID
        # 等待进程结束
        for i in {1..10}; do
            if ! kill -0 $PID 2>/dev/null; then
                break
            fi
            sleep 1
        done
        
        # 如果进程还在，强制终止
        if kill -0 $PID 2>/dev/null; then
            print_warn "正常停止失败，强制终止..."
            kill -9 $PID
        fi
        
        rm -f "$PID_FILE"
        print_info "应用已停止"
    else
        print_warn "应用进程已不存在"
        rm -f "$PID_FILE"
    fi
}

# ----------------------------------------
# 重启应用
# ----------------------------------------
restart_app() {
    stop_app
    sleep 2
    start_app
}

# ----------------------------------------
# 查看状态
# ----------------------------------------
show_status() {
    echo "=========================================="
    echo "BCRM 应用状态"
    echo "=========================================="
    
    # 检查后端进程
    if [ -f "$PID_FILE" ] && kill -0 $(cat $PID_FILE) 2>/dev/null; then
        PID=$(cat $PID_FILE)
        echo -e "后端服务: ${GREEN}运行中${NC} (PID: $PID)"
        
        # 显示进程信息
        ps -p $PID -o pid,vsz,rss,%cpu,%mem,etime --no-headers
    else
        echo -e "后端服务: ${RED}未运行${NC}"
    fi
    
    # 检查端口
    echo ""
    echo "端口监听:"
    netstat -tuln | grep -E ":(80|443|8080)" || echo "  无"
    
    # 检查Nginx
    echo ""
    if systemctl is-active --quiet nginx; then
        echo -e "Nginx: ${GREEN}运行中${NC}"
    else
        echo -e "Nginx: ${RED}未运行${NC}"
    fi
    
    # 检查MySQL
    echo ""
    if systemctl is-active --quiet mysqld; then
        echo -e "MySQL: ${GREEN}运行中${NC}"
    else
        echo -e "MySQL: ${RED}未运行${NC}"
    fi
}

# ----------------------------------------
# 查看日志
# ----------------------------------------
show_logs() {
    LOG_FILE="$LOG_HOME/bcrm.log"
    
    if [ ! -f "$LOG_FILE" ]; then
        print_error "日志文件不存在: $LOG_FILE"
        return 1
    fi
    
    print_info "显示最近100行日志 (Ctrl+C 退出)..."
    tail -n 100 -f "$LOG_FILE"
}

# ----------------------------------------
# 安装部署
# ----------------------------------------
install_app() {
    print_info "开始安装 BCRM 应用..."
    
    # 检查目录
    if [ ! -d "$BACKEND_HOME" ]; then
        print_error "后端目录不存在: $BACKEND_HOME"
        print_info "请确保已上传项目文件"
        return 1
    fi
    
    # 检查前端目录
    if [ ! -d "$FRONTEND_HOME" ] || [ ! -f "$FRONTEND_HOME/index.html" ]; then
        print_error "前端文件不存在: $FRONTEND_HOME"
        print_info "请确保已构建并上传前端文件"
        return 1
    fi
    
    # 检查JAR文件
    JAR_FILE=$(find $BACKEND_HOME -name "bcrm-*.jar" -type f | head -n 1)
    if [ -z "$JAR_FILE" ]; then
        print_error "未找到JAR文件"
        return 1
    fi
    
    print_info "JAR文件: $JAR_FILE"
    
    # 配置Nginx
    if [ -f "/etc/nginx/conf.d/bcrm.conf" ]; then
        print_info "Nginx配置已存在"
    else
        print_warn "请手动配置Nginx: cp nginx/bcrm.conf /etc/nginx/conf.d/"
    fi
    
    # 设置权限
    chown -R nginx:nginx $FRONTEND_HOME 2>/dev/null || true
    chmod -R 755 $FRONTEND_HOME
    
    # 启动应用
    start_app
    
    # 重载Nginx
    print_info "重载Nginx配置..."
    nginx -t && nginx -s reload
    
    print_info "安装完成！"
    print_info "访问地址: http://$(hostname -I | awk '{print $1}')/"
}

# ----------------------------------------
# 更新应用
# ----------------------------------------
update_app() {
    print_info "更新 BCRM 应用..."
    
    stop_app
    
    # 这里可以添加更新逻辑
    # 例如：从git拉取最新代码、解压新的安装包等
    
    start_app
    
    print_info "更新完成！"
}

# ----------------------------------------
# 主程序
# ----------------------------------------
case "${1:-help}" in
    install)  install_app ;;
    update)   update_app ;;
    start)    start_app ;;
    stop)     stop_app ;;
    restart)  restart_app ;;
    status)   show_status ;;
    logs)     show_logs ;;
    *)        show_help ;;
esac
