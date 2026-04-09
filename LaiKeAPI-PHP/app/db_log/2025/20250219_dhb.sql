
ALTER TABLE `lkt_integral_config` 
MODIFY COLUMN `score_ratio` varchar(255) NULL COMMENT '积分支付比例' AFTER `status`;

ALTER TABLE `lkt_order` 
ADD COLUMN `score_deduction` int(11) NOT NULL DEFAULT 0 COMMENT '积分支付抵扣' AFTER `grade_rate`

ALTER TABLE `lkt_order_details` 
ADD COLUMN `actual_total` decimal(12, 2) NULL COMMENT '实际支付金额' AFTER `num`,
ADD COLUMN `score_deduction` int(11) NOT NULL DEFAULT 0 COMMENT '积分支付抵扣' AFTER `actual_total`;