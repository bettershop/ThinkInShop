
ALTER TABLE `tp_db`.`lkt_flashsale_config` 
DROP COLUMN `buy_num`,
DROP COLUMN `imageurl`,
DROP COLUMN `is_sales`,
DROP COLUMN `remind`,
DROP COLUMN `rule`,
DROP COLUMN `order_failure`,
DROP COLUMN `deliver_remind`,
DROP COLUMN `package_settings`,
DROP COLUMN `same_piece`,
DROP COLUMN `same_order`,
ADD COLUMN `good_switch` tinyint(4) NOT NULL DEFAULT 0 COMMENT '自动评价设置 0.关闭 1.开启' AFTER `mch_id`;


ALTER TABLE `lkt_auction_config` 
MODIFY COLUMN `order_failure` bigint(20) NOT NULL DEFAULT 1 COMMENT '订单失效 (单位 秒)' AFTER `is_open`;