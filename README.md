# MDM 主数据管理系统

基于 Spring Boot + Vue3 构建的企业级主数据管理系统。

## 技术栈

### 后端
- Java 21
- Spring Boot 3.2.x
- MyBatis Plus 3.5.x
- MySQL 8.0+
- JWT 0.12.x
- Spring Security
- SpringDoc OpenAPI

### 前端
- Vue 3.4.x
- TypeScript
- Vite 5.x
- Element Plus 2.5.x
- Vue Router 4.3.x
- Pinia 2.1.x
- Axios

## 功能模块

### 已实现模块
1. **数据标准模块**：数据模型管理、编码规则管理、约束规则管理
2. **数据业务模块**：数据查询、新增、变更、审核、归档、版本管理
3. **工作流引擎**：流程定义、审核流转、状态机管理
4. **数据交换模块**：数据分发、接口配置
5. **管理中心**：用户管理、单位管理、角色管理、基础配置

### 待扩展模块
- 数据质量检测与清洗
- 分级管控（多租户）
- 移动端支持

## 项目结构

```
MDM_demo/
├── backend/                    # 后端Spring Boot项目
│   ├── src/main/java/com/maike/mdm/
│   │   ├── controller/         # REST API控制层
│   │   ├── service/            # 业务逻辑层
│   │   ├── mapper/             # MyBatis Mapper
│   │   ├── entity/             # 数据库实体类
│   │   ├── dto/                # 数据传输对象
│   │   ├── config/             # 配置类
│   │   ├── common/             # 通用工具类
│   │   └── MdmSystemApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml     # 应用配置
│   │   └── schema.sql          # 数据库初始化脚本
│   └── pom.xml
├── frontend/                   # 前端Vue项目
│   ├── src/
│   │   ├── views/              # 页面组件
│   │   ├── api/                # API接口封装
│   │   ├── stores/             # Pinia状态管理
│   │   ├── router/             # 路由配置
│   │   ├── App.vue
│   │   └── main.ts
│   ├── index.html
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── package.json
└── README.md
```

## 快速开始

### 环境要求
- JDK 21+
- Node.js 20+
- MySQL 8.0+

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE mdm_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p mdm_db < backend/src/main/resources/schema.sql
```

### 后端启动

```bash
cd backend
mvn spring-boot:run
```

服务将在 `http://localhost:8080` 启动

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端将在 `http://localhost:5173` 启动

## 系统访问

### 登录信息
- **用户名**: admin
- **密码**: admin

### API文档
访问 `http://localhost:8080/swagger-ui.html` 查看API文档

## 核心功能

### 数据模型管理
- 模型创建/编辑/删除
- 模型审核流程
- 版本管理
- 属性配置

### 主数据管理
- 数据新增（手工填写/批量导入）
- 数据变更
- 审核流程（提交/审核通过/拒绝）
- 归档管理
- 历史版本对比

### 工作流管理
- 流程定义
- 流程设计器
- 审核任务处理
- 转办/认领功能

### 权限管理
- 用户管理（CRUD、状态管理、密码重置）
- 单位管理（树形结构）
- 角色管理
- 三员管理模式支持

## 开发规范

### 命名规范
- 类名：UpperCamelCase
- 方法/变量：lowerCamelCase
- 常量：UPPER_SNAKE_CASE
- 表名/字段：snake_case

### 架构分层
- Controller → Service → Mapper/DAO（单向调用）
- DTO/VO分离（禁止直接返回Entity）

### 安全规范
- JWT认证
- 参数化查询（防SQL注入）
- 属性脱敏/加密支持

## License

MIT License