
ALTER TABLE `lkt_mch_distribution_record` 
ADD COLUMN `is_platform_account` tinyint(4) NULL DEFAULT 0 COMMENT '是否是平台 0.不是 1.是' AFTER `add_date`;
ALTER TABLE `lkt_mch_distribution` 
ADD COLUMN `name` varchar(255) NULL COMMENT '分账接收方全称' AFTER `account`;

ALTER TABLE `lkt_order` 
CHANGE COLUMN `Dividend_status` `dividend_status` tinyint(4) NULL DEFAULT 0 COMMENT '分账状态 0.不分账 1.分账' AFTER `wx_order_status`;