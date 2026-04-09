ALTER TABLE `lkt_order` 
ADD COLUMN `order_failure_time` int(0) NOT NULL DEFAULT 0 COMMENT '订单当前失效时间' AFTER `arrive_time`,
MODIFY COLUMN `VerifiedBy_type` tinyint(1) NOT NULL DEFAULT 1 COMMENT '核销人类型 1.店主核销 2.门店核销' AFTER `extraction_code_img`,
MODIFY COLUMN `VerifiedBy` int(11) NULL DEFAULT 0 COMMENT '核销人ID' AFTER `VerifiedBy_type`,
ADD COLUMN `old_freight` decimal(12, 2) NULL COMMENT '历史总运费' AFTER `is_put`,
ADD COLUMN `transaction_id` varchar(100) NULL COMMENT '微信返回支付单号唯一标识' AFTER `store_recycle`;
