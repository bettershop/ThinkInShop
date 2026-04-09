
ALTER TABLE `lkt_record_details` 
ADD COLUMN `currency_symbol` varchar(255) NULL COMMENT '支付时货币符号' AFTER `add_time`,
ADD COLUMN `exchange_rate` decimal(12, 2) NULL COMMENT '支付时货币汇率' AFTER `currency_symbol`,
ADD COLUMN `currency_code` varchar(255) NULL COMMENT '支付时货币编码' AFTER `exchange_rate`;

ALTER TABLE `lkt_order_data`
    ADD COLUMN `currency_symbol` varchar(255) NULL COMMENT '支付时货币符号' AFTER `source`,
    ADD COLUMN `exchange_rate` decimal(12, 2) NULL COMMENT '支付时货币汇率' AFTER `currency_symbol`,
    ADD COLUMN `currency_code` varchar(255) NULL COMMENT '支付时货币编码' AFTER `exchange_rate`;

ALTER TABLE `lkt_mch` 
    ADD COLUMN `cpc` varchar(255) NULL COMMENT '区号' AFTER `ID_number`;

ALTER TABLE `lkt_menu` 
    ADD COLUMN `lang_code` varchar(255) NULL DEFAULT 'zh_CN' COMMENT '语言' AFTER `update_date`,
    ADD COLUMN `country_num` int(11) NULL DEFAULT 156 COMMENT '国家语种默认156' AFTER `lang_code`;

ALTER TABLE `lkt_product_class` 
    ADD COLUMN `notset` int(11) NULL DEFAULT 0 COMMENT '未设置 默认值为0  为1的时候表示是未设置 ' AFTER `country_num`;
ALTER TABLE `lkt_brand_class` 
    ADD COLUMN `notset` int(11) NULL DEFAULT 0 COMMENT '未设置 默认值为0  为1的时候表示是未设置 ' AFTER `lang_code`;