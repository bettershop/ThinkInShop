ALTER TABLE `lkt_order`
    ADD COLUMN `old_freight` decimal(12, 2) NULL COMMENT '历史总运费' AFTER `is_put`;