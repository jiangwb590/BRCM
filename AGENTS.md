# BCRM 项目上下文

## 项目概述

BCRM（Beauty CRM）是一套面向美容院的客户关系管理系统，目前处于开发阶段。项目已从规划阶段进入全面开发阶段，包含完整的前后端代码。

**项目位置**：`D:\workspace\BCRM\`

## 目录结构

```
D:\workspace\BCRM\
├── 美容院CRM系统PRD.md     # 产品需求文档（Markdown格式）
├── 美容院CRM系统PRD.pdf    # PRD文档的PDF版本
├── 开发计划.md             # 详细开发计划和任务分解
├── AGENTS.md               # 本文件，项目上下文说明
├── prototype\              # 原型设计文件
├── error_log\              # 错误日志截图
├── *.jpg                   # 系统截图（各功能模块）
└── code\
    ├── back-end\           # 后端代码（Spring Boot）
    │   ├── pom.xml
    │   └── src\
    │       ├── main\
    │       │   ├── java\com\bcrm\
    │       │   │   ├── controller\      # 20个控制器
    │       │   │   ├── service\         # 业务服务层
    │       │   │   ├── mapper\          # MyBatis Mapper
    │       │   │   ├── entity\          # 22个实体类
    │       │   │   ├── dto\             # 数据传输对象
    │       │   │   ├── vo\              # 视图对象
    │       │   │   ├── config\          # 配置类
    │       │   │   ├── security\        # 安全相关
    │       │   │   ├── utils\           # 工具类
    │       │   │   ├── exception\       # 异常处理
    │       │   │   ├── aspect\          # AOP切面
    │       │   │   ├── annotation\      # 自定义注解
    │       │   │   └── task\            # 定时任务
    │       │   └── resources\
    │       │       ├── application.yml  # 主配置文件
    │       │       ├── db\              # 数据库脚本
    │       │       └── mapper\          # XML映射文件
    │       └── test\                    # 测试代码
    └── front-end\          # 前端代码（Vue 3）
        ├── package.json
        ├── vite.config.js
        └── src\
            ├── views\        # 17个页面模块
            ├── api\          # API请求模块
            ├── router\       # 路由配置
            ├── stores\       # Pinia状态管理
            ├── layouts\      # 布局组件
            ├── directive\    # 自定义指令
            ├── styles\       # 样式文件
            └── utils\        # 工具函数
```

## 产品定位

| 目标用户 | 规模 |
|---------|------|
| 单店美容院 | 客户量 500-5000 人 |
| 连锁美容院 | 多门店统一管理 |
| 美容工作室 | 个人或小型团队 |

## 核心功能模块

| 模块 | 后端Controller | 前端Views | 优先级 | 状态 |
|------|---------------|-----------|--------|------|
| 客户管理 | CustomerController | customer/ | P0 | 已实现 |
| 预约管理 | AppointmentController | appointment/ | P0 | 已实现 |
| 项目管理 | ServiceProjectController, ServicePackageController | project/ | P0 | 已实现 |
| 项目消耗 | ConsumeRecordController | consume/ | P0 | 已实现 |
| 充值管理 | RechargeController | recharge/ | P0 | 已实现 |
| 套餐购买 | - | purchase/ | P0 | 已实现 |
| 会员卡 | MemberCardController | member-card/ | P0 | 已实现 |
| 会员等级 | MemberLevelController | member-level/ | P1 | 已实现 |
| 进销存 | ProductController, StockInController, StockOutController | product/, stock/ | P1 | 已实现 |
| 客户维系 | CustomerCareController | care/ | P1 | 已实现 |
| 消息提醒 | MessageController | message/ | P1 | 已实现 |
| 数据分析 | DashboardController | dashboard/, analysis/ | P0 | 已实现 |
| 系统管理 | SysUserController, SysRoleController, SysMenuController, SysDictController, SysOperLogController | system/ | P0 | 已实现 |

## 用户角色

| 角色 | 权限范围 |
|------|---------|
| 系统管理员 | 系统运维、权限配置 |
| 店长 | 经营全览、数据分析、员工管理 |
| 美容师 | 客户服务、业绩查看 |
| 前台 | 预约登记、收银开单 |

## 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Java运行环境 |
| Spring Boot | 3.2.0 | Web框架 |
| MySQL | 8.0 | 数据库 |
| MyBatis-Plus | 3.5.5 | ORM框架 |
| Druid | 1.2.20 | 连接池 |
| JWT (jjwt) | 0.12.3 | Token认证 |
| Hutool | 5.8.24 | 工具类库 |
| Knife4j | 4.4.0 | API文档 |
| 腾讯云SMS | 3.1.888 | 短信服务（预留） |
| Apache POI | 5.2.5 | Excel处理 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Node.js | 18+ | 运行环境 |
| Vue | 3.4.21 | 前端框架 |
| Element Plus | 2.6.1 | UI组件库 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.3.0 | 路由管理 |
| Axios | 1.6.7 | HTTP请求 |
| ECharts | 5.5.0 | 图表库 |
| dayjs | 1.11.10 | 日期处理 |
| xlsx | 0.18.5 | Excel导入导出 |
| Vite | 5.1.4 | 构建工具 |
| Sass | 1.71.1 | CSS预处理 |

### 安全方案

- **认证**：JWT Token（有效期24小时）
- **授权**：RBAC权限模型
- **密码**：BCrypt加密
- **权限控制**：自定义权限注解 + 按钮级权限

## 运行配置

### 后端配置

| 配置项 | 值 |
|-------|-----|
| 服务端口 | 8080 |
| 上下文路径 | /api |
| 数据库 | bcrm |
| Druid监控 | /druid/* (admin/admin) |
| API文档 | /doc.html |

### 前端配置

| 配置项 | 值 |
|-------|-----|
| 开发端口 | 3000 |
| API代理 | localhost:8080 |

### 常用命令

```bash
# 后端
cd code/back-end
mvn spring-boot:run          # 启动开发服务器
mvn test                     # 运行测试

