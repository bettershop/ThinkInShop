
ALTER TABLE `tp_db`.`lkt_diy` 
    MODIFY COLUMN `version` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '版本号' AFTER `id`,
    MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '页面名称' AFTER `version`,
    MODIFY COLUMN `value` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '页面数据' AFTER `name`,
    ADD COLUMN `tabber_info` mediumtext NULL COMMENT '导航配置信息' AFTER `value`,
    ADD COLUMN `tab_bar` mediumtext NULL COMMENT '导航' AFTER `tabber_info`,
    ADD COLUMN `remark` varchar(255) NULL COMMENT '备注' AFTER `tab_bar`,
    ADD COLUMN `theme_dict_code` varchar(255) NULL COMMENT '主题类型code（字典code）' AFTER `remark`,
    ADD COLUMN `theme_type` tinyint(1) NULL DEFAULT 1 COMMENT '主题类型 1:系统主题 2:自定义主题' AFTER `theme_dict_code`,
    ADD COLUMN `mch_id` int(11) NULL DEFAULT 0 COMMENT '店铺ID' AFTER `store_id`,
    ADD COLUMN `lang_code` varchar(255) NULL DEFAULT 'zh_CN' COMMENT '语言' AFTER `cover`;

/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80029
 Source Host           : localhost:3306
 Source Schema         : v3_new_db

 Target Server Type    : MySQL
 Target Server Version : 80029
 File Encoding         : 65001

 Date: 04/08/2025 09:49:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for lkt_diy_page
-- ----------------------------
DROP TABLE IF EXISTS `lkt_diy_page`;
CREATE TABLE `lkt_diy_page`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '页面id',
  `store_id` int(0) NOT NULL COMMENT '商城id',
  `page_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '页面名称',
  `type` tinyint(1) NULL DEFAULT NULL COMMENT '类型 1：系统页面 2：自定义页面',
  `link` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '链接',
  `page_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '页面对应的key',
  `image` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
  `page_context` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '页面内容',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '状态 0:不启用 1:启用',
  `recycle` int(0) NOT NULL DEFAULT 0 COMMENT '是否回收 0：显示 1：回收',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '添加时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 126 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'diy页面' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

/*
 Navicat Premium Data Transfer

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80029
 Source Host           : localhost:3306
 Source Schema         : v3_new_db

 Target Server Type    : MySQL
 Target Server Version : 80029
 File Encoding         : 65001

 Date: 04/08/2025 09:49:49
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for lkt_diy_page_bind
-- ----------------------------
DROP TABLE IF EXISTS `lkt_diy_page_bind`;
CREATE TABLE `lkt_diy_page_bind`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `diy_id` int(0) NOT NULL COMMENT 'diy主键',
  `diy_page_id` int(0) NOT NULL COMMENT '自定义页面id',
  `link_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'diy数据使用链接对应的json key',
  `unit` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '子组件value',
  `bind_time` datetime(0) NULL DEFAULT NULL COMMENT '绑定时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 182 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'diy页面绑定关系' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

ALTER TABLE `lkt_payment_config`
    ADD COLUMN `isdefaultpay` tinyint(1) NULL DEFAULT 2 COMMENT '是否默认支付方式 1是2否' AFTER `config_data`;