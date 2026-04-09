ALTER TABLE `lkt_core_menu`
    MODIFY COLUMN `name` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '菜单标识' AFTER `title`;


ALTER TABLE `lkt_core_menu`
    MODIFY COLUMN `action` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '菜单文件标识' AFTER `module`;

ALTER TABLE `lkt_return_order`
    MODIFY COLUMN `audit_time` timestamp NULL DEFAULT NULL COMMENT '审核时间' AFTER `re_photo`;

ALTER TABLE `lkt_system_message`
    MODIFY COLUMN `time` timestamp NULL DEFAULT NULL COMMENT '时间' AFTER `content`;