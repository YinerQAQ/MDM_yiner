-- ===========================================================
-- 完善权限矩阵：补全菜单/按钮，重建角色-菜单关联
-- 任务 #32 (BASE_ROLE_MENU 需要显式 ID，使用 UUID())
-- ===========================================================

-- 1. 新增菜单（INSERT IGNORE，幂等）
INSERT IGNORE INTO BASE_MENU (ID, MENU_CODE, MENU_NAME, PARENT_ID, MENU_TYPE, PATH, COMPONENT, ICON, SORT_ORDER, STATUS, VISIBLE, PERMS) VALUES
('menu203', 'ModelAudit',     '模型审核',     'menu002', '菜单', '/model-audit',     'views/ModelAudit.vue',     'Stamp',    3, '正常', '显示', 'menu:modelAudit:view'),
('menu204', 'CategoryConfig', '分类配置',     'menu002', '菜单', '/category-config', 'views/CategoryConfig.vue', 'Files',    4, '正常', '显示', 'menu:categoryConfig:view'),
('menu404', 'DataReceive',    '数据接收',     'menu004', '菜单', '/data-receive',    'views/DataReceive.vue',    'Download', 4, '正常', '显示', 'menu:dataReceive:view'),
('menu405', 'QueryService',   '数据查询服务', 'menu004', '菜单', '/data-query-service','views/DataQueryService.vue','Search', 5, '正常', '显示', 'menu:dataQueryService:view'),
('menu511', 'SecurityLevel',  '密级配置',     'menu005', '菜单', '/security-level',  'views/SecurityLevel.vue',  'Lock',     11,'正常', '显示', 'menu:securityLevel:view'),
('menu512', 'SystemMonitor',  '系统监控',     'menu005', '菜单', '/system-monitor',  'views/SystemMonitor.vue',  'Monitor',  12,'正常', '显示', 'menu:systemMonitor:view');

-- 2. 新增按钮记录（INSERT IGNORE，幂等）
INSERT IGNORE INTO BASE_MENU (ID, MENU_CODE, MENU_NAME, PARENT_ID, MENU_TYPE, SORT_ORDER, STATUS, VISIBLE, PERMS) VALUES
('btn20301', 'ModelAuditApprove', '审核通过', 'menu203', '按钮', 1, '正常', '显示', 'btn:modelAudit:approve'),
('btn20302', 'ModelAuditReject',  '审核驳回', 'menu203', '按钮', 2, '正常', '显示', 'btn:modelAudit:reject'),
('btn40401', 'DataReceiveCreate', '创建接收配置', 'menu404', '按钮', 1, '正常', '显示', 'btn:dataReceive:create'),
('btn40402', 'DataReceiveToggle', '启停接收',     'menu404', '按钮', 2, '正常', '显示', 'btn:dataReceive:toggle'),
('btn40501', 'QueryServiceCreate', '创建查询服务', 'menu405', '按钮', 1, '正常', '显示', 'btn:queryService:create'),
('btn40502', 'QueryServiceToggle', '启停查询服务', 'menu405', '按钮', 2, '正常', '显示', 'btn:queryService:toggle'),
('btn50301', 'UserCreate',        '创建用户', 'menu503', '按钮', 1, '正常', '显示', 'btn:user:create'),
('btn50302', 'UserUpdate',        '编辑用户', 'menu503', '按钮', 2, '正常', '显示', 'btn:user:update'),
('btn50303', 'UserDelete',        '删除用户', 'menu503', '按钮', 3, '正常', '显示', 'btn:user:delete'),
('btn50304', 'UserEnable',        '启用用户', 'menu503', '按钮', 4, '正常', '显示', 'btn:user:enable'),
('btn50305', 'UserDisable',       '停用用户', 'menu503', '按钮', 5, '正常', '显示', 'btn:user:disable'),
('btn50306', 'UserResetPassword', '重置密码', 'menu503', '按钮', 6, '正常', '显示', 'btn:user:resetPassword'),
('btn50201', 'OrgCreate', '创建单位', 'menu502', '按钮', 1, '正常', '显示', 'btn:org:create'),
('btn50202', 'OrgUpdate', '编辑单位', 'menu502', '按钮', 2, '正常', '显示', 'btn:org:update'),
('btn50203', 'OrgDelete', '删除单位', 'menu502', '按钮', 3, '正常', '显示', 'btn:org:delete'),
('btn50204', 'OrgEnable', '启停单位', 'menu502', '按钮', 4, '正常', '显示', 'btn:org:enable'),
('btn50401', 'RoleCreate', '创建角色', 'menu504', '按钮', 1, '正常', '显示', 'btn:role:create'),
('btn50402', 'RoleUpdate', '编辑角色', 'menu504', '按钮', 2, '正常', '显示', 'btn:role:update'),
('btn50403', 'RoleDelete', '删除角色', 'menu504', '按钮', 3, '正常', '显示', 'btn:role:delete'),
('btn50404', 'RoleAssign', '角色分配', 'menu504', '按钮', 4, '正常', '显示', 'btn:role:assign'),
('btn50601', 'MenuCreate', '创建菜单', 'menu506', '按钮', 1, '正常', '显示', 'btn:menu:create'),
('btn50602', 'MenuUpdate', '编辑菜单', 'menu506', '按钮', 2, '正常', '显示', 'btn:menu:update'),
('btn50603', 'MenuDelete', '删除菜单', 'menu506', '按钮', 3, '正常', '显示', 'btn:menu:delete'),
('btn51101', 'SecurityLevelCreate', '创建密级', 'menu511', '按钮', 1, '正常', '显示', 'btn:securityLevel:create'),
('btn51102', 'SecurityLevelUpdate', '编辑密级', 'menu511', '按钮', 2, '正常', '显示', 'btn:securityLevel:update'),
('btn51103', 'SecurityLevelDelete', '删除密级', 'menu511', '按钮', 3, '正常', '显示', 'btn:securityLevel:delete');

