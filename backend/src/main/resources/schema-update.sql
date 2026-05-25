-- 数据库更新脚本：修复状态值和添加缺失字段
USE mdm_db;

-- 添加 MDM_MAIN_DATA 表的 JSON_DATA 列（如果不存在）
SET @dbname = DATABASE();
SET @tablename = 'MDM_MAIN_DATA';
SET @columnname = 'JSON_DATA';
SET @preparedStatement = (SELECT IF(
  (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
   WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @columnname) > 0,
  'SELECT 1',
  'ALTER TABLE MDM_MAIN_DATA ADD COLUMN JSON_DATA TEXT AFTER DATA_STATUS'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 修复 BASE_USER 状态值：active -> 启用
UPDATE BASE_USER SET STATUS = '启用' WHERE STATUS = 'active';

-- 修复 BASE_ORG 状态值：active -> 启用
UPDATE BASE_ORG SET STATUS = '启用' WHERE STATUS = 'active';

-- 修复 MDM_WORKFLOW 状态值：active -> 启用
UPDATE MDM_WORKFLOW SET STATUS = '启用' WHERE STATUS = 'active';

-- 修复 BASE_ROLE 状态值：active -> 启用
UPDATE BASE_ROLE SET STATUS = '启用' WHERE STATUS = 'active';

-- 修复 BASE_GROUP 状态值：active -> 启用
UPDATE BASE_GROUP SET STATUS = '启用' WHERE STATUS = 'active';

-- 修复 MDM_CODE_RULE 状态值：active -> 启用
UPDATE MDM_CODE_RULE SET STATUS = '启用' WHERE STATUS = 'active';

-- 修复 MDM_ESB_MODEL 状态值：active -> 启用
UPDATE MDM_ESB_MODEL SET STATUS = '启用' WHERE STATUS = 'active';