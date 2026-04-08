--新增余额提现到微信余额
ALTER TABLE `lkt_withdraw`
    ADD COLUMN `withdraw_status` int(4) NULL COMMENT '提现类型 1银行卡  2微信余额' AFTER `examine_date`,
    ADD COLUMN `wx_status` varchar(50) NULL COMMENT '提现到微信余额提现状态：1进行中 2已完成 3提现失败' AFTER `withdraw_status`

ALTER TABLE `lkt_withdraw`
    MODIFY COLUMN `Bank_id` int(11) NULL COMMENT '银行卡id' AFTER `mobile`;
ALTER TABLE `v3_db`.`lkt_withdraw`
    MODIFY COLUMN `withdraw_status` int(4) NOT NULL DEFAULT 1 COMMENT '提现类型 1银行卡  2微信余额' AFTER `examine_date`;

ALTER TABLE `lkt_withdraw`
    ADD COLUMN `wx_open_id` varchar(100) NULL COMMENT '用户openid' AFTER `recovery`,
ADD COLUMN `wx_son` varchar(100) NULL COMMENT '微信提现到零钱订单号' AFTER `wx_open_id`,
ADD COLUMN `wx_name` varchar(100) NULL COMMENT '用户微信名称' AFTER `wx_son`;

ALTER TABLE `v3_db`.`lkt_withdraw`
    ADD COLUMN `realname` varchar(50) NULL COMMENT '用户微信余额提现真实姓名' AFTER `wx_name`;