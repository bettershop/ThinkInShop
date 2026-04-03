ALTER TABLE lkt_sku
    ADD COLUMN `lang_code` varchar(255) NULL DEFAULT 'zh_CN' COMMENT '语种' AFTER `is_examine`,
    ADD COLUMN `country_num` int NULL DEFAULT 156 COMMENT '所属国家' AFTER `lang_code`;

-- 新建角色重新分配菜单。

update lkt_brand_class set country_num = 156