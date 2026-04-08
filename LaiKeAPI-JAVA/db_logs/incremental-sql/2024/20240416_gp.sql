ALTER TABLE `lkt_order`
    MODIFY COLUMN `old_freight` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '历史总运费' AFTER `is_put`;