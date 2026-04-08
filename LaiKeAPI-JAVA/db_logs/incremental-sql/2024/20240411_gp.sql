--分销提现记录
ALTER TABLE `lkt_distribution_withdraw`
    MODIFY COLUMN `Bank_id` int(11) NULL COMMENT '银行卡id' AFTER `mobile`,
    ADD COLUMN `examine_date` timestamp(0) NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '提现审核时间' AFTER `is_mch`,
    ADD COLUMN `withdraw_status` int(4) NOT NULL DEFAULT 1 COMMENT '提现类型 1银行卡  2微信余额' AFTER `examine_date`,
    ADD COLUMN `wx_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提现到微信余额提现状态：1进行中 2已完成 3提现失败' AFTER `withdraw_status`,
    ADD COLUMN `wx_open_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户openid' AFTER `wx_status`,
    ADD COLUMN `wx_son` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '微信提现到零钱订单号' AFTER `wx_open_id`,
    ADD COLUMN `wx_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户微信名称' AFTER `wx_son`,
    ADD COLUMN `realname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户微信余额提现真实姓名' AFTER `wx_name`;


ALTER TABLE `lkt_distribution_withdraw`
    MODIFY COLUMN `wx_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 1 COMMENT '提现到微信余额提现状态：1进行中 2已完成 3提现失败' AFTER `withdraw_status`;



CREATE TABLE `lkt_distribution_income` (
                                           `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
                                           `store_id` int(11) NOT NULL DEFAULT '0' COMMENT '商城id',
                                           `user_id` char(15) CHARACTER SET utf8mb4 DEFAULT NULL COMMENT 'user_id',
                                           `estimated_income` decimal(10,2) DEFAULT NULL COMMENT '当日预估收益',
                                           `order_num` int(10) DEFAULT NULL COMMENT '当日有效订单数量',
                                           `order_price` decimal(10,2) DEFAULT NULL COMMENT '当日订单金额',
                                           `new_customer` int(10) DEFAULT NULL COMMENT '当日新增客户  新增绑定永久关系',
                                           `new_invitation` int(10) DEFAULT NULL COMMENT '当日新增邀请 新增绑定临时关系',
                                           `add_time` datetime DEFAULT NULL COMMENT '添加时间',
                                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='分销系统收益统计报表';