# 前端
cd code/front-end
npm run dev                  # 启动开发服务器
npm run build                # 构建生产版本
npm run preview              # 预览构建结果
```

## 数据库设计

### 系统表

| 表名 | 说明 |
|------|------|
| sys_user | 系统用户 |
| sys_role | 角色 |
| sys_user_role | 用户角色关联 |
| sys_menu | 菜单 |
| sys_role_menu | 角色菜单关联 |
| sys_dict | 数据字典 |
| sys_dict_item | 字典项 |
| sys_oper_log | 操作日志 |
| sys_login_log | 登录日志 |

### 业务表

| 表名 | 说明 |
|------|------|
| customer | 客户 |
| appointment | 预约 |
| service_project | 服务项目 |
| service_package | 套餐 |
| package_item | 套餐项目关联 |
| member_card | 会员卡（次卡/储值卡） |
| member_level | 会员等级 |
| package_purchase | 套餐购买记录 |
| recharge_record | 充值记录 |
| consume_record | 消费记录 |
| product | 产品 |
| stock_in_record | 入库记录 |
| stock_out_record | 出库记录 |
| customer_care | 客户关怀 |
| message | 消息 |

## 关键业务规则

### 客户分类

| 类型 | 规则 |
|------|------|
| 潜在客户 | 已登记但未消费 |
| 新客户 | 首次消费 30 天内 |
| 老客户 | 消费超过 3 次且最近消费在 90 天内 |
| VIP 客户 | 累计消费满 5000 元或单次消费满 2000 元 |
| 沉睡客户 | 90 天未消费的老客户 |

### 会员等级

| 等级 | 累计消费 | 折扣 |
|------|---------|------|
| 普通会员 | 0-999 元 | 9.5 折 |
| 银卡会员 | 1000-2999 元 | 9 折 |
| 金卡会员 | 3000-5999 元 | 8.5 折 |
| 钻石会员 | 6000 元以上 | 8 折 |

### 客户来源渠道

抖音、美团、小红书、微信、朋友介绍、自然进店、其他

## 开发阶段

| 阶段 | 状态 | 内容 |
|------|------|------|
| 第一阶段 | 已完成 | 架构搭建、数据库设计、登录认证 |
| 第二阶段 | 已完成 | 客户管理、项目管理、预约管理 |
| 第三阶段 | 已完成 | 项目消耗管理、进销存管理 |
| 第四阶段 | 已完成 | 会员管理、客户维系、消息提醒 |
| 第五阶段 | 已完成 | 数据分析、系统管理 |
| 第六阶段 | 进行中 | 系统测试、Bug 修复、文档完善 |

## 预留功能

以下功能已预留接口，暂未启用：
- 积分系统
- 短信通知（腾讯云SDK已集成，配置项 `sms.tencent.enabled=false`）
- 微信通知
- 多门店支持

## API 接口统计

| 模块 | Controller | 接口数量 |
|------|-----------|---------|
| 认证 | AuthController | 5 |
| 客户 | CustomerController | 10+ |
| 预约 | AppointmentController | 10+ |
| 项目 | ServiceProjectController, ServicePackageController | 15+ |
| 消费 | ConsumeRecordController, RechargeController | 10+ |
| 产品 | ProductController, StockInController, StockOutController | 15+ |
| 会员 | MemberCardController, MemberLevelController | 10+ |
| 关怀 | CustomerCareController | 8+ |
| 消息 | MessageController | 6+ |
| 数据 | DashboardController | 6+ |
| 系统 | SysUserController, SysRoleController, SysMenuController, SysDictController, SysOperLogController | 20+ |

## 文档版本

- **版本**：V2.0
- **更新日期**：2026 年 3 月 18 日
- **密级**：内部资料