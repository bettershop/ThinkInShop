ALTER TABLE `lkt_record_details`
    ADD COLUMN `currency_symbol` varchar(255) NULL COMMENT '支付时货币符号' AFTER `add_time`,
ADD COLUMN `exchange_rate` decimal(12, 2) NULL COMMENT '支付时货币汇率' AFTER `currency_symbol`,
ADD COLUMN `currency_code` varchar(255) NULL COMMENT '支付时货币编码' AFTER `exchange_rate`;


ALTER TABLE `lkt_payment_config`
    ADD COLUMN `isdefaultpay` tinyint(1) NULL DEFAULT 2 COMMENT '是否默认支付方式 1是2否' AFTER `config_data`;