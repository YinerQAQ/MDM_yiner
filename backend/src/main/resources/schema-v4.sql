USE mdm_db;

-- =============================================
-- 菜单表
-- =============================================
CREATE TABLE IF NOT EXISTS BASE_MENU (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    MENU_CODE VARCHAR(120),
    MENU_NAME VARCHAR(255),
    PARENT_ID VARCHAR(64),
    MENU_TYPE VARCHAR(36),
    PATH VARCHAR(255),
    COMPONENT VARCHAR(255),
    ICON VARCHAR(255),
    SORT_ORDER INT DEFAULT 0,
    STATUS VARCHAR(12),
    VISIBLE VARCHAR(12),
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 角色菜单关联表
-- =============================================
CREATE TABLE IF NOT EXISTS BASE_ROLE_MENU (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    ROLE_ID VARCHAR(64),
    MENU_ID VARCHAR(64),
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 字典表
-- =============================================
CREATE TABLE IF NOT EXISTS BASE_DICT (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    DICT_CODE VARCHAR(120) NOT NULL UNIQUE,
    DICT_NAME VARCHAR(255),
    DESCRIPTION VARCHAR(500),
    STATUS VARCHAR(12),
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 字典项表
-- =============================================
CREATE TABLE IF NOT EXISTS BASE_DICT_ITEM (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    DICT_ID VARCHAR(64),
    DICT_CODE VARCHAR(120),
    ITEM_VALUE VARCHAR(255),
    ITEM_LABEL VARCHAR(255),
    SORT_ORDER INT DEFAULT 0,
    STATUS VARCHAR(12),
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 用户组单位关联表
-- =============================================
CREATE TABLE IF NOT EXISTS BASE_GROUP_ORG (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    GROUP_ID VARCHAR(64),
    ORG_ID VARCHAR(64),
    CASCADE_FLAG VARCHAR(12),
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 工作流节点表
-- =============================================
CREATE TABLE IF NOT EXISTS MDM_WORKFLOW_NODE (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    WORKFLOW_ID VARCHAR(64),
    NODE_CODE VARCHAR(120),
    NODE_NAME VARCHAR(255),
    NODE_TYPE VARCHAR(36),
    POSITION_X INT,
    POSITION_Y INT,
    ASSIGNEE_TYPE VARCHAR(36),
    ASSIGNEE_VALUE VARCHAR(255),
    AUTO_APPROVE_EXPR VARCHAR(500),
    TIMEOUT_HOURS INT,
    TIMEOUT_ACTION VARCHAR(36),
    SIGN_TYPE VARCHAR(36),
    SIGN_THRESHOLD INT,
    CLAIM_HOURS INT,
    REQUIRE_OPINION VARCHAR(12),
    REQUIRE_ATTACHMENT VARCHAR(12),
    CALLBACK_SQL TEXT,
    SORT_ORDER INT DEFAULT 0,
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 工作流任务表
-- =============================================
CREATE TABLE IF NOT EXISTS MDM_WORKFLOW_TASK (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    INSTANCE_ID VARCHAR(64),
    NODE_ID VARCHAR(64),
    NODE_NAME VARCHAR(255),
    ASSIGNEE_ID VARCHAR(64),
    ASSIGNEE_NAME VARCHAR(120),
    TASK_STATUS VARCHAR(36),
    OPINION TEXT,
    CLAIM_TIME DATETIME,
    COMPLETE_TIME DATETIME,
    TRANSFER_TO_ID VARCHAR(64),
    TRANSFER_TO_NAME VARCHAR(120),
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 工作流边表
-- =============================================
CREATE TABLE IF NOT EXISTS MDM_WORKFLOW_EDGE (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    WORKFLOW_ID VARCHAR(64),
    SOURCE_NODE_ID VARCHAR(64),
    TARGET_NODE_ID VARCHAR(64),
    CONDITION_EXPR VARCHAR(500),
    IS_DEFAULT VARCHAR(12),
    LABEL VARCHAR(255),
    SORT_ORDER INT DEFAULT 0,
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- ESB信息系统表
-- =============================================
CREATE TABLE IF NOT EXISTS MDM_ESB_INFO_SYSTEM (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    SYSTEM_CODE VARCHAR(120),
    SYSTEM_NAME VARCHAR(255),
    SYSTEM_URL VARCHAR(500),
    AUTH_TYPE VARCHAR(36),
    AUTH_CONFIG TEXT,
    CONTACT_PERSON VARCHAR(120),
    CONTACT_PHONE VARCHAR(30),
    STATUS VARCHAR(12),
    REMARK VARCHAR(500),
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- ESB模型分发配置表
-- =============================================
CREATE TABLE IF NOT EXISTS MDM_ESB_MODEL_DIST (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    MODEL_ID VARCHAR(64),
    DIST_NAME VARCHAR(255),
    DIST_CODE VARCHAR(120),
    INTERFACE_TYPE VARCHAR(36),
    SYNC_TYPE VARCHAR(36),
    DATA_FORMAT VARCHAR(36),
    CRON_EXPR VARCHAR(120),
    BATCH_SIZE INT,
    RETRY_COUNT INT,
    RETRY_INTERVAL INT,
    TIMEOUT INT,
    TARGET_SYSTEM_ID VARCHAR(64),
    TARGET_URL VARCHAR(500),
    STATUS VARCHAR(12),
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- ESB模型分发内容映射表
-- =============================================
CREATE TABLE IF NOT EXISTS MDM_ESB_MODEL_DIST_CONTENT (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    DIST_ID VARCHAR(64),
    ATTRIBUTE_ID VARCHAR(64),
    ATTRIBUTE_CODE VARCHAR(120),
    ATTRIBUTE_NAME VARCHAR(255),
    FIELD_MAPPING VARCHAR(255),
    SORT_ORDER INT DEFAULT 0,
    INCLUDE_FLAG VARCHAR(12)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- ESB模型分发数据表
-- =============================================
CREATE TABLE IF NOT EXISTS MDM_ESB_MODEL_DIST_DATA (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    DIST_ID VARCHAR(64),
    DATA_ID VARCHAR(64),
    MODEL_CODE VARCHAR(120),
    DATA_CODE VARCHAR(120),
    SYNC_STATUS VARCHAR(36),
    RETRY_COUNT INT DEFAULT 0,
    LAST_SYNC_TIME DATETIME,
    ERROR_MSG TEXT,
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- ESB模型分发记录表
-- =============================================
CREATE TABLE IF NOT EXISTS MDM_ESB_MODEL_DIST_RECORD (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    DIST_ID VARCHAR(64),
    DATA_ID VARCHAR(64),
    BATCH_ID VARCHAR(64),
    REQUEST_BODY MEDIUMTEXT,
    RESPONSE_BODY MEDIUMTEXT,
    STATUS VARCHAR(36),
    ERROR_MSG TEXT,
    DURATION BIGINT,
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- ESB异常表
-- =============================================
CREATE TABLE IF NOT EXISTS MDM_ESB_EXCEPTION (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    DIST_ID VARCHAR(64),
    EXCEPTION_TYPE VARCHAR(120),
    EXCEPTION_MSG TEXT,
    NOTIFY_TYPE VARCHAR(36),
    NOTIFY_STATUS VARCHAR(36),
    HANDLE_STATUS VARCHAR(36),
    HANDLER_ID VARCHAR(64),
    HANDLE_TIME DATETIME,
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 系统参数表
-- =============================================
CREATE TABLE IF NOT EXISTS SYS_PARAM (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    PARAM_KEY VARCHAR(120) NOT NULL UNIQUE,
    PARAM_VALUE TEXT,
    PARAM_NAME VARCHAR(255),
    PARAM_TYPE VARCHAR(36),
    DESCRIPTION VARCHAR(500),
    STATUS VARCHAR(12),
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP,
    UPDATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 审计日志表
-- =============================================
CREATE TABLE IF NOT EXISTS SYS_AUDIT_LOG (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID VARCHAR(64),
    USERNAME VARCHAR(120),
    OPERATION VARCHAR(255),
    METHOD VARCHAR(255),
    PARAMS TEXT,
    IP VARCHAR(64),
    RESULT VARCHAR(36),
    DURATION BIGINT,
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 登录日志表
-- =============================================
CREATE TABLE IF NOT EXISTS SYS_LOGIN_LOG (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID VARCHAR(64),
    USERNAME VARCHAR(120),
    IP VARCHAR(64),
    LOCATION VARCHAR(255),
    BROWSER VARCHAR(255),
    OS VARCHAR(255),
    STATUS VARCHAR(12),
    MESSAGE VARCHAR(500),
    CREATE_TIME DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 初始数据：菜单树
-- =============================================
INSERT INTO BASE_MENU (ID, MENU_CODE, MENU_NAME, PARENT_ID, MENU_TYPE, PATH, COMPONENT, ICON, SORT_ORDER, STATUS, VISIBLE) VALUES
('menu001', 'Dashboard', '仪表盘', NULL, '目录', '/dashboard', 'Layout', 'DashboardOutlined', 1, '启用', '1'),
('menu002', 'DataModel', '数据模型', NULL, '目录', '/model', 'Layout', 'DatabaseOutlined', 2, '启用', '1'),
('menu003', 'MainData', '主数据', NULL, '目录', '/data', 'Layout', 'TableOutlined', 3, '启用', '1'),
('menu004', 'Workflow', '工作流', NULL, '目录', '/workflow', 'Layout', 'ClusterOutlined', 4, '启用', '1'),
('menu005', 'ESB', '数据分发', NULL, '目录', '/esb', 'Layout', 'ShareAltOutlined', 5, '启用', '1'),
('menu006', 'System', '系统管理', NULL, '目录', '/system', 'Layout', 'SettingOutlined', 6, '启用', '1'),
('menu101', 'DashboardView', '概览', 'menu001', '菜单', '/dashboard', 'views/Dashboard', 'DashboardOutlined', 1, '启用', '1'),
('menu201', 'DataModelList', '模型管理', 'menu002', '菜单', '/model/list', 'views/DataModelList', 'DatabaseOutlined', 1, '启用', '1'),
('menu301', 'MainDataList', '数据管理', 'menu003', '菜单', '/data/list', 'views/MainDataList', 'TableOutlined', 1, '启用', '1'),
('menu401', 'WorkflowList', '流程管理', 'menu004', '菜单', '/workflow/list', 'views/WorkflowList', 'ClusterOutlined', 1, '启用', '1'),
('menu501', 'ESBSystem', '系统管理', 'menu005', '菜单', '/esb/system', 'views/EsbSystemList', 'ShareAltOutlined', 1, '启用', '1'),
('menu502', 'ESBDist', '分发配置', 'menu005', '菜单', '/esb/dist', 'views/EsbDistList', 'ShareAltOutlined', 2, '启用', '1'),
('menu601', 'UserMgmt', '用户管理', 'menu006', '菜单', '/system/user', 'views/UserMgmt', 'UserOutlined', 1, '启用', '1'),
('menu602', 'RoleMgmt', '角色管理', 'menu006', '菜单', '/system/role', 'views/RoleMgmt', 'SafetyOutlined', 2, '启用', '1'),
('menu603', 'OrgMgmt', '组织管理', 'menu006', '菜单', '/system/org', 'views/OrgMgmt', 'ApartmentOutlined', 3, '启用', '1'),
('menu604', 'DictMgmt', '字典管理', 'menu006', '菜单', '/system/dict', 'views/DictMgmt', 'BookOutlined', 4, '启用', '1'),
('menu605', 'ParamMgmt', '参数管理', 'menu006', '菜单', '/system/param', 'views/ParamMgmt', 'ToolOutlined', 5, '启用', '1'),
('menu606', 'LogMgmt', '日志管理', 'menu006', '菜单', '/system/log', 'views/LogMgmt', 'FileTextOutlined', 6, '启用', '1');

-- =============================================
-- 初始数据：常用字典
-- =============================================
INSERT INTO BASE_DICT (ID, DICT_CODE, DICT_NAME, DESCRIPTION, STATUS) VALUES
('dict001', 'sex', '性别', '用户性别字典', '启用'),
('dict002', 'status', '状态', '通用状态字典', '启用'),
('dict003', 'data_status', '数据状态', '主数据状态字典', '启用'),
('dict004', 'flow_status', '流程状态', '工作流状态字典', '启用'),
('dict005', 'sync_status', '同步状态', 'ESB同步状态字典', '启用'),
('dict006', 'menu_type', '菜单类型', '菜单类型字典', '启用'),
('dict007', 'auth_type', '认证类型', '系统认证类型字典', '启用');

INSERT INTO BASE_DICT_ITEM (ID, DICT_ID, DICT_CODE, ITEM_VALUE, ITEM_LABEL, SORT_ORDER, STATUS) VALUES
('di001', 'dict001', 'sex', 'male', '男', 1, '启用'),
('di002', 'dict001', 'sex', 'female', '女', 2, '启用'),
('di003', 'dict002', 'status', 'active', '启用', 1, '启用'),
('di004', 'dict002', 'status', 'inactive', '停用', 2, '启用'),
('di005', 'dict003', 'data_status', 'draft', '草稿', 1, '启用'),
('di006', 'dict003', 'data_status', 'submitted', '已提交', 2, '启用'),
('di007', 'dict003', 'data_status', 'approved', '审核通过', 3, '启用'),
('di008', 'dict003', 'data_status', 'rejected', '已驳回', 4, '启用'),
('di009', 'dict004', 'flow_status', 'pending', '待处理', 1, '启用'),
('di010', 'dict004', 'flow_status', 'processing', '处理中', 2, '启用'),
('di011', 'dict004', 'flow_status', 'completed', '已完成', 3, '启用'),
('di012', 'dict004', 'flow_status', 'terminated', '已终止', 4, '启用'),
('di013', 'dict005', 'sync_status', 'pending', '待同步', 1, '启用'),
('di014', 'dict005', 'sync_status', 'syncing', '同步中', 2, '启用'),
('di015', 'dict005', 'sync_status', 'success', '同步成功', 3, '启用'),
('di016', 'dict005', 'sync_status', 'failed', '同步失败', 4, '启用'),
('di017', 'dict006', 'menu_type', 'directory', '目录', 1, '启用'),
('di018', 'dict006', 'menu_type', 'menu', '菜单', 2, '启用'),
('di019', 'dict006', 'menu_type', 'button', '按钮', 3, '启用'),
('di020', 'dict007', 'auth_type', 'none', '无认证', 1, '启用'),
('di021', 'dict007', 'auth_type', 'basic', 'Basic认证', 2, '启用'),
('di022', 'dict007', 'auth_type', 'token', 'Token认证', 3, '启用'),
('di023', 'dict007', 'auth_type', 'oauth2', 'OAuth2认证', 4, '启用');

-- =============================================
-- 初始数据：系统默认参数
-- =============================================
INSERT INTO SYS_PARAM (ID, PARAM_KEY, PARAM_VALUE, PARAM_NAME, PARAM_TYPE, DESCRIPTION, STATUS) VALUES
('param001', 'sys.name', 'MDM主数据管理平台', '系统名称', 'STRING', '系统显示名称', '启用'),
('param002', 'sys.logo', '/logo.png', '系统Logo', 'STRING', '系统Logo路径', '启用'),
('param003', 'sys.copyright', '2026 MaiKe Technology', '版权信息', 'STRING', '页面底部版权信息', '启用'),
('param004', 'sys.pageSize', '20', '默认分页大小', 'NUMBER', '列表默认每页显示条数', '启用'),
('param005', 'sys.sessionTimeout', '30', '会话超时(分钟)', 'NUMBER', '用户登录会话超时时间', '启用'),
('param006', 'sys.passwordMinLength', '6', '密码最小长度', 'NUMBER', '用户密码最小长度要求', '启用'),
('param007', 'sys.uploadMaxSize', '10485760', '上传文件大小限制', 'NUMBER', '允许上传的最大文件大小（字节）', '启用'),
('param008', 'esb.retryMaxCount', '3', 'ESB最大重试次数', 'NUMBER', '数据分发失败时的最大重试次数', '启用'),
('param009', 'esb.batchSize', '100', 'ESB批量大小', 'NUMBER', '每次分发批量处理的数据条数', '启用'),
('param010', 'workflow.autoApprove', 'false', '工作流自动审批', 'BOOLEAN', '是否开启工作流自动审批', '启用'),
('param011', 'workflow.timeoutHours', '24', '工作流超时(小时)', 'NUMBER', '任务默认超时时间', '启用'),
('param012', 'audit.logEnabled', 'true', '审计日志开关', 'BOOLEAN', '是否开启审计日志记录', '启用');
