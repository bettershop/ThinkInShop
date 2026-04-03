ALTER TABLE `lkt_supplier_pro_class` ADD COLUMN `real_cid` INT(11) NULL COMMENT '实际商品分类id' AFTER `remark`; 
ALTER TABLE `lkt_order_details` ADD COLUMN `supplier_id` INT(11) DEFAULT 0 NULL COMMENT '供应商id' AFTER `supplier_settlement`;
ALTER TABLE `lkt_order` ADD COLUMN `supplier_id` INT(11) DEFAULT 0 NULL COMMENT '供应商id' AFTER `is_lssued`;