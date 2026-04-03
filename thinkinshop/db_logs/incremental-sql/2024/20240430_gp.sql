ALTER TABLE `lkt_user`
    MODIFY COLUMN `sex` int(11) NOT NULL DEFAULT 1 COMMENT '性别默认男 0:未知 1:男 2:女' AFTER `clientid`,
    ADD COLUMN `is_default_value` int  NULL COMMENT '是否使用的默认用户配置   1是 2否  默认否' AFTER `user_from_id`;


ALTER TABLE `lkt_user`
    MODIFY COLUMN `is_default_value` int(11) NULL DEFAULT 2 COMMENT '是否使用的默认用户配置   1是 2否  默认否' AFTER `user_from_id`;

ALTER TABLE `lkt_user`
    ADD COLUMN `is_default_birthday` int(4) NULL DEFAULT 2 COMMENT '是否使用的默认日期配置   1是 2否  默认否' AFTER `is_default_value`;