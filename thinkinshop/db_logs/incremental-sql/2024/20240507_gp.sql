ALTER TABLE `lkt_product_list`
    MODIFY COLUMN `outage_time` timestamp NULL DEFAULT NULL COMMENT '断供时间' AFTER `lower_remark`;

ALTER TABLE `lkt_member_pro`
    MODIFY COLUMN `add_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '添加时间' AFTER `recovery`;