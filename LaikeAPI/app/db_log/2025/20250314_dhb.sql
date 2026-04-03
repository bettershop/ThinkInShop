
ALTER TABLE `lkt_core_menu` 
    ADD COLUMN `lang_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'zh_CN' COMMENT '语言' AFTER `is_tab`,
    ADD COLUMN `country_num` int(11) NULL DEFAULT 156 COMMENT '国家语种默认156' AFTER `lang_code`;

ALTER TABLE `lkt_core_menu` 
    MODIFY COLUMN `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL AFTER `s_id`,
    MODIFY COLUMN `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL AFTER `title`;

DROP TABLE IF EXISTS `lkt_lang`;
CREATE TABLE `lkt_lang` (
    `lang_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '语音名称',
    `lang_code` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '语言编码',
    `show_num` int DEFAULT NULL COMMENT '排序位置',
    `is_show` int DEFAULT 1 COMMENT '是否显示 1显示2不显示',
    `recycle` int DEFAULT 1 COMMENT '是否回收 1不回收 2 回收',
    `op_time` timestamp NULL DEFAULT NULL COMMENT '操作时间',
    `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    PRIMARY KEY (`id`),
    UNIQUE KEY `lang_code` (`lang_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='国际化语种信息表';

-- ----------------------------
-- Records of lkt_lang
-- ----------------------------
BEGIN;
INSERT INTO `lkt_lang` (`lang_name`, `lang_code`, `show_num`, `is_show`, `recycle`, `op_time`, `id`) VALUES ('中文简体', 'zh_CN', NULL, 1, 1, '2025-03-14 16:20:56', 1);
INSERT INTO `lkt_lang` (`lang_name`, `lang_code`, `show_num`, `is_show`, `recycle`, `op_time`, `id`) VALUES ('中文繁体', 'zh_TW', NULL, 1, 1, '2025-03-14 16:21:31', 2);
INSERT INTO `lkt_lang` (`lang_name`, `lang_code`, `show_num`, `is_show`, `recycle`, `op_time`, `id`) VALUES ('English', 'en_US', NULL, 1, 1, '2025-03-14 16:21:25', 3);
COMMIT;

alter table lkt_data_dictionary_name
    add lang_code VARCHAR(10) default 'zh_CN' not null comment '语种编码';

alter table lkt_data_dictionary_name
    add country_num int default 156 null comment '国家编码';
		
alter table lkt_data_dictionary_list
    add lang_code VARCHAR(20) default 'zh_CN' null comment '语种';

alter table lkt_data_dictionary_list
    add country_num int default 156 null comment '所属国家';

ALTER TABLE `lkt_sku` 
    ADD COLUMN `lang_code` varchar(255) NULL DEFAULT 'zh_CN' COMMENT '语种编码' AFTER `is_examine`,
    ADD COLUMN `country_num` int(11) NOT NULL DEFAULT 156 COMMENT '所属国家' AFTER `lang_code`;

ALTER TABLE lkt_customer
    ADD COLUMN store_langs mediumtext COMMENT '商城语种' AFTER merchant_logo;

alter table lkt_pro_label
    add lang_code varchar(10) default 'zh_CN' null comment '语种编码默认中文简体';

alter table lkt_pro_label
    add country_num int NULL DEFAULT 156 comment '国家语种默认156';

alter table lkt_product_class
    add lang_code varchar(10) default 'zh_CN' null comment '语种编码默认中文简体';

alter table lkt_product_class
    add country_num int default 156 null comment '国家语种默认156';

ALTER TABLE `lkt_jump_path` 
    ADD COLUMN `lang_code` varchar(10) NULL DEFAULT 'zh_CN' COMMENT '语种编码默认中文简体' AFTER `sid`,
    ADD COLUMN `country_num` int(11) NULL DEFAULT 156 COMMENT '所属国家' AFTER `lang_code`;

alter table lkt_brand_class
    add lang_code varchar(10) default 'zh_CN' null comment '语种编码默认中文简体';
ALTER TABLE `lkt_brand_class` 
    CHANGE COLUMN `producer` `country_num` int(11) NOT NULL DEFAULT 156 COMMENT '产地' AFTER `brand_y_name`;
update lkt_brand_class  set country_num = 156  where country_num = 1;

alter table lkt_freight
    add lang_code varchar(10) default 'zh_CN' null comment '语种编码';

alter table lkt_freight
    add country_num int(11) NOT NULL DEFAULT 156 comment '国家编码默认156中国';

ALTER TABLE lkt_product_list
    ADD COLUMN country_num int not NULL DEFAULT 156 COMMENT '国家代码 默认中国编码：156' AFTER is_appointment,
    ADD COLUMN lang_code varchar(10) not NULL DEFAULT 'zh_CN' COMMENT '国际化语言 默认：中文简体 zh_CN' AFTER country_num;

ALTER TABLE lkt_config
    ADD COLUMN default_lang_code varchar(20) not NULL DEFAULT 'zh_CN' COMMENT '商城默认语种' AFTER ios_download_link ,
    ADD COLUMN default_lang_currency varchar(255)  NULL COMMENT '商城默认币种' AFTER default_lang_code;

DROP TABLE IF EXISTS `lkt_currency`;
CREATE TABLE `lkt_currency`  (
  `id` int(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `currency_code` char(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ISO货币代码(如USD)',
  `currency_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '货币名称',
  `is_show` int(11) NOT NULL DEFAULT 1 COMMENT '是否展示 0 不展示 1展示 ',
  `currency_symbol` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '货币符号($)',
  `recycle` int(11) NOT NULL DEFAULT 0 COMMENT '是否删除 0 未删除 1已删除 ',
  `update_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '平台货币信息' ROW_FORMAT = Dynamic;


DROP TABLE IF EXISTS `lkt_currency_store`;
CREATE TABLE `lkt_currency_store`  (
  `store_id` int(11) NULL DEFAULT NULL COMMENT '商城id',
  `currency_id` int(11) NULL DEFAULT NULL COMMENT '货币id',
  `is_show` int(11) NOT NULL DEFAULT 1 COMMENT '是否展示 0 不展示 1展示 ',
  `default_currency` int(11) NULL DEFAULT 0 COMMENT '是否商城基础货币【结算货币】默认 0 否 1 是',
  `exchange_rate` decimal(10, 4) NOT NULL COMMENT '基础货币汇率',
  `recycle` int(11) NOT NULL DEFAULT 1 COMMENT '是否删除 0 未删除 1已删除 ',
  `update_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0)
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商城货币' ROW_FORMAT = Dynamic;
