
ALTER TABLE `lkt_user`
    MODIFY COLUMN `sex` tinyint(11) NOT NULL DEFAULT 1 COMMENT '性别默认男 0:未知 1:男 2:女' AFTER `clientid`,
    ADD COLUMN `is_default_value` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否使用的默认用户配置 1.是 2.否' AFTER `user_from_id`;
    ADD COLUMN `is_default_birthday` tinyint(4) NOT NULL DEFAULT 1 COMMENT '是否使用的默认日期配置 1.是 2.否' AFTER `is_default_value`;
