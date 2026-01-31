-- ----------------------------
-- TDengine 备份恢复管理 - 菜单和权限配置
-- ----------------------------

-- 1. TDengine 备份管理 (一级目录，挂在运维管理 4047 下)
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4056, 'TDengine 备份', '', 1, 1, 4047, 'backup', 'fa:database', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 2. 备份记录管理 (二级菜单)
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4057, '备份记录', '', 2, 1, 4056, 'record', 'fa:file-archive-o', 'iot/backup/record/index', 'IoTBackupRecord', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 备份记录 - 按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4058, '备份记录查询', 'iot:backup:query', 3, 1, 4057, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4059, '创建备份', 'iot:backup:create', 3, 2, 4057, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4060, '删除备份', 'iot:backup:delete', 3, 3, 4057, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4061, '下载备份', 'iot:backup:download', 3, 4, 4057, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4062, '验证备份', 'iot:backup:validate', 3, 5, 4057, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4063, '备份记录导出', 'iot:backup:export', 3, 6, 4057, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 3. 备份配置管理 (二级菜单)
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4064, '备份配置', '', 2, 2, 4056, 'config', 'fa:cogs', 'iot/backup/config/index', 'IoTBackupConfig', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 备份配置 - 按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4065, '备份配置查询', 'iot:backup:config', 3, 1, 4064, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4066, '创建配置', 'iot:backup:config:create', 3, 2, 4064, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4067, '更新配置', 'iot:backup:config:update', 3, 3, 4064, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4068, '删除配置', 'iot:backup:config:delete', 3, 4, 4064, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4069, '启用/禁用配置', 'iot:backup:config:enable', 3, 5, 4064, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4070, '执行备份配置', 'iot:backup:config:execute', 3, 6, 4064, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 4. 恢复记录管理 (二级菜单)
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4071, '恢复记录', '', 2, 3, 4056, 'restore', 'fa:history', 'iot/backup/restore/index', 'IoTRestoreRecord', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- 恢复记录 - 按钮权限
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4072, '恢复记录查询', 'iot:restore:query', 3, 1, 4071, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4073, '执行恢复', 'iot:restore:execute', 3, 2, 4071, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (4074, '恢复记录导出', 'iot:restore:export', 3, 3, 4071, '', '', '', NULL, 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- ----------------------------
-- 说明
-- ----------------------------
-- 菜单ID范围：4056-4074
-- 父级菜单：4047 (运维管理)
--
-- 菜单结构：
-- 4056 TDengine 备份 (目录)
--   ├─ 4057 备份记录 (菜单)
--   │   ├─ 4058 备份记录查询 (按钮)
--   │   ├─ 4059 创建备份 (按钮)
--   │   ├─ 4060 删除备份 (按钮)
--   │   ├─ 4061 下载备份 (按钮)
--   │   ├─ 4062 验证备份 (按钮)
--   │   └─ 4063 备份记录导出 (按钮)
--   ├─ 4064 备份配置 (菜单)
--   │   ├─ 4065 备份配置查询 (按钮)
--   │   ├─ 4066 创建配置 (按钮)
--   │   ├─ 4067 更新配置 (按钮)
--   │   ├─ 4068 删除配置 (按钮)
--   │   ├─ 4069 启用/禁用配置 (按钮)
--   │   └─ 4070 执行备份配置 (按钮)
--   └─ 4071 恢复记录 (菜单)
--       ├─ 4072 恢复记录查询 (按钮)
--       ├─ 4073 执行恢复 (按钮)
--       └─ 4074 恢复记录导出 (按钮)
--
-- 权限标识符：
-- - iot:backup:query          - 查询备份记录
-- - iot:backup:create         - 创建备份
-- - iot:backup:delete         - 删除备份
-- - iot:backup:download       - 下载备份
-- - iot:backup:validate       - 验证备份
-- - iot:backup:export         - 导出备份记录
-- - iot:backup:config         - 查询备份配置
-- - iot:backup:config:create  - 创建备份配置
-- - iot:backup:config:update  - 更新备份配置
-- - iot:backup:config:delete  - 删除备份配置
-- - iot:backup:config:enable  - 启用/禁用备份配置
-- - iot:backup:config:execute - 执行备份配置
-- - iot:restore:query         - 查询恢复记录
-- - iot:restore:execute       - 执行恢复
-- - iot:restore:export        - 导出恢复记录
