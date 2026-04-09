
ALTER TABLE `lkt_config` 
CHANGE COLUMN `default_lang_currency` `default_currency` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '商城默认币种' AFTER `default_lang_code`;

ALTER TABLE `lkt_customer` 
ADD COLUMN `cpc` varchar(255) NULL DEFAULT '86' COMMENT '区号' AFTER `store_langs`;

ALTER TABLE `lkt_jump_path` 
ADD COLUMN `mch_id` int(11) NULL COMMENT '店铺ID' AFTER `store_id`;


ALTER TABLE `lkt_order` 
ADD COLUMN `stripe_id` varchar(255) NULL COMMENT 'stripe支付会话ID（用于前端调起支付）' AFTER `capture_id`,
ADD COLUMN `stripe_payment_intent` varchar(255) NULL COMMENT 'Stripe支付意图ID，stripe主要标识，用于退款、查询订单状态' AFTER `stripe_id`;

ALTER TABLE `lkt_order_details` 
ADD COLUMN `p_integral` int(11) NULL DEFAULT 0 COMMENT '产品积分（单个产品对应的积分，允许为NULL）' AFTER `store_self_delivery`;

ALTER TABLE `lkt_order_details` 
MODIFY COLUMN `platform_coupon_price` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '平台优惠券优惠金额' AFTER `write_time_id`,
MODIFY COLUMN `store_coupon_price` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '店铺优惠券优惠金额' AFTER `platform_coupon_price`;