-- ===========================================================
-- 3. 重建非 admin 角色的 BASE_ROLE_MENU 关联
-- ===========================================================
DELETE FROM BASE_ROLE_MENU WHERE ROLE_ID IN ('role002', 'role003', 'role004');

-- ROLE_DATA_ADMIN (role002) 数据管理员
INSERT INTO BASE_ROLE_MENU (ID, ROLE_ID, MENU_ID) VALUES
(UUID(), 'role002', 'menu001'), (UUID(), 'role002', 'menu002'), (UUID(), 'role002', 'menu003'), (UUID(), 'role002', 'menu004'), (UUID(), 'role002', 'menu005'),
(UUID(), 'role002', 'menu101'),
(UUID(), 'role002', 'menu201'), (UUID(), 'role002', 'menu202'), (UUID(), 'role002', 'menu203'), (UUID(), 'role002', 'menu204'),
(UUID(), 'role002', 'btn20101'), (UUID(), 'role002', 'btn20102'), (UUID(), 'role002', 'btn20103'), (UUID(), 'role002', 'btn20104'),
(UUID(), 'role002', 'btn20301'), (UUID(), 'role002', 'btn20302'),
(UUID(), 'role002', 'menu301'), (UUID(), 'role002', 'menu302'), (UUID(), 'role002', 'menu303'), (UUID(), 'role002', 'menu304'),
(UUID(), 'role002', 'btn30101'), (UUID(), 'role002', 'btn30102'), (UUID(), 'role002', 'btn30103'), (UUID(), 'role002', 'btn30104'),
(UUID(), 'role002', 'btn30105'), (UUID(), 'role002', 'btn30106'), (UUID(), 'role002', 'btn30107'), (UUID(), 'role002', 'btn30108'),
(UUID(), 'role002', 'btn30109'), (UUID(), 'role002', 'btn30110'), (UUID(), 'role002', 'btn30111'), (UUID(), 'role002', 'btn30112'),
(UUID(), 'role002', 'btn30301'), (UUID(), 'role002', 'btn30302'), (UUID(), 'role002', 'btn30303'), (UUID(), 'role002', 'btn30304'),
(UUID(), 'role002', 'menu401'), (UUID(), 'role002', 'menu402'), (UUID(), 'role002', 'menu403'), (UUID(), 'role002', 'menu404'), (UUID(), 'role002', 'menu405'),
(UUID(), 'role002', 'btn40101'), (UUID(), 'role002', 'btn40102'),
(UUID(), 'role002', 'btn40401'), (UUID(), 'role002', 'btn40402'),
(UUID(), 'role002', 'btn40501'), (UUID(), 'role002', 'btn40502'),
(UUID(), 'role002', 'menu501'),
(UUID(), 'role002', 'btn50101'), (UUID(), 'role002', 'btn50102'), (UUID(), 'role002', 'btn50103');

