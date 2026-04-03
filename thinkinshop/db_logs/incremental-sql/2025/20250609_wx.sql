ALTER TABLE `lkt_order_data`
    ADD COLUMN `currency_symbol` varchar(255) NULL COMMENT '支付时货币符号' AFTER `source`,
    ADD COLUMN `exchange_rate` decimal(12, 2) NULL COMMENT '支付时货币汇率' AFTER `currency_symbol`,
    ADD COLUMN `currency_code` varchar(255) NULL COMMENT '支付时货币编码' AFTER `exchange_rate`;