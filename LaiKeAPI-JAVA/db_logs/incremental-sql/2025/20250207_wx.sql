ALTER TABLE `lkt_core_menu`
    ADD COLUMN `lang_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'zh_CN' COMMENT '语言' AFTER `is_tab`,
    ADD COLUMN `country_num` int(11) NULL DEFAULT 156 COMMENT '国家语种默认156' AFTER `lang_code`;

ALTER TABLE `lkt_core_menu`
    MODIFY COLUMN `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL AFTER `s_id`,
    MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL AFTER `title`;

ALTER TABLE `lkt_guide_menu`
    ADD COLUMN `lang_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'zh_CN' COMMENT '语言' AFTER `add_date`,
    ADD COLUMN `country_num` int(11) NULL DEFAULT 156 COMMENT '国家语种默认156' AFTER `lang_code`;


ALTER TABLE lkt_ds_country
    MODIFY COLUMN `code2` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT 'ITU电话代码' AFTER `code`,
    ADD COLUMN `num3` int NULL COMMENT '数字编码' AFTER `is_show`;


-- 地区表字段名修改
ALTER TABLE lkt_map
    CHANGE COLUMN `GroupID` `id` int NOT NULL AUTO_INCREMENT COMMENT '地区id' FIRST,
    CHANGE COLUMN `G_CName` `district_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '地区名称' AFTER `GroupID`,
    CHANGE COLUMN `G_ParentID` `district_pid` int NULL DEFAULT NULL COMMENT '父地区id' AFTER `G_CName`,
    CHANGE COLUMN `G_ShowOrder` `district_show_order` int NULL DEFAULT NULL COMMENT '显示排序' AFTER `G_ParentID`,
    CHANGE COLUMN `G_Level` `district_level` int NULL DEFAULT NULL COMMENT '层级' AFTER `G_ShowOrder`,
    CHANGE COLUMN `G_ChildCount` `district_childcount` int NULL DEFAULT 0 COMMENT '子地区数（未用）' AFTER `G_Level`,
    CHANGE COLUMN `G_Delete` `district_delete` int NULL DEFAULT 0 COMMENT '是否删除（未用）' AFTER `G_ChildCount`,
    CHANGE COLUMN `G_Num` `district_num` int NULL DEFAULT 0 COMMENT '（未用）' AFTER `G_Delete`,
    ADD COLUMN `district_country_num` int NULL DEFAULT 156 COMMENT '国家编码' AFTER `G_Num`,
    DROP PRIMARY KEY,
    ADD PRIMARY KEY (`id`) USING BTREE;


-- ----------------------------
-- Table structure for lkt_country_info
-- ----------------------------
-- ----------------------------
-- Table structure for lkt_lang
-- ----------------------------
DROP TABLE IF EXISTS `lkt_lang`;
CREATE TABLE `lkt_lang` (
                            `lang_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '语音名称',
                            `lang_code` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '语言编码',
                            `show_num` int DEFAULT NULL COMMENT '排序位置',
                            `is_show` int DEFAULT '1' COMMENT '是否显示 1显示2不显示',
                            `recycle` int DEFAULT '1' COMMENT '是否回收 1不回收 2 回收',
                            `op_time` timestamp NULL DEFAULT NULL COMMENT '操作时间',
                            `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `lang_code` (`lang_code`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='国际化语种信息表';

-- ----------------------------
-- Records of lkt_lang
-- ----------------------------
BEGIN;
INSERT INTO `lkt_lang` (`lang_name`, `lang_code`, `show_num`, `is_show`, `recycle`, `op_time`, `id`) VALUES ('中文简体', 'zh_CN', NULL, 1, 1, '2025-03-14 16:20:56', 1);
INSERT INTO `lkt_lang` (`lang_name`, `lang_code`, `show_num`, `is_show`, `recycle`, `op_time`, `id`) VALUES ('中文繁体', 'zh_TW', NULL, 1, 1, '2025-03-14 16:21:31', 2);
INSERT INTO `lkt_lang` (`lang_name`, `lang_code`, `show_num`, `is_show`, `recycle`, `op_time`, `id`) VALUES ('English', 'en_US', NULL, 1, 1, '2025-03-14 16:21:25', 3);
INSERT INTO `lkt_lang` (`lang_name`, `lang_code`, `show_num`, `is_show`, `recycle`, `op_time`, `id`) VALUES ('法语', 'fr', NULL, 1, 1, '2025-03-20 16:24:28', 4);
COMMIT;



ALTER TABLE lkt_user
    ADD COLUMN cpc varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '86' COMMENT '电话代码' AFTER fans_num ,
    ADD COLUMN country_num int NULL DEFAULT 156 COMMENT '国家代码' AFTER cpc ;

update lkt_user set cpc = '86' , country_num = 156;

UPDATE lkt_user set lang = 'zh_CN' WHERE lang = 'zh_cn';
UPDATE lkt_user set lang = 'zh_CN' WHERE lang = 'zh-cn';
UPDATE lkt_user set lang = 'zh_CN' WHERE lang = 'cn';
UPDATE lkt_user set lang = 'zh_CN' WHERE lang = 'zh';
UPDATE lkt_user set lang = 'en_US' WHERE lang = 'en';
UPDATE lkt_user set lang = 'zh_CN' WHERE lang = '';


-- 管理后台商品列表查询出所有 lang_pid 为空（is null）的商品记录 ，其他语种通过商品列表操作列的语种做切换 查看新增编辑不同语种
ALTER TABLE lkt_product_list
    ADD COLUMN country_num int not NULL DEFAULT 156 COMMENT '国家代码 默认中国编码：156' AFTER is_zc ,
    ADD COLUMN lang_code varchar(10) not NULL DEFAULT 'zh_CN' COMMENT '国际化语言 默认：中文简体 zh_CN' AFTER country_num;

-- 默认中文
update lkt_product_list set lang_code = 'zh_CN' ,country_num = 156;


ALTER TABLE lkt_config
    ADD COLUMN default_lang_code varchar(255) not NULL DEFAULT 'zh_CN' COMMENT '商城默认语种' AFTER ios_download_link;


ALTER TABLE lkt_customer
    ADD COLUMN store_langs mediumtext COMMENT '商城语种' AFTER merchant_logo;





