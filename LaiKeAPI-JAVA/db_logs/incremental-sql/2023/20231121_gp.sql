-- 优化48462  供应商订单打印
ALTER TABLE `lkt_print_setup`
    ADD COLUMN `supplier_id` int(11) NULL COMMENT '供应商id' AFTER `mch_id`;