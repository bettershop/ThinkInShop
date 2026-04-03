ALTER TABLE lkt_mch_config
    MODIFY COLUMN `is_display` tinyint(4) NULL DEFAULT 1 COMMENT '是否显示 0.不显示 1.显示' AFTER `commodity_setup`;

--邮政编码数据类型
ALTER TABLE lkt_service_address
    MODIFY COLUMN `code` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 000000 COMMENT '邮政编号' AFTER `address_xq`;