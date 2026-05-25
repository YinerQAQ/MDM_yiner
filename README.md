# MDM 主数据管理系统

企业级主数据管理系统（Master Data Management），基于 Spring Boot + Vue3 前后端分离架构，提供数据标准、数据业务、工作流引擎、数据交换与管理中心五大核心能力。

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 18+ | 运行环境 |
| Spring Boot | 3.1.8 | Web框架 |
| MyBatis Plus | 3.5.6 | ORM框架 |
| MySQL | 8.0+ | 数据库 |
| Spring Security | 6.x | 安全框架 |
| JWT (jjwt) | 0.12.5 | 认证令牌 |
| SpringDoc OpenAPI | 2.2.0 | API文档 |
| Lombok | - | 代码简化 |
| BCrypt | - | 密码加密 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.x | 前端框架 |
| TypeScript | 5.3.x | 类型安全 |
| Vite | 5.1.x | 构建工具 |
| Element Plus | 2.5.x | UI组件库 |
| Vue Router | 4.3.x | 路由管理 |
| Pinia | 2.3.x | 状态管理 |
| Axios | 1.6.x | HTTP请求 |
| ECharts | 5.5.x | 数据可视化 |

## 项目结构

```
MDM_demo/
├── backend/                        # 后端 Spring Boot 项目
│   ├── src/main/java/com/maike/mdm/
│   │   ├── controller/             # REST API 控制层
│   │   │   ├── AuthController      # 登录认证
│   │   │   ├── DataModelController # 数据模型管理
│   │   │   ├── MainDataController  # 主数据管理
│   │   │   ├── WorkflowController  # 工作流管理
│   │   │   ├── UserController      # 用户管理
│   │   │   └── OrgController       # 单位管理
│   │   ├── service/                # 业务逻辑层
│   │   ├── mapper/                 # MyBatis Mapper 数据访问层
│   │   ├── entity/                 # 数据库实体类
│   │   ├── dto/                    # 数据传输对象（Request/Response）
│   │   ├── config/                 # 配置类（SecurityConfig 等）
│   │   ├── common/                 # 通用工具（异常、响应、加密）
│   │   └── MdmSystemApplication    # 启动入口
│   ├── src/main/resources/
│   │   ├── application.yml         # 应用配置
│   │   ├── schema-init.sql         # 全新数据库初始化脚本
│   │   ├── schema-update.sql       # 已有数据库增量更新脚本
│   │   └── schema.sql              # 基础建表脚本
│   └── pom.xml                     # Maven 依赖配置
├── frontend/                       # 前端 Vue 项目
│   ├── src/
│   │   ├── views/                  # 页面组件
│   │   │   ├── Login.vue           # 登录页
│   │   │   ├── Dashboard.vue       # 仪表盘首页
│   │   │   ├── DataModels.vue      # 数据模型管理
│   │   │   ├── MainData.vue        # 主数据管理
│   │   │   ├── Workflows.vue       # 工作流管理
│   │   │   ├── Users.vue           # 用户管理
│   │   │   ├── Orgs.vue            # 单位管理
│   │   │   ├── DataExchange.vue    # 数据交换
│   │   │   └── Layout.vue          # 页面布局框架
│   │   ├── api/                    # API 接口封装
│   │   │   ├── axios.ts            # Axios 实例与拦截器
│   │   │   ├── auth.ts             # 认证接口
│   │   │   ├── models.ts           # 数据模型接口
│   │   │   ├── mainData.ts         # 主数据接口
│   │   │   ├── workflows.ts        # 工作流接口
│   │   │   ├── users.ts            # 用户接口
│   │   │   ├── orgs.ts             # 单位接口
│   │   │   └── types.ts            # TypeScript 类型定义
│   │   ├── stores/                 # Pinia 状态管理
│   │   ├── router/                 # Vue Router 路由配置
│   │   ├── App.vue                 # 根组件
│   │   └── main.ts                 # 入口文件
│   ├── vite.config.ts              # Vite 配置（含 API 代理）
│   ├── tsconfig.json               # TypeScript 配置
│   └── package.json                # 依赖配置
└── README.md
```

