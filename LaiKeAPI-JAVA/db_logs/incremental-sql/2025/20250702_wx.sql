ALTER TABLE lkt_menu
    ADD COLUMN `lang_code` varchar(255) NULL DEFAULT 'zh_CN' COMMENT '语言' AFTER `update_date`,
    ADD COLUMN `country_num` int NULL DEFAULT 156 COMMENT '国家编码' AFTER `lang_code`;