

ALTER TABLE `lkt_record` 
MODIFY COLUMN `money` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '操作金额' AFTER `user_id`,
MODIFY COLUMN `oldmoney` decimal(12, 2) NOT NULL DEFAULT 0.00 COMMENT '原有金额' AFTER `money`;

ALTER TABLE `lkt_order` 
MODIFY COLUMN `offset_balance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '抵扣余额' AFTER `remind`;