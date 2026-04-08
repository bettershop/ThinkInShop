ALTER TABLE `lkt_express`
    ADD COLUMN `wx_delivery_id` varchar(50) NULL COMMENT '微信物流公司id' AFTER `add_date`;

ALTER TABLE `lkt_distribution_grade`
    MODIFY COLUMN `datetime` timestamp NOT NULL COMMENT '创建时间' AFTER `sets`;

ALTER TABLE `lkt_mch_store_account`
    MODIFY COLUMN `add_date` timestamp NOT NULL COMMENT '添加时间' AFTER `password`,
    MODIFY COLUMN `last_login` timestamp NULL DEFAULT NULL COMMENT '上次登录时间' AFTER `add_date`;



ALTER TABLE `lkt_mch_admin`
    MODIFY COLUMN `add_date` timestamp NULL DEFAULT NULL COMMENT '添加时间' AFTER `password`,
    MODIFY COLUMN `last_time` timestamp NULL DEFAULT NULL COMMENT '最后一次登录时间' AFTER `recycle`;