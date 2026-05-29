-- ============================================================
-- 修复非管理员用户访问问题：补全测试用户与角色菜单授权
-- 任务 #27：data_mgr / auditor / applicant 三个非 admin 角色
-- 设计原则：
--   1) 仅追加缺失数据，不删除/覆盖 admin 已有授权
--   2) 用户初始密码统一存为明文 "123456"，由 AuthServiceImpl
--      的明文兼容分支在首次登录时自动升级为 BCrypt
-- ============================================================
USE mdm_db;

-- -------------------------------------------------------------
-- 1. 补充测试用户：auditor（审核人）、applicant（申请人）
--    若已存在则跳过（INSERT IGNORE 依赖主键 ID 唯一约束）
-- -------------------------------------------------------------
INSERT IGNORE INTO BASE_USER
    (ID, USERNAME, PASSWORD, NICKNAME, SEX, ORGID, ORGNAME, EMAIL, PHONE, STATUS, SECURITY_LEVEL)
VALUES
    ('user002', 'auditor',   '123456', '审核员',  '男', 'ORG001', '集团总部', 'auditor@maike.com',   '13800000002', '启用', '内部'),
    ('user003', 'applicant', '123456', '申请人',  '男', 'ORG001', '集团总部', 'applicant@maike.com', '13800000003', '启用', '内部');

-- -------------------------------------------------------------
-- 2. 用户与角色关联
--    auditor   -> role003 (ROLE_AUDITOR)
--    applicant -> role004 (ROLE_APPLICANT)
-- -------------------------------------------------------------
INSERT IGNORE INTO BASE_USER_ROLE (ID, USER_ID, ROLE_ID) VALUES
    ('ur003', 'user002', 'role003'),
    ('ur004', 'user003', 'role004');

-- -------------------------------------------------------------
-- 3. 角色菜单授权补全
--    使用 INSERT IGNORE + 复合主键 ID 防止重复
--    DATA_ADMIN: 数据交换全部 + 流程管理 + 父目录 menu004/menu005
--    AUDITOR:    补 menu304(数据归档) 以满足"数据审核+查询+归档"
--    APPLICANT:  已具备 menu101+menu301+menu302+menu003，无需追加
-- -------------------------------------------------------------

-- ROLE_DATA_ADMIN 追加：数据交换目录 + 数据分发 + 分发监控 + 数据交换 + 管理中心目录 + 流程管理
INSERT IGNORE INTO BASE_ROLE_MENU (ID, ROLE_ID, MENU_ID) VALUES
    ('rm_da_004', 'role002', 'menu004'),
    ('rm_da_005', 'role002', 'menu005'),
    ('rm_da_401', 'role002', 'menu401'),
    ('rm_da_402', 'role002', 'menu402'),
    ('rm_da_403', 'role002', 'menu403'),
    ('rm_da_501', 'role002', 'menu501'),
    -- 数据分发/交换相关按钮
    ('rm_da_b401', 'role002', 'btn40101'),
    ('rm_da_b402', 'role002', 'btn40102'),
    -- 流程管理按钮
    ('rm_da_b501', 'role002', 'btn50101'),
    ('rm_da_b502', 'role002', 'btn50102'),
    ('rm_da_b503', 'role002', 'btn50103');

-- ROLE_AUDITOR 追加：数据归档菜单
INSERT IGNORE INTO BASE_ROLE_MENU (ID, ROLE_ID, MENU_ID) VALUES
    ('rm_au_304', 'role003', 'menu304');

-- ============================================================
-- 完成。验证 SQL（手动执行查看）：
--   SELECT u.USERNAME, r.ROLE_CODE FROM BASE_USER u
--     JOIN BASE_USER_ROLE ur ON u.ID=ur.USER_ID
--     JOIN BASE_ROLE r ON r.ID=ur.ROLE_ID;
--   SELECT r.ROLE_CODE, COUNT(*) AS perm_cnt FROM BASE_ROLE_MENU rm
--     JOIN BASE_ROLE r ON r.ID=rm.ROLE_ID GROUP BY r.ROLE_CODE;
-- ============================================================