## 快速开始

### 1. 环境准备

确保本地已安装以下环境：

- **JDK 18+**（推荐 JDK 21）
- **Node.js 20+**（推荐 LTS 版本）
- **MySQL 8.0+**
- **Maven 3.8+**

### 2. 数据库初始化

**全新数据库**（首次部署）：
```sql
-- 1. 创建数据库
CREATE DATABASE mdm_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 执行初始化脚本
mysql -u root -p mdm_db < backend/src/main/resources/schema-init.sql
```

**已有数据库**（版本升级）：
```sql
-- 执行增量更新脚本，添加新字段并修复数据
mysql -u root -p mdm_db < backend/src/main/resources/schema-update.sql
```

> 默认数据库连接：`localhost:3306/mdm_db`，用户名 `root`，密码 `root`。
> 如需修改，请编辑 `backend/src/main/resources/application.yml`。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 `http://localhost:5173` 启动，API 请求通过 Vite 代理转发到后端 8080 端口。

### 5. 访问系统

- **系统地址**：http://localhost:5173
- **默认账号**：用户名 `admin`，密码 `admin`
- **API 文档**：http://localhost:8080/swagger-ui.html

> 首次登录后，明文密码会自动升级为 BCrypt 加密存储。

## 核心功能

### 数据标准
- 数据模型管理（创建、编辑、审核、发布）
- 编码规则与编码方案配置
- 属性约束规则管理

### 主数据业务
- 主数据增删改查
- 数据审核流程（提交 → 审核 → 通过/拒绝）
- 数据归档与版本管理
- JSON 格式扩展数据存储

### 工作流引擎
- 流程定义与状态管理
- 审核流转与任务处理
- 流程实例追踪

### 数据交换
- 数据分发策略配置
- 接口管理

### 管理中心
- 用户管理（增删改查、密码重置、状态启停）
- 单位管理（树形组织结构）
- 角色与权限配置
- 三员管理模式（系统管理员、安全管理员、审计管理员）

## 常见问题

### 后端启动失败 — 端口占用
```bash
# Windows：查找并释放 8080 端口
netstat -ano | findstr :8080
taskkill /PID <进程ID> /F
```

### 数据库连接失败
1. 确认 MySQL 服务已启动
2. 检查 `application.yml` 中的数据库地址、用户名、密码
3. 确认数据库 `mdm_db` 已创建

### 前端登录提示"网络异常"
1. 确认后端服务正常运行（访问 http://localhost:8080/api-docs 验证）
2. 确认前端 Vite 代理配置正确（`vite.config.ts` 中 proxy `/api` → `http://localhost:8080`）
3. 如前端端口不是 5173，检查 `SecurityConfig.java` 中 CORS 是否放行了对应端口

## 开发规范

### 后端架构分层
```
Controller（API入口）→ Service（业务逻辑）→ Mapper（数据访问）
```
- DTO/VO 与 Entity 分离，禁止直接返回数据库实体
- 统一使用 `ApiResponse<T>` 封装返回结果
- 异统一使用 `BusinessException` 抛出，由 `GlobalExceptionHandler` 统一捕获

### 前端规范
- API 调用统一通过 `api/` 目录封装，不直接在组件中写 Axios
- 使用 Pinia store 管理全局状态（如认证信息）
- TypeScript 类型定义集中在 `api/types.ts`

### 数据库规范
- 表名、字段名使用大写蛇形命名（如 `BASE_USER`、`SECURITY_LEVEL`）
- MyBatis Plus 实体类使用 `@TableField` 注解精确映射列名
- 状态字段使用语义化中文值（如 `启用`/`停用`，而非 `active`/`inactive`）

## License

MIT License