-- ROLE_AUDITOR (role003) 审核人
INSERT INTO BASE_ROLE_MENU (ID, ROLE_ID, MENU_ID) VALUES
(UUID(), 'role003', 'menu001'), (UUID(), 'role003', 'menu002'), (UUID(), 'role003', 'menu003'),
(UUID(), 'role003', 'menu101'),
(UUID(), 'role003', 'menu301'),
(UUID(), 'role003', 'menu303'),
(UUID(), 'role003', 'btn30301'), (UUID(), 'role003', 'btn30302'), (UUID(), 'role003', 'btn30303'), (UUID(), 'role003', 'btn30304'),
(UUID(), 'role003', 'menu304'),
(UUID(), 'role003', 'btn30109'),
(UUID(), 'role003', 'menu203'),
(UUID(), 'role003', 'btn20301'), (UUID(), 'role003', 'btn20302');

-- ROLE_APPLICANT (role004) 申请人
INSERT INTO BASE_ROLE_MENU (ID, ROLE_ID, MENU_ID) VALUES
(UUID(), 'role004', 'menu001'), (UUID(), 'role004', 'menu003'),
(UUID(), 'role004', 'menu101'),
(UUID(), 'role004', 'menu301'),
(UUID(), 'role004', 'menu302'),
(UUID(), 'role004', 'btn30101'), (UUID(), 'role004', 'btn30102'), (UUID(), 'role004', 'btn30104'), (UUID(), 'role004', 'btn30107');

-- ===========================================================
-- 4. admin (role001) 补齐新菜单/按钮关联（先 DELETE 同 MENU_ID 的旧记录避免重复，再 INSERT）
-- ===========================================================
DELETE FROM BASE_ROLE_MENU WHERE ROLE_ID='role001' AND MENU_ID IN (
  'menu203','menu204','menu404','menu405','menu511','menu512',
  'btn20301','btn20302',
  'btn40401','btn40402','btn40501','btn40502',
  'btn50301','btn50302','btn50303','btn50304','btn50305','btn50306',
  'btn50201','btn50202','btn50203','btn50204',
  'btn50401','btn50402','btn50403','btn50404',
  'btn50601','btn50602','btn50603',
  'btn51101','btn51102','btn51103'
);

INSERT INTO BASE_ROLE_MENU (ID, ROLE_ID, MENU_ID) VALUES
(UUID(),'role001','menu203'),(UUID(),'role001','menu204'),
(UUID(),'role001','menu404'),(UUID(),'role001','menu405'),
(UUID(),'role001','menu511'),(UUID(),'role001','menu512'),
(UUID(),'role001','btn20301'),(UUID(),'role001','btn20302'),
(UUID(),'role001','btn40401'),(UUID(),'role001','btn40402'),
(UUID(),'role001','btn40501'),(UUID(),'role001','btn40502'),
(UUID(),'role001','btn50301'),(UUID(),'role001','btn50302'),(UUID(),'role001','btn50303'),
(UUID(),'role001','btn50304'),(UUID(),'role001','btn50305'),(UUID(),'role001','btn50306'),
(UUID(),'role001','btn50201'),(UUID(),'role001','btn50202'),(UUID(),'role001','btn50203'),(UUID(),'role001','btn50204'),
(UUID(),'role001','btn50401'),(UUID(),'role001','btn50402'),(UUID(),'role001','btn50403'),(UUID(),'role001','btn50404'),
(UUID(),'role001','btn50601'),(UUID(),'role001','btn50602'),(UUID(),'role001','btn50603'),
(UUID(),'role001','btn51101'),(UUID(),'role001','btn51102'),(UUID(),'role001','btn51103');
