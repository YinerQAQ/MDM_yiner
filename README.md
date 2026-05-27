# MDM 主数据管理系统

![Version](https://img.shields.io/badge/version-2.0.0-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.8-green)
![Java](https://img.shields.io/badge/JDK-18-orange)
![Vue](https://img.shields.io/badge/Vue-3.4-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-4479A1)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

企业级主数据管理平台，提供数据标准定义、全生命周期管理、可视化工作流、ESB 数据交换及统一管理中心五大核心能力。

## 功能亮点

| 模块 | 核心能力 | 关键特性 |
|------|---------|---------|
| **数据标准管理** | 模型设计器、编码规则引擎、约束规则校验 | 可视化模型设计、多段编码规则、属性约束校验 |
| **数据业务管理** | 全生命周期管理 | 申请→审核→发布→变更→归档，版本对比 |
| **工作流引擎** | 可视化流程设计 | 多节点审批、转办/认领/会签、流程实例追踪 |
| **数据交换（ESB）** | 系统注册、分发策略 | 同步/异步分发、重试监控、异常处理 |
| **管理中心** | RBAC 权限体系 | 用户/角色/组织/群组、菜单管理、数据字典、审计日志、系统参数 |

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
│  Spring Boot 3.1.8 + Java 18 + MyBatis Plus 3.5.6   │
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
│       │   ├── controller/           # 15 个 REST 控制器
│       │   ├── service/              # 业务逻辑层
│       │   ├── entity/               # 35 个数据实体
│       │   ├── mapper/               # MyBatis Plus Mapper
│       │   ├── dto/                  # 数据传输对象
│       │   ├── common/               # 通用工具与常量
│       │   └── config/               # 安全与框架配置
│       └── resources/
│           ├── application.yml       # 应用配置
│           └── schema-*.sql          # 数据库脚本
├── frontend/                         # 前端应用
│   └── src/
│       ├── views/                    # 24 个页面组件
│       ├── api/                      # 15 个 API 模块
│       ├── components/               # 公共组件
│       ├── stores/                   # Pinia 状态管理
│       ├── router/                   # 路由配置
│       └── styles/                   # 全局样式
└── init-db.ps1                       # 数据库初始化脚本
```

## 快速开始

### 环境要求

- JDK 18+
- Node.js 18+
- MySQL 8.0+
- Maven 3.8+

### 数据库初始化

按以下顺序执行 SQL 脚本：

```sql
1. schema-init.sql      -- 基础表结构
2. schema-update.sql    -- 增量更新
3. schema-v2.sql        -- v2 新增表
4. schema-v3.sql        -- v3 新增表
5. schema-v4.sql        -- v4 新增表
```

脚本位于 `backend/src/main/resources/` 目录下。

### 启动后端

```bash
cd backend
mvn spring-boot:run
```

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 默认账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | admin | 系统管理员 |

## 数据库说明

系统共 **37** 张数据表，按业务领域分类：

| 分类 | 表名 | 说明 |
|------|------|------|
| **用户与权限** | base_user, base_role, base_menu, base_org, base_group, base_user_role, base_user_group, base_role_menu, base_group_org | RBAC 权限体系 |
| **数据标准** | mdm_data_model, mdm_model_attribute, mdm_model_constraint, mdm_code_rule, mdm_code_scheme, mdm_code_segment, mdm_code_record | 模型、编码、约束 |
| **主数据** | mdm_main_data, mdm_archive_apply, mdm_archive_data | 主数据与归档 |
| **工作流** | mdm_workflow, mdm_workflow_node, mdm_workflow_edge, mdm_workflow_instance, mdm_workflow_task | 流程定义与实例 |
| **ESB 交换** | mdm_esb_info_system, mdm_esb_model_dist, mdm_esb_model_dist_content, mdm_esb_model_dist_data, mdm_esb_model_dist_record, mdm_esb_exception | 分发与监控 |
| **系统管理** | sys_audit_log, sys_login_log, sys_param, base_dict, base_dict_item | 日志、参数、字典 |

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
