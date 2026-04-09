#!/usr/bin/env python
"""
执行数据库修复SQL脚本
"""
import paramiko

def run_sql_fix():
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect("47.97.245.82", username="root", password="Diandian@123", timeout=30)
    
    print("="*60)
    print("执行数据库表结构修复")
    print("="*60)
    
    # 1. 上传SQL文件
    print("\n[1] 上传SQL修复脚本...")
    sftp = ssh.open_sftp()
    local_file = r"D:\workspace\BCRM\code\deploy\sql\fix_db_tables.sql"
    remote_file = "/tmp/fix_db_tables.sql"
    sftp.put(local_file, remote_file)
    sftp.close()
    print("SQL文件已上传到 /tmp/fix_db_tables.sql")
    
    # 2. 执行SQL
    print("\n[2] 执行SQL修复脚本...")
    sql_cmd = f"""mysql -u bcrm -p'BCRM@2024#App' bcrm < {remote_file}"""
    stdin, stdout, stderr = ssh.exec_command(sql_cmd, timeout=120)
    
    output = stdout.read().decode('utf-8', errors='ignore')
    error = stderr.read().decode('utf-8', errors='ignore')
    
    if output:
        print("输出:", output)
    if error and "Warning" not in error:
        print("错误:", error)
    
    # 3. 验证表是否创建成功
    print("\n[3] 验证表结构...")
    verify_cmd = """mysql -u bcrm -p'BCRM@2024#App' -e "
        USE bcrm;
        SHOW TABLES LIKE 'consume_record';
        SHOW TABLES LIKE 'customer_care';
        SHOW TABLES LIKE 'member_card';
        SHOW TABLES LIKE 'recharge_record';
        SHOW TABLES LIKE 'package_purchase';
        SHOW TABLES LIKE 'service_project';
        DESC customer;
        DESC appointment;
    " 2>/dev/null"""
    
    stdin, stdout, stderr = ssh.exec_command(verify_cmd)
    result = stdout.read().decode('utf-8', errors='ignore')
    print(result)
    
    # 4. 重启服务
    print("\n[4] 重启BCRM服务...")
    stdin, stdout, stderr = ssh.exec_command("systemctl restart bcrm")
    print(stdout.read().decode('utf-8', errors='ignore'))
    
    # 5. 检查服务状态
    import time
    time.sleep(5)
    print("\n[5] 检查服务状态...")
    stdin, stdout, stderr = ssh.exec_command("systemctl status bcrm --no-pager | head -20")
    print(stdout.read().decode('utf-8', errors='ignore'))
    
    ssh.close()
    print("\n" + "="*60)
    print("数据库修复完成！")
    print("="*60)

if __name__ == "__main__":
    run_sql_fix()
