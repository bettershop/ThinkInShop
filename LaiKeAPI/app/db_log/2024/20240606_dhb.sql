
ALTER TABLE `lkt_mch` 
MODIFY COLUMN `integral_money` decimal(12, 0) NOT NULL COMMENT '商户积分' AFTER `review_result`,
MODIFY COLUMN `cashable_money` decimal(12, 2) NOT NULL COMMENT '可取现金额' AFTER `latitude`;