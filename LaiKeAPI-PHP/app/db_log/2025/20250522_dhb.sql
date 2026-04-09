

ALTER TABLE `lkt_user` 
ADD COLUMN `preferred_currency` int(11) NULL DEFAULT 0 COMMENT '用户偏好货币id' AFTER `country_num`,
ADD COLUMN `fans_num` int(11) NOT NULL DEFAULT 0 AFTER `token_living_pc`;

ALTER TABLE `lkt_order` 
ADD COLUMN `currency_symbol` varchar(255) NULL COMMENT '支付时货币符号' AFTER `paypal_id`,
ADD COLUMN `exchange_rate` decimal(12, 2) NULL COMMENT '支付时货币汇率' AFTER `currency_symbol`,
ADD COLUMN `currency_code` varchar(255) NULL COMMENT '支付时货币编码' AFTER `exchange_rate`;