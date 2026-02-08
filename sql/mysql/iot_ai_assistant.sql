-- IoT AI 智能助手模块
-- 对话表和消息表

-- ----------------------------
-- Table: iot_ai_conversation - AI 对话
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_ai_conversation` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '对话编号',
    `user_id`       BIGINT       NOT NULL COMMENT '用户编号',
    `title`         VARCHAR(255) NOT NULL DEFAULT '' COMMENT '对话标题',
    `model`         VARCHAR(100)          DEFAULT NULL COMMENT '模型标识',
    `message_count` INT          NOT NULL DEFAULT 0 COMMENT '消息数量',
    `creator`       VARCHAR(64)           DEFAULT '' COMMENT '创建者',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       VARCHAR(64)           DEFAULT '' COMMENT '更新者',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       BIT          NOT NULL DEFAULT 0 COMMENT '是否删除',
    `tenant_id`     BIGINT       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT AI 对话表';

-- ----------------------------
-- Table: iot_ai_message - AI 消息
-- ----------------------------
CREATE TABLE IF NOT EXISTS `iot_ai_message` (
    `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '消息编号',
    `conversation_id` BIGINT      NOT NULL COMMENT '对话编号',
    `role`            VARCHAR(20) NOT NULL COMMENT '角色: user/assistant/system/tool',
    `content`         TEXT                 DEFAULT NULL COMMENT '消息内容',
    `tool_calls`      JSON                 DEFAULT NULL COMMENT '工具调用信息(JSON)',
    `tool_call_id`    VARCHAR(100)         DEFAULT NULL COMMENT '工具调用ID(tool角色时使用)',
    `tool_name`       VARCHAR(100)         DEFAULT NULL COMMENT '工具名称(tool角色时使用)',
    `tokens`          INT         NOT NULL DEFAULT 0 COMMENT '消耗Token数',
    `creator`         VARCHAR(64)          DEFAULT '' COMMENT '创建者',
    `create_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`         VARCHAR(64)          DEFAULT '' COMMENT '更新者',
    `update_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         BIT         NOT NULL DEFAULT 0 COMMENT '是否删除',
    `tenant_id`       BIGINT      NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`),
    INDEX `idx_conversation_id` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT AI 消息表';
