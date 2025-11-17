-- ----------------------------
-- Table structure for report_visualization_dashboard
-- ----------------------------
CREATE TABLE IF NOT EXISTS `report_visualization_dashboard` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name` varchar(100) NOT NULL COMMENT '大屏名称',
    `description` varchar(500) DEFAULT NULL COMMENT '大屏描述',
    `pic_url` varchar(500) DEFAULT NULL COMMENT '预览图片 URL',
    `thumbnail` varchar(500) DEFAULT NULL COMMENT '缩略图 URL',
    `config` longtext NOT NULL COMMENT '大屏配置（JSON）',
    `width` int NOT NULL DEFAULT 1920 COMMENT '画布宽度',
    `height` int NOT NULL DEFAULT 1080 COMMENT '画布高度',
    `background_color` varchar(50) DEFAULT '#0e1117' COMMENT '背景颜色',
    `background_image` varchar(500) DEFAULT NULL COMMENT '背景图片 URL',
    `scale_mode` varchar(20) DEFAULT 'scale' COMMENT '屏幕适配模式',
    `status` tinyint NOT NULL DEFAULT 0 COMMENT '发布状态（0-未发布 1-已发布）',
    `password` varchar(50) DEFAULT NULL COMMENT '访问密码',
    `is_public` bit(1) DEFAULT b'0' COMMENT '是否公开',
    `category` varchar(50) DEFAULT NULL COMMENT '分类',
    `tags` varchar(200) DEFAULT NULL COMMENT '标签（逗号分隔）',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_creator` (`creator`) USING BTREE,
    KEY `idx_category` (`category`) USING BTREE,
    KEY `idx_status` (`status`) USING BTREE,
    KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='可视化大屏表';

-- ----------------------------
-- Menu SQL for visualization dashboard
-- ----------------------------
-- 菜单 SQL
INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
    '可视化大屏', '', 2, 0, 2153,
    'visualization', 'ep:data-analysis', 'report/visualization/index', 0, 'ReportVisualization'
);

-- 按钮父菜单ID
-- 获取刚插入的菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '大屏查询', 'report:visualization:query', 3, 1, @parentId, '', '', '', 0
);

INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '大屏创建', 'report:visualization:create', 3, 2, @parentId, '', '', '', 0
);

INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '大屏更新', 'report:visualization:update', 3, 3, @parentId, '', '', '', 0
);

INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '大屏删除', 'report:visualization:delete', 3, 4, @parentId, '', '', '', 0
);

INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
    '大屏导出', 'report:visualization:export', 3, 5, @parentId, '', '', '', 0
);
