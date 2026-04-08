ALTER TABLE lkt_diy ADD COLUMN theme_dict_code VARCHAR(16) COMMENT '主题类型code（字典code）'AFTER value;
ALTER TABLE lkt_diy ADD COLUMN theme_type TINYINT(1) COMMENT '主题类型 1:系统主题 2:自定义主题' DEFAULT 1 AFTER theme_dict_code;
ALTER TABLE lkt_diy ADD COLUMN remark VARCHAR(255) COMMENT '备注' AFTER value;
ALTER TABLE lkt_diy ADD COLUMN mch_id INT COMMENT  '店铺id' DEFAULT 0 AFTER store_id;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- diy页面
-- ----------------------------
DROP TABLE IF EXISTS `lkt_diy_page`;
CREATE TABLE `lkt_diy_page`  (
                                 `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '页面id',
                                 `store_id` int(11) NOT NULL COMMENT '商城id',
                                 `page_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '页面名称',
                                 `link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '链接',
                                 `page_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '页面对应的key',
                                 `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '缩略图',
                                 `page_context` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '页面内容',
                                 `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态 0:不启用 1:启用',
                                 `recycle` int(1) NOT NULL DEFAULT 0 COMMENT '是否回收 0：显示 1：回收',
                                 `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '添加时间',
                                 `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
                                 `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'diy页面' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- diy页面绑定关系
-- ----------------------------
DROP TABLE IF EXISTS `lkt_diy_page_bind`;
CREATE TABLE `lkt_diy_page_bind`  (
                                      `id` int(11) NOT NULL AUTO_INCREMENT,
                                      `diy_id` int(11) NOT NULL COMMENT 'diy主键',
                                      `diy_page_id` int(11) NOT NULL COMMENT '自定义页面id',
                                      `link_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'diy数据使用链接对应的json key',
                                      `unit` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '子组件value',
                                      `bind_time` datetime(0) NULL DEFAULT NULL COMMENT '绑定时间',
                                      PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'diy页面绑定关系' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;





