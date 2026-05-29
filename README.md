# MDM 主数据管理系统

![Version](https://img.shields.io/badge/version-1.3.0-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.8-green)
![Java](https://img.shields.io/badge/JDK-21-orange)
![Vue](https://img.shields.io/badge/Vue-3.4-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-4479A1)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

企业级主数据管理平台，提供数据标准定义、全生命周期管理、可视化工作流、ESB 数据交换及统一管理中心五大核心能力。

## 功能模块概览

### 数据标准

| 功能 | 说明 |
|------|------|
| 模型设计器 | 可视化数据模型设计，支持属性定义与约束配置 |
| 编码规则引擎 | 多段编码规则（前缀/日期/流水号），自动生成编码 |
| 约束规则校验 | 属性唯一性、格式正则、取值范围等多维度约束 |

### 数据业务

| 功能 | 说明 |
|------|------|
| 全生命周期管理 | 申请 → 审核 → 发布 → 变更 → 归档，完整流程闭环 |
| 数据申请与审核 | 多角色协作，工作流驱动的审批机制 |
| 数据归档 | 历史数据归档与版本对比 |

### 数据交换

| 功能 | 说明 |
|------|------|
| ESB 系统注册 | 外部系统注册与管理 |
| 分发策略 | 同步/异步分发，支持重试与异常处理 |
| 分发监控 | 实时监控分发状态与异常告警 |

### 管理中心

| 功能 | 说明 |
|------|------|
| RBAC 权限体系 | 用户/角色/组织/群组，76 个权限码精细控制 |
| 菜单管理 | 树形层级菜单配置，支持全局搜索 |
| 数据字典 & 系统参数 | 灵活的字典项与参数配置 |
| 审计日志 | 全操作留痕，AOP 切面自动记录 |

### 数据安全

| 功能 | 说明 |
|------|------|
| 数据密级配置 | 多级密级分类与访问控制 |
| 安全加固 | SpEL 表达式安全评估、SQL 注入防护 |
| 登录态双校验 | Token 存在性 + 时效性双重验证 |

## 技术栈

### 后端

- **Java 21** + **Spring Boot 3.1.8**
- **MyBatis Plus 3.5.6** + **MySQL 8.0+**
- **Spring Security** + **JWT 0.12.x**
- **SpringDoc OpenAPI**（Swagger 自动文档）
- **AOP**（审计日志、数据权限切面）

### 前端

- **Vue 3.4** + **TypeScript** + **Vite 5**
- **Element Plus 2.5** + **Vue Router 4.3** + **Pinia 2.1**
- **Axios**（统一 HTTP 封装）

## 技术架构

```
┌─────────────────────────────────────────────────────┐
│                    前端 (Vue 3)                       │
│  Vue 3.4 + TypeScript + Vite 5 + Element Plus 2.5   │
│  Pinia + Vue Router + Axios + ECharts + Vue Flow     │
└───────────────────────┬─────────────────────────────┘
                        │ HTTP / REST API
┌───────────────────────┴─────────────────────────────┐
│                  后端 (Spring Boot)                    │
│  Spring Boot 3.1.8 + Java 21 + MyBatis Plus 3.5.6   │
│  Spring Security + JWT + SpringDoc OpenAPI + AOP      │
└───────────────────────┬─────────────────────────────┘
                        │ JDBC
┌───────────────────────┴─────────────────────────────┐
│                    MySQL 8.0+                         │
│                   37 张数据表                          │
└─────────────────────────────────────────────────────┘
```

## 项目结构

```
MDM_demo/
├── backend/                          # 后端服务
│   └── src/main/
│       ├── java/com/maike/mdm/
│       │   ├── controller/           # REST 控制器
│       │   ├── service/              # 业务逻辑层
│       │   ├── entity/               # 数据实体
│       │   ├── mapper/               # MyBatis Plus Mapper
│       │   ├── dto/                  # 数据传输对象
│       │   ├── common/               # 通用工具、注解、切面
│       │   └── config/               # 安全与框架配置
│       └── resources/
│           ├── application.yml       # 应用配置
│           └── schema-*.sql          # 数据库脚本
├── frontend/                         # 前端应用
│   └── src/
│       ├── views/                    # 页面组件
│       ├── api/                      # API 模块
│       ├── components/               # 公共组件
│       ├── stores/                   # Pinia 状态管理
│       ├── router/                   # 路由配置
│       ├── directives/               # 自定义指令（权限等）
│       └── styles/                   # 全局样式
├── docs/                             # 项目文档
└── init-db.ps1                       # 数据库初始化脚本
```

## 快速开始

### 环境要求

- JDK 21+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 数据库初始化

1. 创建 MySQL 数据库：
```sql
CREATE DATABASE mdm_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 按以下顺序执行 SQL 脚本（位于 `backend/src/main/resources/`）：

```sql
1. schema-init.sql      -- 基础表结构
2. schema-update.sql    -- 增量更新
3. schema-v2.sql        -- v2 新增表
4. schema-v3.sql        -- v3 新增表
5. schema-v4.sql        -- v4 新增表
6. schema-v5.sql        -- v5 权限矩阵
7. sql/permission-matrix.sql  -- 权限数据
```

3. 或使用 PowerShell 一键初始化：
```powershell
.\init-db.ps1
```

### 启动后端

修改 `backend/src/main/resources/application.yml` 中的数据库连接信息后：

```bash
cd backend
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`

### 默认账号

| 用户名    | 密码    | 角色     | 说明           |
| --------- | ------- | -------- | -------------- |
| admin     | admin   | 系统管理员 | 全部权限       |
| data_mgr  | 123456  | 数据管理员 | 数据管理与审核 |
| auditor   | 123456  | 审核人   | 数据审核       |
| applicant | 123456  | 申请人   | 数据申请       |

## API 文档

启动后端后，访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

基于 SpringDoc OpenAPI 自动生成，涵盖所有 REST 接口。

## 开发规范

- 后端遵循 Controller → Service → Mapper 三层架构
- Entity 使用 MyBatis Plus 注解映射数据库字段（`@TableField` 须精确匹配列名）
- 前端使用 Composition API + TypeScript 严格模式
- API 层统一通过 Axios 封装，响应拦截处理认证异常
- 数据库字段采用 `snake_case`，Java 采用 `camelCase`

## License

[MIT](LICENSE)
